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
                            val uriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            if (uriColumn != -1) {
                                val localUriStr = cursor.getString(uriColumn)
                                if (localUriStr != null) {
                                    val localUri = Uri.parse(localUriStr)
                                    val file = File(localUri.path ?: "")
                                    installApk(context, file)
                                }
                            }
                        }
                    }
                }
                cursor?.close()
            }
        }
    }

    private fun installApk(context: Context, file: File) {
        try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

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
