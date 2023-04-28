package vip.smart3makerspaces.peoplecounter

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.lifecycle.lifecycleScope
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.predictions.aws.AWSPredictionsPlugin
import com.amplifyframework.predictions.models.IdentifyActionType
import com.amplifyframework.predictions.result.IdentifyLabelsResult
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
    }

/*    // function to get image URIs
    private fun getImageURI(): ArrayList<Uri> {
        val list = ArrayList<Uri>()
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val mCursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        // uses cursor object to iterate through image storage
        mCursor.use { cursor ->
            cursor?.let {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                while (cursor.moveToNext()) {
                    val imagePath = cursor.getString(columnIndex)
                    val imageFile = File(imagePath)
                    if (imageFile.exists()) {
                        val imageUri = Uri.fromFile(imageFile)
                        list.add(imageUri)
                    }
                }
            }
        }
        return list
    }

    private fun getBitmapFromUri() {
        // list of image URIs
        val list = getImageURI()
        // loops through each uri and turns it into bitmap
        list.forEach { item ->
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, item)
            } else {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(contentResolver, item)
                )
            }
            // detect labels function
            detectLabels(bitmap)
            // deletes image after use
            contentResolver.delete(item, null, null)
        }
    }
    private fun timer() {
        val timer = Timer()
        val delay = 0L // Start immediately
        val period = 5 * 60 * 1000L // 5 minutes in milliseconds

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                getBitmapFromUri()
            }
        }, delay, period)
    }*/

    private suspend fun compressImageFromUri(uri: Uri): Bitmap {
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

    private fun detectLabels(image: Bitmap) {
        Amplify.Predictions.identify(IdentifyActionType.DETECT_LABELS, image,
            { result ->
                val identifyResult = result as IdentifyLabelsResult

                for (label in identifyResult.labels) {
                    // Log label properties
                    Log.i(TAG, "Name: ${label.name}")
                    Log.i(TAG, "Confidence: ${label.confidence}")
                    Log.i(TAG, "Box: ${label.box}")
                    Log.i(TAG, "Id: ${label.id}")
                    Log.i(TAG, "Polygon: ${label.polygon}")
                    Log.i(TAG, "Value: ${label.value}")
                    Log.i(TAG, "Type alias: ${label.typeAlias}")
                    Log.i(TAG, "Boxes:")

                    for (box in label.boxes) {
                        // Append label bounding boxes
                        Log.i(TAG, "Box: $box")
                    }

                    // Log parent labels
                    Log.i(TAG, "Parent labels:")
                    for (parentLabel in label.parentLabels) {
                        Log.i(TAG, "Parent label: $parentLabel")
                    }
                }
            },
            { Log.e(TAG, "Label detection failed", it) }
        )
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
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
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

        // Set up image capture listener, which is triggered after photo has
        // been taken
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
                            val compressedBitmap = compressImageFromUri(it)
                            detectLabels(compressedBitmap)
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
