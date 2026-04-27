package com.polyscores.kenya.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId != -1L) {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                
                if (cursor != null && cursor.moveToFirst()) {
                    val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (statusColumn != -1) {
                        val status = cursor.getInt(statusColumn)
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            // Get the content:// URI directly from DownloadManager
                            val apkUri = downloadManager.getUriForDownloadedFile(downloadId)
                            if (apkUri != null) {
                                installApk(context, apkUri)
                            } else {
                                Log.e("UpdateReceiver", "Downloaded file URI is null")
                            }
                        }
                    }
                }
                cursor?.close()
            }
        }
    }

    private fun installApk(context: Context, apkUri: Uri) {
        try {
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e("UpdateReceiver", "Error installing APK", e)
        }
    }
}
