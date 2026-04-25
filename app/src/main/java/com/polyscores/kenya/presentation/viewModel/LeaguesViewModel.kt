package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polyscores.kenya.data.model.League
import com.polyscores.kenya.data.repository.LeaguesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LeaguesViewModel(application: Application) : AndroidViewModel(application) {

    private val leaguesRepository = LeaguesRepository()

    val leagues: StateFlow<List<League>> = leaguesRepository.getActiveLeagues()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedLeagueId = MutableStateFlow("default")
    val selectedLeagueId: StateFlow<String> = _selectedLeagueId.asStateFlow()

    fun setLeagueId(leagueId: String) {
        _selectedLeagueId.value = leagueId
    }

    fun createLeague(
        name: String, 
        season: String, 
        about: String = "", 
        rules: String = "", 
        prizes: String = "", 
        startDate: Long = System.currentTimeMillis(),
        endDate: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
        onSuccess: () -> Unit = {}, 
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val league = League(
                name = name,
                season = season,
                about = about,
                rules = rules,
                prizes = prizes,
                startDate = com.google.firebase.Timestamp(java.util.Date(startDate)),
                endDate = com.google.firebase.Timestamp(java.util.Date(endDate)),
                isActive = true
            )
            val result = leaguesRepository.createLeague(league)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun deleteLeague(leagueId: String) {
        viewModelScope.launch {
            leaguesRepository.deleteLeague(leagueId)
        }
    }

    fun addTeamToLeague(leagueId: String, teamId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val league = leagues.value.find { it.id == leagueId }
            if (league != null) {
                if (!league.teamIds.contains(teamId)) {
                    val updatedLeague = league.copy(teamIds = league.teamIds + teamId)
                    val result = leaguesRepository.updateLeague(updatedLeague)
                    if (result.isSuccess) {
                        onSuccess()
                    } else {
                        onError(result.exceptionOrNull()?.message ?: "Unknown error")
                    }
                } else {
                    onError("Team is already in this league")
                }
            } else {
                onError("League not found")
            }
        }
    }
}
