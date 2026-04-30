package com.polyscores.kenya.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.polyscores.kenya.R
import com.polyscores.kenya.data.model.MatchEvent
import com.polyscores.kenya.data.model.MatchEventType

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val goalChannelId = "polyscores_goals_v2"
    private val cardChannelId = "polyscores_cards_v2"
    private val defaultChannelId = "polyscores_events_v2"

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Goal Channel
            val goalChannel = NotificationChannel(
                goalChannelId, 
                "Live Match Goals", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for goals with custom horn sound"
                val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.goal_horn}")
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }

            // Cards/Whistle Channel
            val cardChannel = NotificationChannel(
                cardChannelId, 
                "Live Match Cards", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for cards and whistles"
                val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.whistle}")
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }

            // Default Channel
            val defaultChannel = NotificationChannel(
                defaultChannelId, 
                "Live Match Events", 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General match event notifications"
            }

            notificationManager.createNotificationChannels(listOf(goalChannel, cardChannel, defaultChannel))
        }
    }

    fun showEventNotification(
        matchHome: String, 
        matchAway: String, 
        event: MatchEvent,
        playSound: Boolean = true,
        vibrate: Boolean = true
    ) {
        val isGoal = event.eventType == MatchEventType.GOAL || event.eventType == MatchEventType.PENALTY_GOAL || event.eventType == MatchEventType.OWN_GOAL
        val isCard = event.eventType == MatchEventType.RED_CARD || event.eventType == MatchEventType.YELLOW_CARD

        val targetChannelId = when {
            isGoal -> goalChannelId
            isCard -> cardChannelId
            else -> defaultChannelId
        }

        val title = when (event.eventType) {
            MatchEventType.GOAL, MatchEventType.PENALTY_GOAL -> "GOAL! \u26BD"
            MatchEventType.OWN_GOAL -> "OWN GOAL! \u26BD"
            MatchEventType.RED_CARD -> "RED CARD! \uD83D\uDFE5"
            MatchEventType.YELLOW_CARD -> "YELLOW CARD \uD83D\uDFE8"
            else -> "Match Event"
        }

        val text = "${event.minute}' - ${event.playerName} (${event.eventType.name.replace("_", " ")})"

        val soundUri = when {
            isGoal -> android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.goal_horn}")
            isCard -> android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.whistle}")
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val builder = NotificationCompat.Builder(context, targetChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$title - $matchHome vs $matchAway")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (playSound) {
            builder.setSound(soundUri)
        } else {
            builder.setSound(null)
        }

        if (vibrate) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        notificationManager.notify(event.id.hashCode(), builder.build())
    }

    fun showMatchStatusNotification(
        matchHome: String,
        matchAway: String,
        status: com.polyscores.kenya.data.model.MatchStatus,
        playSound: Boolean = true,
        vibrate: Boolean = true
    ) {
        val title = when (status) {
            com.polyscores.kenya.data.model.MatchStatus.LIVE -> "Match Started! \u26BD"
            com.polyscores.kenya.data.model.MatchStatus.HALFTIME -> "Half Time \u23F2"
            com.polyscores.kenya.data.model.MatchStatus.SECOND_HALF -> "Second Half Started \u26BD"
            com.polyscores.kenya.data.model.MatchStatus.FULLTIME -> "Full Time \uD83C\uDFC1"
            else -> "Match Update"
        }

        val text = "Status: ${status.name.replace("_", " ")}"

        val builder = NotificationCompat.Builder(context, defaultChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$title - $matchHome vs $matchAway")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (playSound) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        if (vibrate) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500))
        }

        notificationManager.notify((matchHome + status.name).hashCode(), builder.build())
    }

    // FCM Notification Handlers
    fun showLineupNotification(title: String, body: String, data: Map<String, String>? = null) {
        val builder = NotificationCompat.Builder(context, defaultChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun showGoalNotification(title: String, body: String, data: Map<String, String>) {
        val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.goal_horn}")
        val builder = NotificationCompat.Builder(context, goalChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(null, true) // Wakes up the device
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun showMatchNotification(title: String, body: String, data: Map<String, String>) {
        val builder = NotificationCompat.Builder(context, defaultChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(null, true) // Wakes up the device
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 500, 200, 500))

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun showGeneralNotification(title: String, body: String) {
        val builder = NotificationCompat.Builder(context, defaultChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 500, 200, 500))

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
