package com.polyscores.kenya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.polyscores.kenya.data.model.Player
import com.polyscores.kenya.data.model.Team
import com.polyscores.kenya.data.remote.FirestoreInstance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

class TeamsRepository {

    private val db: FirebaseFirestore = FirestoreInstance.db
    private val teamsCollection = db.collection(FirestoreInstance.COLLECTION_TEAMS)
    private val playersCollection = db.collection(FirestoreInstance.COLLECTION_PLAYERS)

    /**
     * Get all teams as a Flow for real-time updates
     */
    fun getAllTeams(): Flow<List<Team>> = callbackFlow {
        val listener = teamsCollection
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TeamsRepository", "Error getting all teams", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val teams = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Team::class.java)
                } ?: emptyList()

                trySend(teams)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get active teams only
     * Note: We removed server-side orderBy("name") to avoid requiring a composite index
     * We sort locally instead to prevent FAILED_PRECONDITION errors.
     */
    fun getActiveTeams(): Flow<List<Team>> = callbackFlow {
        val listener = teamsCollection
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TeamsRepository", "Error getting active teams", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val teams = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Team::class.java)
                }?.sortedBy { it.name } ?: emptyList()

                trySend(teams)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get a single team by ID
     */
    suspend fun getTeamById(teamId: String): Team? {
        return try {
            teamsCollection.document(teamId).get().await().toObject(Team::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get players for a specific team
     */
    fun getTeamPlayers(teamId: String): Flow<List<Player>> = callbackFlow {
        val listener = playersCollection
            .whereEqualTo("teamId", teamId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val players = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Player::class.java)
                }?.sortedBy { it.jerseyNumber } ?: emptyList()

                trySend(players)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Create a new team
     */
    suspend fun createTeam(team: Team): Result<String> {
        return try {
            val docRef = teamsCollection.document()
            val newTeam = team.copy(id = docRef.id)
            docRef.set(newTeam).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing team
     */
    suspend fun updateTeam(team: Team): Result<Unit> {
        return try {
            teamsCollection.document(team.id).set(team).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a team
     */
    suspend fun deleteTeam(teamId: String): Result<Unit> {
        return try {
            // Delete all associated players first
            val players = playersCollection.whereEqualTo("teamId", teamId).get().await()
            for (player in players.documents) {
                player.reference.delete().await()
            }

            // Delete the team
            teamsCollection.document(teamId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== PLAYER OPERATIONS ====================

    /**
     * Create a new player
     */
    suspend fun createPlayer(player: Player): Result<String> {
        return try {
            val docRef = playersCollection.document()
            val newPlayer = player.copy(id = docRef.id)
            docRef.set(newPlayer).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing player
     */
    suspend fun updatePlayer(player: Player): Result<Unit> {
        return try {
            playersCollection.document(player.id).set(player).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a player
     */
    suspend fun deletePlayer(playerId: String): Result<Unit> {
        return try {
            playersCollection.document(playerId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a single player by ID
     */
    suspend fun getPlayerById(playerId: String): Player? {
        return try {
            playersCollection.document(playerId).get().await().toObject(Player::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
