package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.data.model.MatchEvent
import com.polyscores.kenya.data.model.MatchEventType
import com.polyscores.kenya.data.model.MatchStatus
import com.polyscores.kenya.data.preferences.PreferencesManager
import com.polyscores.kenya.data.repository.MatchesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MatchesViewModel(
    application: Application,
    private val matchesRepository: MatchesRepository,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, MatchesRepository(), PreferencesManager(application))

    private val _latestGoalEvent = MutableSharedFlow<Pair<Match, Boolean>>() // Match and isHomeScored
    val latestGoalEvent = _latestGoalEvent.asSharedFlow()

    private var previousMatches: List<Match>? = null

    val matches: StateFlow<List<Match>> = matchesRepository.getAllMatches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            matches.collect { currentMatches ->
                checkAndTriggerGoalAlerts(currentMatches)
                previousMatches = currentMatches
            }
        }
    }

    private fun checkAndTriggerGoalAlerts(currentMatches: List<Match>) {
        if (previousMatches == null) return
        
        for (currentMatch in currentMatches) {
            if (currentMatch.matchStatus == MatchStatus.LIVE || currentMatch.matchStatus == MatchStatus.EXTRA_TIME) {
                val previousMatch = previousMatches!!.find { it.id == currentMatch.id }
                if (previousMatch != null) {
                    val homeScored = currentMatch.homeScore > previousMatch.homeScore
                    val awayScored = currentMatch.awayScore > previousMatch.awayScore
                    
                    if (homeScored || awayScored) {
                        viewModelScope.launch {
                            val prefs = preferencesManager.userPreferencesFlow.stateIn(viewModelScope).value
                            if (prefs.notificationsEnabled) {
                                _latestGoalEvent.emit(Pair(currentMatch, homeScored))
                                
                                val notificationHelper = com.polyscores.kenya.utils.NotificationHelper(getApplication())
                                val dummyEvent = MatchEvent(
                                    id = "goal_${System.currentTimeMillis()}",
                                    matchId = currentMatch.id,
                                    eventType = MatchEventType.GOAL,
                                    minute = 90,
                                    playerName = if (homeScored) currentMatch.homeTeamName else currentMatch.awayTeamName
                                )
                                notificationHelper.showEventNotification(
                                    matchHome = currentMatch.homeTeamName, 
                                    matchAway = currentMatch.awayTeamName, 
                                    event = dummyEvent,
                                    playSound = prefs.soundEnabled,
                                    vibrate = prefs.vibrationEnabled
                                )
                            }
                        }
                    }
                }
            }
        }
    }



    fun updateMatchStatus(matchId: String, newStatus: MatchStatus) {
        viewModelScope.launch {
            val currentMatch = matches.value.find { it.id == matchId }
            var setStartTime = false
            var setSecondHalfStartTime = false

            if (currentMatch != null) {
                if (newStatus == MatchStatus.LIVE && currentMatch.matchStatus == MatchStatus.SCHEDULED) {
                    setStartTime = true
                } else if (newStatus == MatchStatus.SECOND_HALF && currentMatch.matchStatus == MatchStatus.HALFTIME) {
                    setSecondHalfStartTime = true
                }
            }

            matchesRepository.updateMatchStatus(matchId, newStatus, setStartTime, setSecondHalfStartTime)

            if (currentMatch != null && (newStatus == MatchStatus.LIVE || newStatus == MatchStatus.HALFTIME || newStatus == MatchStatus.SECOND_HALF || newStatus == MatchStatus.FULLTIME)) {
                val prefs = preferencesManager.userPreferencesFlow.stateIn(viewModelScope).value
                if (prefs.notificationsEnabled) {
                    com.polyscores.kenya.utils.NotificationHelper(getApplication()).showMatchStatusNotification(
                        matchHome = currentMatch.homeTeamName,
                        matchAway = currentMatch.awayTeamName,
                        status = newStatus,
                        playSound = prefs.soundEnabled,
                        vibrate = prefs.vibrationEnabled
                    )
                }
            }
        }
    }

    fun deleteMatch(matchId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val success = matchesRepository.deleteMatch(matchId)
            if (success) onSuccess() else onError("Failed to delete match")
        }
    }

    fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int) {
        viewModelScope.launch {
            matchesRepository.updateMatchScore(matchId, homeScore, awayScore)
        }
    }

    fun updateMatchAnalytics(
        matchId: String,
        homePoss: Int,
        awayPoss: Int,
        homeShots: Int,
        awayShots: Int,
        homeShotsOnTarget: Int,
        awayShotsOnTarget: Int,
        homeCorners: Int,
        awayCorners: Int,
        homeFouls: Int,
        awayFouls: Int
    ) {
        viewModelScope.launch {
            matchesRepository.updateMatchStats(
                matchId = matchId,
                homePossession = homePoss,
                awayPossession = awayPoss,
                homeShots = homeShots,
                awayShots = awayShots,
                homeShotsOnTarget = homeShotsOnTarget,
                awayShotsOnTarget = awayShotsOnTarget,
                homeCorners = homeCorners,
                awayCorners = awayCorners,
                homeFouls = homeFouls,
                awayFouls = awayFouls
            )
        }
    }

    fun addMatchEvent(event: MatchEvent) {
        viewModelScope.launch {
            matchesRepository.addMatchEvent(event)
            val currentMatch = matches.value.find { it.id == event.matchId }
            if (currentMatch != null) {
                // Auto-increment score
                if (event.eventType == MatchEventType.GOAL || event.eventType == MatchEventType.PENALTY_GOAL || event.eventType == MatchEventType.OWN_GOAL) {
                    val isHomeTeam = event.teamId == currentMatch.homeTeamId
                    val isOwnGoal = event.eventType == MatchEventType.OWN_GOAL
                    
                    // If it's an own goal by the home team, the away team gets the point, and vice-versa
                    val scoreForHome = if (isOwnGoal) !isHomeTeam else isHomeTeam
                    val scoreForAway = if (isOwnGoal) isHomeTeam else !isHomeTeam

                    val newHomeScore = if (scoreForHome) currentMatch.homeScore + 1 else currentMatch.homeScore
                    val newAwayScore = if (scoreForAway) currentMatch.awayScore + 1 else currentMatch.awayScore
                    
                    // The team that effectively "scored" the goal (for coloring purposes) is the one that got the point
                    val scoringTeamId = if (scoreForHome) currentMatch.homeTeamId else currentMatch.awayTeamId
                    
                    matchesRepository.updateMatchScore(currentMatch.id, newHomeScore, newAwayScore, scoringTeamId)
                }

                val prefs = preferencesManager.userPreferencesFlow.stateIn(viewModelScope).value
                if (prefs.notificationsEnabled) {
                    com.polyscores.kenya.utils.NotificationHelper(getApplication()).showEventNotification(
                        matchHome = currentMatch.homeTeamName,
                        matchAway = currentMatch.awayTeamName,
                        event = event,
                        playSound = prefs.soundEnabled,
                        vibrate = prefs.vibrationEnabled
                    )
                }
            }
        }
    }

    val topScorers: StateFlow<List<Triple<String, String, Int>>> = matchesRepository.getTopScorers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateMatchLineups(
        matchId: String,
        homeStartingXI: List<String>,
        homeBench: List<String>,
        awayStartingXI: List<String>,
        awayBench: List<String>
    ) {
        viewModelScope.launch {
            matchesRepository.updateMatchLineups(
                matchId,
                homeStartingXI,
                homeBench,
                awayStartingXI,
                awayBench
            )
        }
    }

    fun createMatch(
        homeTeam: com.polyscores.kenya.data.model.Team?,
        awayTeam: com.polyscores.kenya.data.model.Team?,
        homeTeamName: String,
        awayTeamName: String,
        venue: String,
        round: String,
        timestampMillis: Long,
        refereeName: String,
        leagueId: String = "default",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val match = Match(
                leagueId = leagueId,
                homeTeamId = homeTeam?.id ?: "",
                awayTeamId = awayTeam?.id ?: "",
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                homeTeamLogo = homeTeam?.logoUrl ?: "",
                awayTeamLogo = awayTeam?.logoUrl ?: "",
                homeScore = 0,
                awayScore = 0,
                matchStatus = MatchStatus.SCHEDULED,
                scheduledTime = com.google.firebase.Timestamp(java.util.Date(timestampMillis)),
                venue = venue,
                round = round,
                refereeName = refereeName
            )

            val result = matchesRepository.createMatch(match)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun getMatchEvents(matchId: String): kotlinx.coroutines.flow.Flow<List<MatchEvent>> {
        return matchesRepository.getMatchEvents(matchId)
    }
}
