package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polyscores.kenya.data.preferences.PreferencesManager
import com.polyscores.kenya.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.polyscores.kenya.data.repository.DatabaseAdminRepository

class SettingsViewModel(
    application: Application,
    private val databaseAdminRepository: DatabaseAdminRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, DatabaseAdminRepository())

    private val preferencesManager = PreferencesManager(application)

    private val _isWipingDatabase = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isWipingDatabase: StateFlow<Boolean> = _isWipingDatabase.asStateFlow()

    private val _wipeDatabaseResult = kotlinx.coroutines.flow.MutableStateFlow<Boolean?>(null)
    val wipeDatabaseResult: StateFlow<Boolean?> = _wipeDatabaseResult.asStateFlow()

    fun resetWipeState() {
        _wipeDatabaseResult.value = null
    }

    fun wipeDatabase() {
        viewModelScope.launch {
            _isWipingDatabase.value = true
            val success = databaseAdminRepository.wipeAllDummyData()
            _isWipingDatabase.value = false
            _wipeDatabaseResult.value = success
        }
    }

    val userPreferences: StateFlow<UserPreferences> = preferencesManager.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateNotificationsEnabled(enabled)
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSoundEnabled(enabled)
        }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateVibrationEnabled(enabled)
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateDarkMode(enabled)
        }
    }
}
