package vip.smart3makerspaces.peoplecounter.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteSheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

enum class Sheet(val title: String) {
    COUNT("count"),
    PERSON("person")
}

class GoogleSheetsRepository(private val service: Sheets) {

    private val TAG = "GoogleSheetsRepo"
    private val SPREADSHEET_TITLE = "People Counter Data"
    private val SHEET_TITLES = listOf<String>("count", "person")
    private val SHEET_COLUMN_HEADERS = mapOf<String, List<String>>(
        "count" to listOf<String>("timestamp", "count"),
        "person" to listOf<String>("timestamp", "confidence", "left", "top", "right", "bottom"),
    )

    private fun addSheetToRequests(requests: MutableList<Request>, title: String) {
        val addSheetRequest = AddSheetRequest().setProperties(SheetProperties().setTitle(title))
        requests.add(Request().setAddSheet(addSheetRequest))
    }

    @Throws(IOException::class)
    suspend fun createSpreadsheet(): String {
        return withContext(Dispatchers.IO) {
            val spreadsheet = Spreadsheet()
                .setProperties(SpreadsheetProperties().setTitle(SPREADSHEET_TITLE))
            val spreadsheetResult = service.spreadsheets().create(spreadsheet).execute()
            val spreadsheetId = spreadsheetResult.spreadsheetId
            Log.i(TAG, "Created spreadsheet with ID: $spreadsheetId")

            var requests = mutableListOf<Request>();

            // Add requests to create sheets with the titles in SHEET_TITLES
            for (title in SHEET_TITLES) {
                addSheetToRequests(requests, title)
                Log.i(TAG, "Added request to create sheet with title: $title")
            }

            // Add request to delete first sheet with default title
            val firstSheetId = spreadsheetResult.sheets.first().properties.sheetId
            requests.add(Request().setDeleteSheet(DeleteSheetRequest().setSheetId(firstSheetId)))
            Log.i(TAG, "Added request to delete first sheet with ID: $firstSheetId")

            // Run batch update with requests
            val batchUpdateSpreadsheetRequest = BatchUpdateSpreadsheetRequest()
                .setRequests(requests)
            service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateSpreadsheetRequest)
                .execute()

            SHEET_COLUMN_HEADERS.forEach { headers ->
                val columnHeaders = ValueRange().setValues(listOf(headers.value))
                val range = "${headers.key}!1:1"
                service.spreadsheets().values().update(spreadsheetId, range, columnHeaders)
                    .setValueInputOption("RAW")
                    .execute()
            }

            return@withContext spreadsheetId!!
        }
    }

    @Throws(IOException::class)
    suspend fun appendRow(spreadsheetId: String, sheetTitle: String, values: List<String>) {
        return withContext(Dispatchers.IO) {
            val content = ValueRange().setValues(listOf(values))
            service.spreadsheets().values().append(spreadsheetId, sheetTitle, content)
                .setValueInputOption("RAW")
                .execute()

            Log.i(TAG, "Appended to $sheetTitle values: ${values.joinToString()}")
        }
    }
}