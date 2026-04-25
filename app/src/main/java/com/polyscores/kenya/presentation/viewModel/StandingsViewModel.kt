package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polyscores.kenya.data.model.StandingsEntry
import com.polyscores.kenya.data.repository.StandingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StandingsViewModel(application: Application) : AndroidViewModel(application) {

    private val standingsRepository = StandingsRepository()

    private val _standings = MutableStateFlow<List<StandingsEntry>>(emptyList())
    val standings: StateFlow<List<StandingsEntry>> = _standings.asStateFlow()

    fun loadStandings(leagueId: String, matches: List<com.polyscores.kenya.data.model.Match>, teams: List<com.polyscores.kenya.data.model.Team>) {
        viewModelScope.launch {
            val currentStandings = standingsRepository.getStandings(leagueId)
            if (currentStandings.isNotEmpty()) {
                _standings.value = currentStandings
            } else {
                // Auto-calculate on the fly for the public view
                val rawStandings = standingsRepository.calculateStandings(leagueId, matches, teams)
                val sortedRawStandings = rawStandings.sortedWith(compareByDescending<StandingsEntry> { it.points }
                    .thenByDescending { it.goalDifference }
                    .thenByDescending { it.goalsFor })
                    .mapIndexed { index, entry -> entry.copy(position = index + 1) }
                _standings.value = sortedRawStandings
            }
        }
    }

    fun updateStandings(leagueId: String, newStandings: List<StandingsEntry>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val sortedStandings = newStandings.sortedWith(compareByDescending<StandingsEntry> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor })
                .mapIndexed { index, entry -> entry.copy(position = index + 1) }
            
            standingsRepository.saveStandings(leagueId, sortedStandings)
            
            _standings.value = sortedStandings
            onSuccess()
        }
    }
    fun autoCalculateStandings(leagueId: String, matches: List<com.polyscores.kenya.data.model.Match>, teams: List<com.polyscores.kenya.data.model.Team>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            // First calculate raw from matches and league teams
            val rawStandings = standingsRepository.calculateStandings(leagueId, matches, teams)
            
            // Map the team details (name and logo) properly since calculateStandings might not have them
            val detailedStandings = rawStandings.map { entry ->
                val team = teams.find { it.id == entry.teamId }
                if (team != null) {
                    entry.copy(teamName = team.name, teamLogo = team.logoUrl)
                } else {
                    entry
                }
            }

            // We update locally, and then we let the user click 'Save Standings' to persist it, or we can save it right now
            val sortedStandings = detailedStandings.sortedWith(compareByDescending<StandingsEntry> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor })
                .mapIndexed { index, entry -> entry.copy(position = index + 1) }

            standingsRepository.saveStandings(leagueId, sortedStandings)
            _standings.value = sortedStandings
            onSuccess()
        }
    }
}
