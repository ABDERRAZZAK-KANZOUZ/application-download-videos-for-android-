package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.database.AppDatabase
import com.example.network.MediaResolver
import com.example.repository.DownloadRepository
import com.example.repository.HistoryRepository
import com.example.repository.SettingsRepository
import com.example.services.DownloadEngine
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) {

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mediahub_secure_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val downloadRepository: DownloadRepository by lazy {
        DownloadRepository(database.downloadDao())
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(database.historyDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }

    val mediaResolver: MediaResolver by lazy {
        MediaResolver(okHttpClient)
    }

    val downloadEngine: DownloadEngine by lazy {
        DownloadEngine(
            context = context,
            okHttpClient = okHttpClient,
            downloadRepository = downloadRepository,
            historyRepository = historyRepository,
            settingsRepository = settingsRepository
        )
    }
}
