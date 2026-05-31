package com.example.repository

import com.example.database.SettingsDao
import com.example.database.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDao: SettingsDao) {
    val settingsFlow: Flow<SettingsEntity> = settingsDao.getSettingsFlow().map {
        it ?: SettingsEntity()
    }

    suspend fun getSettings(): SettingsEntity {
        return settingsDao.getSettings() ?: SettingsEntity()
    }

    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insertSettings(settings)
    }
}
