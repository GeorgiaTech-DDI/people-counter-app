package vip.smart3makerspaces.peoplecounter

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
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

    private val photoPicker =
        registerForActivityResult(PickVisualMedia())
        { uri ->
            if (uri != null) {
                Log.d(TAG, "Selected media with URI: $uri")

                // Set imageView to the selected image's URI
                findViewById<ImageView>(R.id.imageView)
                    .setImageURI(uri)

                // Create a bitmap using selected image
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                } else {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(contentResolver, uri)
                    )
                }

                // Detect labels in the bitmap
                detectLabels(bitmap)
            } else {
                Log.d(TAG, "No media selected")
            }
        }

    private fun pickSingleImage() {
        photoPicker.launch(PickVisualMediaRequest(
                PickVisualMedia.ImageOnly
            )
        )
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