package vip.smart3makerspaces.peoplecounter

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
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
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
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vip.smart3makerspaces.peoplecounter.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var captureTimer: Timer
    private var isCapturing = false
    private lateinit var db: AppDatabase
    private lateinit var personCountDao: PersonCountDao
    private lateinit var chart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo buttons
        viewBinding.imageCaptureButton.setOnClickListener { togglePhotoStream() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize AWS Amplify
        try {
            // Add the AWSCognitoAuthPlugin and AWSPredictionsPlugin plugins
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSPredictionsPlugin())
            Amplify.configure(applicationContext)

            Log.i(TAG, "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e(TAG, "Could not initialize Amplify", error)
        }

        chart = findViewById(R.id.chart)
        val xAxis = chart.xAxis
        val xAxisFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                val date = Date(value.toLong())
                val dateTimeFormatter = SimpleDateFormat("HH:mm:ss MM/dd")
                return dateTimeFormatter.format(date)
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1000f
        xAxis.setLabelCount(5, true)
        xAxis.isGranularityEnabled = true
        xAxis.valueFormatter = xAxisFormatter
        xAxis.labelRotationAngle = 300f
        xAxis.textColor = Color.parseColor("#FFFFFF")

        val yAxis = chart.axisLeft
        yAxis.textColor = Color.parseColor("#FFFFFF")

        chart.legend.textColor = Color.parseColor("#FFFFFF")

        // Build person count database and DAO
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "person-count-db"
        ).build()
        personCountDao = db.personCountDao()

        val personCountObserver = Observer<List<PersonCount>> { data ->
            val entries = mutableListOf<Entry>()
            for (personCount in data) {
                entries.add(Entry(personCount.time.toFloat(), personCount.count.toFloat()))
            }
            entries.sortWith(EntryXComparator())
            val dataSet = LineDataSet(entries, "People counted")
            val lineData = LineData(dataSet)
            chart.data = lineData
            chart.data.setValueTextColor(Color.parseColor("#FFFFFF"))
            chart.invalidate()
            Log.i(TAG, "Updated line chart")
        }
        personCountDao.getAll().observe(this, personCountObserver)
    }

    private suspend fun compressImage(uri: Uri): Bitmap {
        // Run code on I/O thread instead of main thread
        return withContext(Dispatchers.IO) {
            // Create a temp file from the given URI
            val uriInputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile(
                UUID.randomUUID().toString(),
                "",
                this@MainActivity.cacheDir
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
                this@MainActivity,
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

    private suspend fun detectPeople(image: Bitmap): Int {
        try {
            val result = Amplify.Predictions.identify(IdentifyActionType.DETECT_LABELS, image)
            val identifyResult = result as IdentifyLabelsResult

            for (label in identifyResult.labels) {
                // Return the number of people detected
                if (label.name == "Person") {
                    Log.i(TAG, "Detected ${label.boxes.size} person(s)")
                    return label.boxes.size
                }
            }
        } catch (error: PredictionsException) {
            Log.e(TAG, "Label detection failed", error)
        }

        // Return -1 if error occurred during label detection
        return -1
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
                            // Send photo to Rekognition to detect number of people
                            val count = detectPeople(compressedBitmap)
                            Log.i(TAG, "Returned $count from detectPeople")
                            if (count != -1) {  // If detection succeeded
                                // Add data to person count database
                                personCountDao.insertAll(
                                    PersonCount(
                                        timestamp,
                                        count
                                    )
                                )
                                Log.i(TAG, "Inserted ($timestamp, $count) to database")
                            }
                            // Delete captured photo
                            contentResolver.delete(it, null, null)
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
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
        private const val TAG = "MainActivity"
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
