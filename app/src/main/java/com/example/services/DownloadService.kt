package com.example.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MediaHubApp
import kotlinx.coroutines.*

class DownloadService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "MediaHub_Download_Channel"
        const val NOTIFICATION_ID = 808
        const val ACTION_START = "ACTION_START_DOWNLOADS"
        const val ACTION_STOP = "ACTION_STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, buildNotification("Downloading media assets...", 0))
                monitorDownloads()
            }
            ACTION_STOP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    stopForeground(true)
                }
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun monitorDownloads() {
        // Query the app container for DownloadEngine activity to update notifications in-service
        val appContainer = (application as? MediaHubApp)?.appContainer ?: return
        val downloadEngine = appContainer.downloadEngine
        
        scope.launch {
            downloadEngine.downloadProgressFlow.collect { progressMap ->
                if (progressMap.isEmpty()) {
                    updateNotification("Downloads completed or active items paused.", 100)
                    delay(3000)
                    if (downloadEngine.downloadProgressFlow.value.isEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            stopForeground(STOP_FOREGROUND_DETACH)
                        } else {
                            stopForeground(false)
                        }
                    }
                    return@collect
                }
                
                val totalProgress = progressMap.values.sum()
                val averageProgress = totalProgress / progressMap.size
                updateNotification("Downloading ${progressMap.size} files... ($averageProgress%)", averageProgress)
            }
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MediaHub::DownloadWakeLock").apply {
            acquire(10 * 60 * 1000L) // 10 minutes lock hold cap
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MediaHub Active Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress indicator notification for downloading media assets."
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String, progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MediaHub Live Downloader")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String, progress: Int) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(text, progress))
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
