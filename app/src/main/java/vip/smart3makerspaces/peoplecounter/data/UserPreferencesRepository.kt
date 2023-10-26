package vip.smart3makerspaces.peoplecounter.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

data class UserPreferences(
    val spreadsheetId: String
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private val TAG = "UserPreferencesRepo"

    private object PreferencesKeys {
        val SPREADSHEET_ID = stringPreferencesKey("spreadsheet_id")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading settings.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapUserPreferences(preferences)
        }

    suspend fun updateSpreadsheetId(spreadsheetId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SPREADSHEET_ID] = spreadsheetId
        }
    }

    suspend fun fetchInitialPreferences() =
        mapUserPreferences(dataStore.data.first().toPreferences())

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        val spreadsheetId = preferences[PreferencesKeys.SPREADSHEET_ID] ?: ""
        return UserPreferences(spreadsheetId)
    }
}