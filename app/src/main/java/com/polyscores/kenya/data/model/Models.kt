package com.polyscores.kenya.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// ==================== DATA MODELS ====================

/**
 * Team model representing a football team in the polytechnic
 */
data class Team(
    @DocumentId val id: String = "",
    val name: String = "",
    val shortName: String = "",
    val logoUrl: String = "",
    val primaryColor: String = "#000000",
    val secondaryColor: String = "#FFFFFF",
    val department: String = "",
    val yearEstablished: Int = 0,
    val captainId: String = "",
    val coachName: String = "",
    val contactPhone: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true
)

/**
 * Player model representing a team member
 */
data class Player(
    @DocumentId val id: String = "",
    val teamId: String = "",
    val name: String = "",
    val jerseyNumber: Int = 0,
    val position: PlayerPosition = PlayerPosition.FORWARD,
    val age: Int = 0,
    val phoneNumber: String = "",
    val isCaptain: Boolean = false,
    val photoUrl: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

enum class PlayerPosition {
    GOALKEEPER, DEFENDER, MIDFIELDER, FORWARD
}

/**
 * League/Competition model
 */
data class League(
    @DocumentId val id: String = "",
    val name: String = "",
    val season: String = "",
    val about: String = "",
    val rules: String = "",
    val prizes: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val isActive: Boolean = true,
    val format: LeagueFormat = LeagueFormat.LEAGUE,
    val teamIds: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
)

enum class LeagueFormat {
    LEAGUE, KNOCKOUT, GROUP_AND_KNOCKOUT
}

/**
 * Match model representing a football match
 */
data class Match(
    @DocumentId val id: String = "",
    val leagueId: String = "",
    val homeTeamId: String = "",
    val awayTeamId: String = "",
    val homeTeamName: String = "",
    val awayTeamName: String = "",
    val homeTeamLogo: String = "",
    val awayTeamLogo: String = "",
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val matchStatus: MatchStatus = MatchStatus.SCHEDULED,
    val scheduledTime: Timestamp = Timestamp.now(),
    val startTime: Timestamp? = null,
    val secondHalfStartTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val venue: String = "",
    val round: String = "",
    val referee: String = "",
    val weather: String = "",
    val attendance: Int = 0,
    val homeShots: Int = 0,
    val awayShots: Int = 0,
    val homeShotsOnTarget: Int = 0,
    val awayShotsOnTarget: Int = 0,
    val homePossession: Int = 50,
    val awayPossession: Int = 50,
    val homeCorners: Int = 0,
    val awayCorners: Int = 0,
    val homeFouls: Int = 0,
    val awayFouls: Int = 0,
    val homeYellowCards: Int = 0,
    val awayYellowCards: Int = 0,
    val homeRedCards: Int = 0,
    val awayRedCards: Int = 0,
    val homeStartingXI: List<String> = emptyList(),
    val homeBench: List<String> = emptyList(),
    val awayStartingXI: List<String> = emptyList(),
    val awayBench: List<String> = emptyList(),
    val lastUpdated: Timestamp = Timestamp.now(),
    val refereeName: String = ""
)

enum class MatchStatus {
    SCHEDULED, LIVE, HALFTIME, SECOND_HALF, FULLTIME, EXTRA_TIME, PENALTY_SHOOTOUT, POSTPONED, CANCELLED, AWARDED
}

/**
 * Match Event (Goal, Card, Substitution)
 */
data class MatchEvent(
    @DocumentId val id: String = "",
    val matchId: String = "",
    val eventType: MatchEventType = MatchEventType.GOAL,
    val teamId: String = "",
    val playerId: String = "",
    val playerName: String = "",
    val assistPlayerId: String = "",
    val assistPlayerName: String = "",
    val minute: Int = 0,
    val extraTime: Int = 0,
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

enum class MatchEventType {
    GOAL, PENALTY_GOAL, OWN_GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION_IN, SUBSTITUTION_OUT,
    PENALTY_MISSED, VAR_CHECK, INJURY
}

/**
 * League Standings/Table Entry
 */
data class StandingsEntry(
    val teamId: String = "",
    val teamName: String = "",
    val teamLogo: String = "",
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val goalDifference: Int = 0,
    val points: Int = 0,
    val form: List<MatchResult> = emptyList(),
    val position: Int = 0
)

enum class MatchResult {
    WIN, DRAW, LOSS
}

/**
 * Player Statistics
 */
data class PlayerStats(
    @DocumentId val id: String = "",
    val playerId: String = "",
    val playerName: String = "",
    val teamId: String = "",
    val leagueId: String = "",
    val matchesPlayed: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val minutesPlayed: Int = 0,
    val shotsOnTarget: Int = 0,
    val totalShots: Int = 0,
    val passAccuracy: Double = 0.0,
    val rating: Double = 0.0
)

/**
 * Admin User Model
 */
data class AdminUser(
    @DocumentId val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: AdminRole = AdminRole.SCORE_UPDATER,
    val department: String = "",
    val phone: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastLogin: Timestamp? = null,
    val isActive: Boolean = true
)

enum class AdminRole {
    SUPER_ADMIN, SCORE_UPDATER, VIEWER
}

/**
 * Notification Model
 */
data class ScoreNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: NotificationType = NotificationType.GOAL,
    val matchId: String = "",
    val teamId: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

enum class NotificationType {
    GOAL, MATCH_START, HALF_TIME, FULL_TIME, CARD, MATCH_UPDATE
}
