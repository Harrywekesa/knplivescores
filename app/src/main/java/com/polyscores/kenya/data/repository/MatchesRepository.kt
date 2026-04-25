package com.polyscores.kenya.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.data.model.MatchEvent
import com.polyscores.kenya.data.model.MatchEventType
import com.polyscores.kenya.data.model.MatchStatus
import com.polyscores.kenya.data.remote.FirestoreInstance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MatchesRepository {

    private val db: FirebaseFirestore = FirestoreInstance.db
    private val matchesCollection = db.collection(FirestoreInstance.COLLECTION_MATCHES)
    private val eventsCollection = db.collection(FirestoreInstance.COLLECTION_MATCH_EVENTS)

    /**
     * Get all matches as a Flow for real-time updates
     */
    fun getAllMatches(): Flow<List<Match>> = callbackFlow {
        val listener = matchesCollection
            .orderBy("scheduledTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("MatchesRepository", "Error getAllMatches", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val matches = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Match::class.java)
                } ?: emptyList()

                trySend(matches)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get live matches only
     */
    fun getLiveMatches(): Flow<List<Match>> = callbackFlow {
        val listener = matchesCollection
            .whereEqualTo("matchStatus", MatchStatus.LIVE.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("MatchesRepository", "Error getLiveMatches", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val matches = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Match::class.java)
                } ?: emptyList()

                trySend(matches)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get matches by league
     */
    fun getMatchesByLeague(leagueId: String): Flow<List<Match>> = callbackFlow {
        val listener = matchesCollection
            .whereEqualTo("leagueId", leagueId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("MatchesRepository", "Error getMatchesByLeague", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val matches = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Match::class.java)
                }?.sortedByDescending { it.scheduledTime } ?: emptyList()

                trySend(matches)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get a single match by ID
     */
    suspend fun getMatchById(matchId: String): Match? {
        return try {
            matchesCollection.document(matchId).get().await().toObject(Match::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get match events for a specific match
     */
    fun getMatchEvents(matchId: String): Flow<List<MatchEvent>> = callbackFlow {
        val listener = eventsCollection
            .whereEqualTo("matchId", matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("MatchesRepository", "Error getMatchEvents", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MatchEvent::class.java)
                }?.sortedBy { it.minute } ?: emptyList()

                trySend(events)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Create a new match
     */
    suspend fun createMatch(match: Match): Result<String> {
        return try {
            val docRef = matchesCollection.document()
            val newMatch = match.copy(id = docRef.id)
            docRef.set(newMatch).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update match score
     */
    suspend fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int): Result<Unit> {
        return try {
            matchesCollection.document(matchId)
                .update(
                    mapOf(
                        "homeScore" to homeScore,
                        "awayScore" to awayScore,
                        "lastUpdated" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update match status
     */
    suspend fun updateMatchStatus(
        matchId: String, 
        status: MatchStatus,
        setStartTime: Boolean = false,
        setSecondHalfStartTime: Boolean = false
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "matchStatus" to status.name,
                "lastUpdated" to Timestamp.now()
            )

            if (status == MatchStatus.FULLTIME) {
                updates["endTime"] = Timestamp.now()
            }
            if (setStartTime) {
                updates["startTime"] = Timestamp.now()
            }
            if (setSecondHalfStartTime) {
                updates["secondHalfStartTime"] = Timestamp.now()
            }

            matchesCollection.document(matchId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update match lineups
     */
    suspend fun updateMatchLineups(
        matchId: String,
        homeStartingXI: List<String>,
        homeBench: List<String>,
        awayStartingXI: List<String>,
        awayBench: List<String>
    ): Result<Unit> {
        return try {
            matchesCollection.document(matchId)
                .update(
                    mapOf(
                        "homeStartingXI" to homeStartingXI,
                        "homeBench" to homeBench,
                        "awayStartingXI" to awayStartingXI,
                        "awayBench" to awayBench,
                        "lastUpdated" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add a match event (goal, card, etc.)
     */
    suspend fun addMatchEvent(event: MatchEvent): Result<String> {
        return try {
            val docRef = eventsCollection.document()
            val newEvent = event.copy(id = docRef.id)
            docRef.set(newEvent).await()

            // Update match lastUpdated timestamp
            matchesCollection.document(event.matchId)
                .update("lastUpdated", Timestamp.now())
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a match
     */
    suspend fun deleteMatch(matchId: String): Boolean {
        return try {
            // Delete all associated events first
            val events = eventsCollection.whereEqualTo("matchId", matchId).get().await()
            for (event in events.documents) {
                event.reference.delete().await()
            }

            // Delete the match
            matchesCollection.document(matchId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get top scorers
     */
    fun getTopScorers(): Flow<List<Triple<String, String, Int>>> = callbackFlow {
        val listener = eventsCollection
            .whereIn("eventType", listOf(MatchEventType.GOAL.name, MatchEventType.PENALTY_GOAL.name))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("MatchesRepository", "Error getTopScorers", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MatchEvent::class.java)
                } ?: emptyList()

                // Group by playerName and teamId
                val topScorers = events
                    .groupBy { Pair(it.playerName, it.teamId) }
                    .map { Triple(it.key.first, it.key.second, it.value.size) }
                    .sortedByDescending { it.third }
                    .take(10) // Top 10

                trySend(topScorers)
            }

        awaitClose { listener.remove() }
    }
}
