package vip.smart3makerspaces.peoplecounter.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vip.smart3makerspaces.peoplecounter.R
import vip.smart3makerspaces.peoplecounter.data.UserPreferencesRepository

private const val USER_PREFERENCES_NAME = "user_preferences"

val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

class StartActivity : AppCompatActivity() {

    private val TAG = "StartActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        lifecycleScope.launch {
            val userPreferences = UserPreferencesRepository(dataStore).fetchInitialPreferences()
            Log.i(TAG, "UserPreferences[SPREADSHEET_ID]: ${userPreferences.spreadsheetId}")

            if (userPreferences.spreadsheetId.isEmpty()) {
                Log.i(TAG, "UserPreferences[SPREADSHEET_ID] is empty, starting LoginActivity")
                val loginIntent = Intent(this@StartActivity, LoginActivity::class.java)
                this@StartActivity.startActivity(loginIntent)
            } else {
                Log.i(TAG, "UserPreferences[SPREADSHEET_ID] is not empty, starting RecognitionActivity")
                val openCameraIntent = Intent(this@StartActivity, RecognitionActivity::class.java)
                this@StartActivity.startActivity(openCameraIntent)
            }
        }
    }
}