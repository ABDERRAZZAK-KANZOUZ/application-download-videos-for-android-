package com.example.repository

import com.example.database.DownloadDao
import com.example.database.DownloadEntity
import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val downloadDao: DownloadDao) {
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    suspend fun getDownloadById(id: Long): DownloadEntity? {
        return downloadDao.getDownloadById(id)
    }

    suspend fun insertDownload(download: DownloadEntity): Long {
        return downloadDao.insertDownload(download)
    }

    suspend fun updateDownload(download: DownloadEntity) {
        downloadDao.updateDownload(download)
    }

    suspend fun deleteDownloadById(id: Long) {
        downloadDao.deleteDownloadById(id)
    }

    suspend fun updateProgress(id: Long, status: String, progress: Int) {
        downloadDao.updateProgress(id, status, progress)
    }
}
