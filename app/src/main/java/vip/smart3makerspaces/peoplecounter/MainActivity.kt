package vip.smart3makerspaces.peoplecounter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.content.ContentUris
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.predictions.aws.AWSPredictionsPlugin
import com.amplifyframework.predictions.models.IdentifyActionType
import com.amplifyframework.predictions.result.IdentifyLabelsResult
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        // Set resultText to be scrollable
        findViewById<TextView>(R.id.resultText).movementMethod =
            ScrollingMovementMethod()

        findViewById<Button>(R.id.pickImageButton)
            .setOnClickListener {
                pickSingleImage()
            }
    }

    // function to get image URIs
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
        val list = getImageURI();
        // loops through each uri and turns it into bitmap
        list.forEach { item ->
            var bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
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
    }


    private fun detectLabels(image: Bitmap) {
        Amplify.Predictions.identify(IdentifyActionType.DETECT_LABELS, image,
            { result ->
                val identifyResult = result as IdentifyLabelsResult
                val resultText = findViewById<TextView>(R.id.resultText)
                resultText.text = ""

                for (label in identifyResult.labels) {
                    // Append label name
                    resultText.append("Detected ${label.name}\n")

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
                        resultText.append(" At $box\n")
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
}
