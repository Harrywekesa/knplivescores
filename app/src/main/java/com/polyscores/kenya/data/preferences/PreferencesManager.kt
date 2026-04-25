package com.polyscores.kenya.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val deviceId: String = ""
)

class PreferencesManager(private val context: Context) {

    private val dataStore = context.dataStore

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
            val soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true
            val vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
            val darkMode = preferences[PreferencesKeys.DARK_MODE] ?: false
            
            var deviceId = preferences[PreferencesKeys.DEVICE_ID]
            if (deviceId == null) {
                deviceId = UUID.randomUUID().toString()
            }
            
            UserPreferences(
                notificationsEnabled = notificationsEnabled,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                darkMode = darkMode,
                deviceId = deviceId
            )
        }

    suspend fun saveDeviceIdIfNeeded(currentPreferences: UserPreferences) {
        val existingDeviceId = dataStore.data.map { it[PreferencesKeys.DEVICE_ID] }
        dataStore.edit { preferences ->
            if (preferences[PreferencesKeys.DEVICE_ID] == null) {
                preferences[PreferencesKeys.DEVICE_ID] = currentPreferences.deviceId
            }
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun updateDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DEVICE_ID = stringPreferencesKey("device_id")
    }
}
