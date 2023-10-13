package vip.smart3makerspaces.peoplecounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteSheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    companion object {
        private const val REQUEST_SIGN_IN = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestSignIn(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener { account ->
                        val scopes = listOf(SheetsScopes.SPREADSHEETS)
                        val credential = GoogleAccountCredential.usingOAuth2(this, scopes)
                        credential.selectedAccount = account.account
                        val jsonFactory = JacksonFactory.getDefaultInstance()
                        val httpTransport = NetHttpTransport()
                        val service = Sheets.Builder(httpTransport, jsonFactory, credential)
                            .setApplicationName(getString(R.string.app_name))
                            .build()
                        createSpreadsheet(service)
                    }
                    .addOnFailureListener { e ->
                        Log.e("LoginActivity", e.message!!)
                    }
            }
        }
    }

    private fun requestSignIn(context: Context) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, signInOptions)
        startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
    }

    private fun createSpreadsheet(service: Sheets) {
        var spreadsheet = Spreadsheet()
            .setProperties(
                SpreadsheetProperties()
                    .setTitle("People Counter Data")
            )
        launch(Dispatchers.IO) {
            spreadsheet = service.spreadsheets().create(spreadsheet).execute()
            Log.i("LoginActivity", "ID: ${spreadsheet.spreadsheetId}")

            var requests = mutableListOf<Request>();

            requests.add(Request()
                .setAddSheet(
                    AddSheetRequest()
                        .setProperties(
                            SheetProperties()
                                .setTitle("count"))))
            requests.add(Request()
                .setAddSheet(
                    AddSheetRequest()
                        .setProperties(
                            SheetProperties()
                                .setTitle("person"))))
            requests.add(Request()
                .setDeleteSheet(
                    DeleteSheetRequest()
                        .setSheetId(spreadsheet.sheets.first().properties.sheetId)
                ))

            val batchUpdateSpreadsheetRequest = BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
            service.spreadsheets().batchUpdate(spreadsheet.spreadsheetId, batchUpdateSpreadsheetRequest).execute()

            val countLabels: ValueRange = ValueRange()
                .setValues(listOf(listOf("timestamp", "count")))
            val personLabels: ValueRange = ValueRange()
                .setValues(listOf(listOf("timestamp", "confidence", "left", "top", "right", "bottom")))
            service.spreadsheets().values().update(spreadsheet.spreadsheetId, "count!A1:B1", countLabels)
                .setValueInputOption("RAW")
                .execute()
            service.spreadsheets().values().update(spreadsheet.spreadsheetId, "person!A1:F1", personLabels)
                .setValueInputOption("RAW")
                .execute()
        }
    }
}