package com.polyscores.kenya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DatabaseAdminRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun wipeAllDummyData(): Boolean {
        return try {
            val collectionsToWipe = listOf(
                "leagues",
                "teams",
                "players",
                "matches",
                "match_events",
                "standings"
            )

            // We must delete documents in a batch or one by one.
            for (collectionPath in collectionsToWipe) {
                val collectionRef = db.collection(collectionPath)
                val snapshot = collectionRef.get().await()
                
                // Firestore batch allows up to 500 operations
                val batch = db.batch()
                for (document in snapshot.documents) {
                    batch.delete(document.reference)
                }
                
                if (snapshot.documents.isNotEmpty()) {
                    batch.commit().await()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
