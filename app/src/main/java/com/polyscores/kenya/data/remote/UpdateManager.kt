package com.polyscores.kenya.data.remote

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.polyscores.kenya.BuildConfig
import kotlinx.coroutines.tasks.await

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val releaseNotes: String,
    val downloadUrl: String
)

class UpdateManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    suspend fun checkForUpdates(): AppUpdateInfo? {
        return try {
            val document = db.collection("app_config")
                .document("version_info")
                .get()
                .await()

            if (document.exists()) {
                val latestVersionCode = document.getLong("latestVersionCode")?.toInt() ?: return null
                val latestVersionName = document.getString("latestVersionName") ?: return null
                val releaseNotes = document.getString("releaseNotes") ?: ""
                val downloadUrl = document.getString("downloadUrl") ?: return null

                AppUpdateInfo(
                    isUpdateAvailable = latestVersionCode > BuildConfig.VERSION_CODE,
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error checking for updates", e)
            null
        }
    }

    fun downloadUpdate(downloadUrl: String, versionName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Downloading Update")
                .setDescription("PolyScores App version $versionName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "PolyScoresApp-v$versionName.apk"
                )
                .setMimeType("application/vnd.android.package-archive")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error downloading update", e)
        }
    }
}
