package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polyscores.kenya.data.model.Player
import com.polyscores.kenya.data.model.PlayerPosition
import com.polyscores.kenya.data.model.Team
import com.polyscores.kenya.data.repository.TeamsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeamsViewModel(application: Application) : AndroidViewModel(application) {

    private val teamsRepository = TeamsRepository()

    val teams: StateFlow<List<Team>> = teamsRepository.getActiveTeams()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createTeam(name: String, department: String, coach: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val team = Team(
                name = name,
                department = department,
                coachName = coach,
                isActive = true
            )
            val result = teamsRepository.createTeam(team)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            teamsRepository.deleteTeam(teamId)
        }
    }

    fun getTeamPlayers(teamId: String): Flow<List<Player>> {
        return teamsRepository.getTeamPlayers(teamId)
    }

    fun createPlayer(
        teamId: String,
        name: String,
        jerseyNumber: Int,
        position: PlayerPosition,
        age: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val player = Player(
                teamId = teamId,
                name = name,
                jerseyNumber = jerseyNumber,
                position = position,
                age = age
            )
            val result = teamsRepository.createPlayer(player)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun deletePlayer(playerId: String) {
        viewModelScope.launch {
            teamsRepository.deletePlayer(playerId)
        }
    }
}
