package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val url: String,
    val thumbnail: String?,
    val filePath: String?,
    val size: Long, // Size in bytes
    val status: String, // "PENDING", "DOWNLOADING", "PAUSED", "COMPLETED", "FAILED", "QUEUED"
    val progress: Int, // 0 - 100
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val url: String,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val darkMode: Boolean = true, // Default to dark mode
    val wifiOnly: Boolean = false,
    val maxDownloads: Int = 3
)
