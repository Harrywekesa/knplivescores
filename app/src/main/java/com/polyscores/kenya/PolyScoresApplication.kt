package com.polyscores.kenya

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp

class PolyScoresApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Enable Offline Persistence for Firestore
        val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        com.google.firebase.firestore.FirebaseFirestore.getInstance().firestoreSettings = settings

        // Create notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Live Scores Channel
            val liveScoresChannel = NotificationChannel(
                CHANNEL_LIVE_SCORES,
                "Live Scores",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time score updates and match events"
                enableLights(true)
                enableVibration(true)
            }

            // Match Updates Channel
            val matchUpdatesChannel = NotificationChannel(
                CHANNEL_MATCH_UPDATES,
                "Match Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Match start, half-time, and full-time notifications"
                enableLights(true)
            }

            // General Channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General app notifications and announcements"
            }

            notificationManager.createNotificationChannel(liveScoresChannel)
            notificationManager.createNotificationChannel(matchUpdatesChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    companion object {
        const val CHANNEL_LIVE_SCORES = "live_scores"
        const val CHANNEL_MATCH_UPDATES = "match_updates"
        const val CHANNEL_GENERAL = "general"
    }
}
