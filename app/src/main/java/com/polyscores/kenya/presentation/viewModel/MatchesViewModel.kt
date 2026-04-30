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
            // First check for 2nd Yellow Card logic if it's a Yellow Card
            var actualEvent = event
            if (event.eventType == MatchEventType.YELLOW_CARD) {
                val previousEvents = matchesRepository.getMatchEventsOnce(event.matchId)
                val yellowCount = previousEvents.count { it.playerId == event.playerId && it.eventType == MatchEventType.YELLOW_CARD }
                if (yellowCount >= 1) {
                    // This is their second yellow, convert to Red Card
                    actualEvent = event.copy(
                        eventType = MatchEventType.RED_CARD,
                        description = "${event.description} (Second Yellow)"
                    )
                }
            }

            matchesRepository.addMatchEvent(actualEvent)
            val currentMatch = matches.value.find { it.id == actualEvent.matchId }
            if (currentMatch != null) {
                // Auto-increment score
                if (actualEvent.eventType == MatchEventType.GOAL || actualEvent.eventType == MatchEventType.PENALTY_GOAL || actualEvent.eventType == MatchEventType.OWN_GOAL) {
                    val isHomeTeam = actualEvent.teamId == currentMatch.homeTeamId
                    val isOwnGoal = actualEvent.eventType == MatchEventType.OWN_GOAL
                    
                    // If it's an own goal by the home team, the away team gets the point, and vice-versa
                    val scoreForHome = if (isOwnGoal) !isHomeTeam else isHomeTeam
                    val scoreForAway = if (isOwnGoal) isHomeTeam else !isHomeTeam

                    val newHomeScore = if (scoreForHome) currentMatch.homeScore + 1 else currentMatch.homeScore
                    val newAwayScore = if (scoreForAway) currentMatch.awayScore + 1 else currentMatch.awayScore
                    
                    // The team that effectively "scored" the goal (for coloring purposes) is the one that got the point
                    val scoringTeamId = if (scoreForHome) currentMatch.homeTeamId else currentMatch.awayTeamId
                    
                    matchesRepository.updateMatchScore(currentMatch.id, newHomeScore, newAwayScore, scoringTeamId)
                }

                // Handle Substitutions and Red Cards (Remove/Add to StartingXI)
                if (actualEvent.eventType == MatchEventType.RED_CARD || 
                    actualEvent.eventType == MatchEventType.SUBSTITUTION_OUT || 
                    actualEvent.eventType == MatchEventType.SUBSTITUTION_IN) {
                    
                    val isHomeTeam = actualEvent.teamId == currentMatch.homeTeamId
                    val hStart = currentMatch.homeStartingXI.toMutableList()
                    val hBench = currentMatch.homeBench.toMutableList()
                    val aStart = currentMatch.awayStartingXI.toMutableList()
                    val aBench = currentMatch.awayBench.toMutableList()

                    if (actualEvent.eventType == MatchEventType.RED_CARD || actualEvent.eventType == MatchEventType.SUBSTITUTION_OUT) {
                        if (isHomeTeam) hStart.remove(actualEvent.playerId) else aStart.remove(actualEvent.playerId)
                    } else if (actualEvent.eventType == MatchEventType.SUBSTITUTION_IN) {
                        if (isHomeTeam) {
                            hBench.remove(actualEvent.playerId)
                            if (!hStart.contains(actualEvent.playerId)) hStart.add(actualEvent.playerId)
                        } else {
                            aBench.remove(actualEvent.playerId)
                            if (!aStart.contains(actualEvent.playerId)) aStart.add(actualEvent.playerId)
                        }
                    }

                    matchesRepository.updateMatchLineups(currentMatch.id, hStart, hBench, aStart, aBench)
                }

                val prefs = preferencesManager.userPreferencesFlow.stateIn(viewModelScope).value
                if (prefs.notificationsEnabled) {
                    com.polyscores.kenya.utils.NotificationHelper(getApplication()).showEventNotification(
                        matchHome = currentMatch.homeTeamName,
                        matchAway = currentMatch.awayTeamName,
                        event = actualEvent,
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
            val currentMatch = matches.value.find { it.id == matchId }
            if (currentMatch != null) {
                val prefs = preferencesManager.userPreferencesFlow.stateIn(viewModelScope).value
                if (prefs.notificationsEnabled) {
                    com.polyscores.kenya.utils.NotificationHelper(getApplication()).showLineupNotification(
                        title = "Lineups Released! 📋",
                        body = "${currentMatch.homeTeamName} vs ${currentMatch.awayTeamName} starting XI is out now!"
                    )
                }
            }
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
