package com.polyscores.kenya.data.remote

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let { notification ->
            val title = notification.title ?: "PolyScores"
            val body = notification.body ?: ""
            val type = message.data["type"] ?: "general"

            Log.d("FCM", "Notification received: $title - $body")

            // Handle the notification based on type
            when (type) {
                "goal" -> handleGoalNotification(title, body, message.data)
                "match_start" -> handleMatchStartNotification(title, body, message.data)
                "full_time" -> handleFullTimeNotification(title, body, message.data)
                else -> handleGeneralNotification(title, body, message.data)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM token: $token")
        // Send token to server if needed
    }

    private fun handleGoalNotification(title: String, body: String, data: Map<String, String>) {
        // Display goal notification with match details
        com.polyscores.kenya.utils.NotificationHelper(this).showGoalNotification(title, body, data)
    }

    private fun handleMatchStartNotification(title: String, body: String, data: Map<String, String>) {
        com.polyscores.kenya.utils.NotificationHelper(this).showMatchNotification(title, body, data)
    }

    private fun handleFullTimeNotification(title: String, body: String, data: Map<String, String>) {
        com.polyscores.kenya.utils.NotificationHelper(this).showMatchNotification(title, body, data)
    }

    private fun handleGeneralNotification(title: String, body: String, data: Map<String, String>) {
        com.polyscores.kenya.utils.NotificationHelper(this).showGeneralNotification(title, body)
    }
}
