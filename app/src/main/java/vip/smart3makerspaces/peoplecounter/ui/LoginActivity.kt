package vip.smart3makerspaces.peoplecounter.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import vip.smart3makerspaces.peoplecounter.R
import vip.smart3makerspaces.peoplecounter.data.GoogleSheetsRepository
import vip.smart3makerspaces.peoplecounter.data.UserPreferencesRepository
import java.io.IOException

class LoginActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val TAG = "LoginActivity"

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    .addOnSuccessListener { account ->
                        Log.i(TAG, "Successfully signed into account: ${account.email}")

                        // Initialize Google Sheets API service
                        val scopes = listOf(SheetsScopes.SPREADSHEETS)
                        val credential = GoogleAccountCredential.usingOAuth2(this, scopes)
                        credential.selectedAccount = account.account
                        val jsonFactory = JacksonFactory.getDefaultInstance()
                        val httpTransport = NetHttpTransport()
                        val service = Sheets.Builder(httpTransport, jsonFactory, credential)
                            .setApplicationName(getString(R.string.app_name))
                            .build()
                        val googleSheetsRepo = GoogleSheetsRepository(service)

                        launch(Dispatchers.IO) {
                            // Create spreadsheet and save SPREADSHEET_ID in DataStore
                            try {
                                val spreadsheetId = googleSheetsRepo.createSpreadsheet()
                                UserPreferencesRepository(applicationContext.dataStore)
                                    .updateSpreadsheetId(spreadsheetId)

                                Log.i(TAG, "Created spreadsheet ID: $spreadsheetId, starting RecognitionActivity")
                                val openCameraIntent = Intent(this@LoginActivity, RecognitionActivity::class.java)
                                this@LoginActivity.startActivity(openCameraIntent)
                            } catch (e: IOException) {
                                Log.e(TAG, e.message!!)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, e.message!!)
                    }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)

        requestSignIn(this)
    }

    private fun requestSignIn(context: Context) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, signInOptions)

        signInLauncher.launch(client.signInIntent)
    }
}