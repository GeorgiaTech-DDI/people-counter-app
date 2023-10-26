package vip.smart3makerspaces.peoplecounter.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.predictions.PredictionsException
import com.amplifyframework.predictions.aws.AWSPredictionsPlugin
import com.amplifyframework.predictions.models.IdentifyActionType
import com.amplifyframework.predictions.result.IdentifyLabelsResult
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vip.smart3makerspaces.peoplecounter.CounterApplication
import vip.smart3makerspaces.peoplecounter.R
import vip.smart3makerspaces.peoplecounter.data.GoogleSheetsRepository
import vip.smart3makerspaces.peoplecounter.data.Sheet
import vip.smart3makerspaces.peoplecounter.data.UserPreferencesRepository
import vip.smart3makerspaces.peoplecounter.data.entity.Count
import vip.smart3makerspaces.peoplecounter.data.entity.Person
import vip.smart3makerspaces.peoplecounter.databinding.ActivityRecognitionBinding
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.properties.Delegates

class RecognitionActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityRecognitionBinding
    private val recognitionViewModel: RecognitionViewModel by viewModels {
        RecognitionViewModelFactory((application as CounterApplication).roomRepository)
    }

    private var imageCapture: ImageCapture? = null
    private lateinit var captureTimer: Timer
    private var isCapturing = false
    private lateinit var chart: LineChart
    private var firstTimestamp by Delegates.notNull<Long>()
    private lateinit var googleSheetsRepository: GoogleSheetsRepository
    private lateinit var spreadsheetId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRecognitionBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        requestCameraPermissions()
        initializeAmplify()
        initializeSheetsRepo()
        initializeChart()
        setupObservers()

        // Set up the listeners for take photo buttons
        viewBinding.imageCaptureButton.setOnClickListener { togglePhotoStream() }

        // Save spreadsheet ID from user preferences
        lifecycleScope.launch(Dispatchers.Main) {
            spreadsheetId = UserPreferencesRepository(dataStore).fetchInitialPreferences().spreadsheetId
        }
    }

    private fun requestCameraPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun initializeAmplify() {
        try {
            // Add the AWSCognitoAuthPlugin and AWSPredictionsPlugin plugins
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSPredictionsPlugin())
            Amplify.configure(applicationContext)

            Log.i(TAG, "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e(TAG, "Could not initialize Amplify", error)
        }
    }

    private fun initializeChart() {
        chart = findViewById(R.id.chart)

        // Disable chart description and legend for cleaner look
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.labelRotationAngle = 300f

        // Convert Unix timestamp in Long to human-readable String for x-axis
        val xAxisFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                val timestamp = firstTimestamp + (value.toLong() * 1000L)
                val date = Date(timestamp)
                val dateTimeFormatter = getDateTimeInstance()
                return dateTimeFormatter.format(date)
            }
        }
        xAxis.valueFormatter = xAxisFormatter

        chart.setBackgroundColor(Color.parseColor("#FFFFFF"))
    }

    private fun initializeSheetsRepo() {
        val account = GoogleSignIn.getLastSignedInAccount(applicationContext)

        if (account != null) {
            val scopes = listOf(SheetsScopes.SPREADSHEETS)
            val credential = GoogleAccountCredential.usingOAuth2(this, scopes)
            credential.selectedAccount = account.account
            val jsonFactory = JacksonFactory.getDefaultInstance()
            val httpTransport = NetHttpTransport()
            val service = Sheets.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(getString(R.string.app_name))
                .build()
            googleSheetsRepository = GoogleSheetsRepository(service)
        } else {
            Log.e(TAG, "Initializing Google Sheets repo failed")
        }
    }

    private fun setupObservers() {
        recognitionViewModel.allCounts.observe(this) { counts ->
            counts.let {
                if (counts.isNotEmpty()) {
                    val entries = mutableListOf<Entry>()
                    firstTimestamp = counts.first().timestamp
                    for (personCount in counts) {
                        // Since Float cannot reliably hold large Unix timestamps,
                        // use an offset based on the first timestamp instead
                        val offset = ((personCount.timestamp - firstTimestamp) / 1000L).toFloat()
                        entries.add(Entry(offset, personCount.count.toFloat()))
                    }
                    entries.sortWith(EntryXComparator())
                    val dataSet = LineDataSet(entries, "People counted")
                    val lineData = LineData(dataSet)
                    chart.data = lineData
                    chart.invalidate()
                    Log.i(TAG, "Updated line chart")
                }
            }
        }
    }

    private suspend fun compressImage(uri: Uri): Bitmap {
        // Run code on I/O thread instead of main thread
        return withContext(Dispatchers.IO) {
            // Create a temp file from the given URI
            val uriInputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile(
                UUID.randomUUID().toString(),
                "",
                this@RecognitionActivity.cacheDir
            )
            Log.i(TAG, "Created temp file: ${tempFile.path}")

            // Copy original file to temp file
            val fileOutputStream = FileOutputStream(tempFile)
            uriInputStream?.copyTo(fileOutputStream)
            uriInputStream?.close()
            fileOutputStream.close()
            Log.i(TAG, "Original file size in bytes: ${tempFile.length()}")

            // Compress the file to AWS Rekognition size limits (5MB)
            val compressedFile = Compressor.compress(
                this@RecognitionActivity,
                tempFile
            ) {
                default()
                size(5_242_880)
            }
            Log.i(TAG, "Compressed file size in bytes: ${compressedFile.length()}")

            // Convert the compressed file into a bitmap
            val compressedBitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    compressedFile.toUri()
                )
            } else {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        contentResolver, compressedFile.toUri())
                )
            }

            tempFile.deleteOnExit() // Schedule the temp file for deletion

            return@withContext compressedBitmap
        }
    }

    private suspend fun detectPeople(image: Bitmap): List<Person> {
        val result = Amplify.Predictions.identify(IdentifyActionType.DETECT_LABELS, image)
        val identifyResult = result as IdentifyLabelsResult

        val personList = mutableListOf<Person>()

        for (label in identifyResult.labels) {
            // Return a list of people detected
            if (label.name == "Person") {
                Log.i(TAG, "Detected ${label.boxes.size} person(s)")

                for (box in label.boxes) {
                    val person = Person(
                        timestamp = 0L,
                        confidence = label.confidence,
                        left = box.left,
                        top = box.top,
                        right = box.right,
                        bottom = box.bottom
                    )
                    personList.add(person)
                }
            }
        }

        return personList
    }

    private fun togglePhotoStream() {
        if (isCapturing) {
            // Cancel image capture scheduler
            captureTimer.cancel()

            // Change text of image capture button to start
            viewBinding.imageCaptureButton.text = getString(R.string.start_capture)

            isCapturing = false
        } else {
            isCapturing = true

            // Change text of image capture button to start
            viewBinding.imageCaptureButton.text = getString(R.string.stop_capture)

            // Schedule a image capture every 5 seconds
            captureTimer = Timer()
            captureTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    takePhoto()
                }
            }, 0, 5000)
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val timestamp = System.currentTimeMillis()
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(timestamp)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    lifecycleScope.launch(Dispatchers.IO) {
                        output.savedUri?.let {
                            // Compress captured photo
                            val compressedBitmap = compressImage(it)

                            try {
                                // Send photo to Rekognition to detect people
                                val peopleDetected = detectPeople(compressedBitmap)
                                Log.i(TAG, "Returned list of ${peopleDetected.size} people from detectPeople")

                                // Change the timestamp of each detected person to the image timestamp
                                for (person in peopleDetected) {
                                    person.timestamp = timestamp

                                    // Use the RecognitionViewModel to add Person to the database
                                    recognitionViewModel.insertPersonToDatabase(person)

                                    // Append person values to spreadsheet
                                    val personValues = listOf(
                                        person.timestamp.toString(),
                                        person.confidence.toString(),
                                        person.left.toString(),
                                        person.top.toString(),
                                        person.right.toString(),
                                        person.bottom.toString())
                                    Log.i(TAG, "Inserted ($timestamp, ${personValues.joinToString()}) to Person table")
                                    googleSheetsRepository.appendRow(spreadsheetId, Sheet.PERSON.title, personValues)
                                }

                                val personCount = Count(
                                    timestamp,
                                    peopleDetected.size
                                )
                                // Use the RecognitionViewModel to add Count to the database
                                recognitionViewModel.insertCountToDatabase(personCount)

                                val countValues = listOf(
                                    personCount.timestamp.toString(),
                                    personCount.count.toString()
                                )
                                Log.i(TAG, "Inserted (${countValues.joinToString()}}) to Count table")
                                googleSheetsRepository.appendRow(spreadsheetId, Sheet.COUNT.title, countValues)
                            } catch (error: PredictionsException) {
                                Log.e(TAG, "label detection failed", error)
                            } finally {
                                // Delete captured photo
                                contentResolver.delete(it, null, null)
                            }
                        }
                    }
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "RecognitionActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
