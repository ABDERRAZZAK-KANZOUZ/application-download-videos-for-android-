package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MediaHubApp

class DownloadRecoveryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? MediaHubApp ?: return Result.failure()
        val downloadEngine = app.appContainer.downloadEngine
        
        // Recover are restart any files pending or queued in queue
        downloadEngine.queuePendingDownloads()
        
        return Result.success()
    }
}
