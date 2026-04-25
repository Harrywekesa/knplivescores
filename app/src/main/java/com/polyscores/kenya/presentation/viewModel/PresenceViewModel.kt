package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polyscores.kenya.data.preferences.PreferencesManager
import com.polyscores.kenya.data.repository.PresenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PresenceViewModel(
    application: Application,
    private val presenceRepository: PresenceRepository,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, PresenceRepository(), PreferencesManager(application))

    val activeDeviceCount: StateFlow<Int> = presenceRepository.activeDeviceCount

    init {
        viewModelScope.launch {
            preferencesManager.userPreferencesFlow.collect { prefs ->
                if (prefs.deviceId.isNotBlank()) {
                    preferencesManager.saveDeviceIdIfNeeded(prefs)
                    presenceRepository.startPresenceTracking(prefs.deviceId)
                }
            }
        }
    }
}
