package vip.smart3makerspaces.testapp

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.resultText).movementMethod = ScrollingMovementMethod();
        try {
            // Add these lines to add the AWSCognitoAuthPlugin and AWSPredictionsPlugin plugins
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSPredictionsPlugin())
            Amplify.configure(applicationContext)

            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }

        findViewById<Button>(R.id.pickImageButton)
            .setOnClickListener {
                pickSingleImage()
            }


    }

    private val pickMedia =
        registerForActivityResult(PickVisualMedia()
        ) { uri: Uri? ->
            val imageView = findViewById<ImageView>(R.id.imageView)
            if (uri != null) {
                Log.d("PickMedia", "Selected URI: $uri")
                imageView.setImageURI(uri)

                lateinit var bitmap: Bitmap
                if (Build.VERSION.SDK_INT < 28) {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                } else {
                    val imageSource = ImageDecoder.createSource(contentResolver, uri)
                    bitmap = ImageDecoder.decodeBitmap(imageSource)
                }

                runBlocking {
                    launch {
                        detectLabels(bitmap)
                    }
                }
            } else {
                Log.d("PickMedia", "No media selected")
            }
        }
    private fun pickSingleImage() {
        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }

    private fun detectLabels(image: Bitmap) {
        Amplify.Predictions.identify(IdentifyActionType.DETECT_LABELS, image,
            { result ->
                val identifyResult = result as IdentifyLabelsResult
                val resultText = findViewById<TextView>(R.id.resultText)
                resultText.text = ""
                for (label in identifyResult.labels) {
                    resultText.append("${label.name}: ${label.confidence}\n")
                }
            },
            { Log.e("MyAmplifyApp", "Label detection failed", it) }
        )
    }
}