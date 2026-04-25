package com.polyscores.kenya.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object FirestoreInstance {
    val db: FirebaseFirestore by lazy {
        val firestore = FirebaseFirestore.getInstance()

        // Configure Firestore settings
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        firestore
    }

    // Collection references
    const val COLLECTION_TEAMS = "teams"
    const val COLLECTION_PLAYERS = "players"
    const val COLLECTION_LEAGUES = "leagues"
    const val COLLECTION_MATCHES = "matches"
    const val COLLECTION_MATCH_EVENTS = "match_events"
    const val COLLECTION_STANDINGS = "standings"
    const val COLLECTION_PLAYER_STATS = "player_stats"
    const val COLLECTION_ADMINS = "admins"
    const val COLLECTION_NOTIFICATIONS = "notifications"
}
