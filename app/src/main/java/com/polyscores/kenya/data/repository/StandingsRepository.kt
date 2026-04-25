package com.polyscores.kenya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.data.model.MatchResult
import com.polyscores.kenya.data.model.MatchStatus
import com.polyscores.kenya.data.model.StandingsEntry
import com.polyscores.kenya.data.remote.FirestoreInstance
import kotlinx.coroutines.tasks.await

class StandingsRepository {

    private val db: FirebaseFirestore = FirestoreInstance.db
    private val standingsCollection = db.collection(FirestoreInstance.COLLECTION_STANDINGS)

    /**
     * Calculate and generate standings from match results
     */
    suspend fun calculateStandings(leagueId: String, matches: List<Match>, leagueTeams: List<com.polyscores.kenya.data.model.Team>): List<StandingsEntry> {
        val teamStats = mutableMapOf<String, MutableStandingsData>()

        // Initialize all teams in the league so they appear even with 0 points
        leagueTeams.forEach { team ->
            teamStats[team.id] = MutableStandingsData(
                teamId = team.id,
                teamName = team.name,
                teamLogo = team.logoUrl
            )
        }

        // Get all completed matches for the league
        val completedMatches = matches.filter {
            it.leagueId == leagueId &&
            it.matchStatus == MatchStatus.FULLTIME
        }

        // Process each match to calculate stats
        completedMatches.forEach { match ->
            // In case a match has teams not in leagueTeams (should not happen, but safe fallback)
            if (!teamStats.containsKey(match.homeTeamId)) {
                teamStats[match.homeTeamId] = MutableStandingsData(
                    teamId = match.homeTeamId,
                    teamName = match.homeTeamName,
                    teamLogo = match.homeTeamLogo
                )
            }
            if (!teamStats.containsKey(match.awayTeamId)) {
                teamStats[match.awayTeamId] = MutableStandingsData(
                    teamId = match.awayTeamId,
                    teamName = match.awayTeamName,
                    teamLogo = match.awayTeamLogo
                )
            }

            // Update home team stats
            val homeStats = teamStats[match.homeTeamId]!!
            homeStats.played++
            homeStats.goalsFor += match.homeScore
            homeStats.goalsAgainst += match.awayScore

            // Update away team stats
            val awayStats = teamStats[match.awayTeamId]!!
            awayStats.played++
            awayStats.goalsFor += match.awayScore
            awayStats.goalsAgainst += match.homeScore

            // Determine result
            when {
                match.homeScore > match.awayScore -> {
                    homeStats.won++
                    homeStats.points += 3
                    homeStats.form = (homeStats.form + MatchResult.WIN).takeLast(5)
                    awayStats.lost++
                    awayStats.form = (awayStats.form + MatchResult.LOSS).takeLast(5)
                }
                match.homeScore < match.awayScore -> {
                    awayStats.won++
                    awayStats.points += 3
                    awayStats.form = (awayStats.form + MatchResult.WIN).takeLast(5)
                    homeStats.lost++
                    homeStats.form = (homeStats.form + MatchResult.LOSS).takeLast(5)
                }
                else -> {
                    homeStats.drawn++
                    homeStats.points += 1
                    homeStats.form = (homeStats.form + MatchResult.DRAW).takeLast(5)
                    awayStats.drawn++
                    awayStats.points += 1
                    awayStats.form = (awayStats.form + MatchResult.DRAW).takeLast(5)
                }
            }
        }

        // Convert to StandingsEntry and sort
        val standings = teamStats.values.map { data ->
            StandingsEntry(
                teamId = data.teamId,
                teamName = data.teamName,
                teamLogo = data.teamLogo,
                played = data.played,
                won = data.won,
                drawn = data.drawn,
                lost = data.lost,
                goalsFor = data.goalsFor,
                goalsAgainst = data.goalsAgainst,
                goalDifference = data.goalsFor - data.goalsAgainst,
                points = data.points,
                form = data.form,
                position = 0 // Will be set after sorting
            )
        }.sortedWith(compareByDescending<StandingsEntry> { it.points }
            .thenByDescending { it.goalDifference }
            .thenByDescending { it.goalsFor })
            .mapIndexed { index, entry ->
                entry.copy(position = index + 1)
            }

        // Save to Firestore
        saveStandings(leagueId, standings)

        return standings
    }

    /**
     * Save standings to Firestore
     */
    suspend fun saveStandings(leagueId: String, standings: List<StandingsEntry>) {
        try {
            val batch = db.batch()
            val leagueStandingsRef = standingsCollection.document(leagueId)

            val data = mapOf(
                "leagueId" to leagueId,
                "standings" to standings,
                "lastUpdated" to com.google.firebase.Timestamp.now()
            )

            batch.set(leagueStandingsRef, data)
            batch.commit().await()
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }

    /**
     * Get standings for a league
     */
    suspend fun getStandings(leagueId: String): List<StandingsEntry> {
        return try {
            val doc = standingsCollection.document(leagueId).get().await()
            val data = doc.data ?: return emptyList()

            @Suppress("UNCHECKED_CAST")
            val standingsList = data["standings"] as? List<Map<String, Any>> ?: return emptyList()

            standingsList.map { map ->
                StandingsEntry(
                    teamId = map["teamId"] as? String ?: "",
                    teamName = map["teamName"] as? String ?: "",
                    teamLogo = map["teamLogo"] as? String ?: "",
                    played = (map["played"] as? Long)?.toInt() ?: 0,
                    won = (map["won"] as? Long)?.toInt() ?: 0,
                    drawn = (map["drawn"] as? Long)?.toInt() ?: 0,
                    lost = (map["lost"] as? Long)?.toInt() ?: 0,
                    goalsFor = (map["goalsFor"] as? Long)?.toInt() ?: 0,
                    goalsAgainst = (map["goalsAgainst"] as? Long)?.toInt() ?: 0,
                    goalDifference = (map["goalDifference"] as? Long)?.toInt() ?: 0,
                    points = (map["points"] as? Long)?.toInt() ?: 0,
                    form = (map["form"] as? List<String>)?.map { MatchResult.valueOf(it) } ?: emptyList(),
                    position = (map["position"] as? Long)?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Mutable data class for calculating standings
 */
private data class MutableStandingsData(
    val teamId: String,
    val teamName: String,
    val teamLogo: String,
    var played: Int = 0,
    var won: Int = 0,
    var drawn: Int = 0,
    var lost: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0,
    var points: Int = 0,
    var form: List<MatchResult> = emptyList()
)
