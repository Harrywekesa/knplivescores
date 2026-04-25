package com.polyscores.kenya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.polyscores.kenya.data.model.League
import com.polyscores.kenya.data.remote.FirestoreInstance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class LeaguesRepository {

    private val db: FirebaseFirestore = FirestoreInstance.db
    private val leaguesCollection = db.collection(FirestoreInstance.COLLECTION_LEAGUES)

    /**
     * Get all leagues as a Flow for real-time updates
     */
    fun getActiveLeagues(): Flow<List<League>> = callbackFlow {
        val listener = leaguesCollection
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val leagues = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(League::class.java)
                } ?: emptyList()

                trySend(leagues)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Create a new league
     */
    suspend fun createLeague(league: League): Result<String> {
        return try {
            val docRef = leaguesCollection.document()
            val newLeague = league.copy(id = docRef.id)
            docRef.set(newLeague).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a league
     */
    suspend fun deleteLeague(leagueId: String): Result<Unit> {
        return try {
            leaguesCollection.document(leagueId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Update a league (e.g., adding teams)
     */
    suspend fun updateLeague(league: League): Result<Unit> {
        return try {
            leaguesCollection.document(league.id).set(league).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
