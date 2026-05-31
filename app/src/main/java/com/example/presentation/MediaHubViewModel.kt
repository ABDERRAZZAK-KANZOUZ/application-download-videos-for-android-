package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.MediaHubApp
import com.example.database.DownloadEntity
import com.example.database.HistoryEntity
import com.example.database.SettingsEntity
import com.example.network.MediaDetails
import com.example.network.MediaFormatOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class MediaHubViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as MediaHubApp
    private val container = app.appContainer
    private val downloadRepo = container.downloadRepository
    private val historyRepo = container.historyRepository
    private val settingsRepo = container.settingsRepository
    private val resolver = container.mediaResolver
    val downloadEngine = container.downloadEngine

    // --- Screen Navigation Drawer/Stack ---
    private val _currentScreen = MutableStateFlow("splash")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- Onboarding Flow state ---
    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    init {
        // Trigger Splash Screen Timer to Home
        viewModelScope.launch {
            delay(1800)
            val settings = settingsRepo.getSettings()
            // If settings exist, user is already onboarded. Let's check a basic value.
            _isOnboarded.value = settings.maxDownloads > 0
            if (_isOnboarded.value) {
                _currentScreen.value = "home"
            } else {
                _currentScreen.value = "onboarding"
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            // Save starting settings
            settingsRepo.saveSettings(SettingsEntity(id = 1, darkMode = true, wifiOnly = false, maxDownloads = 3))
            _isOnboarded.value = true
            _currentScreen.value = "home"
        }
    }

    // --- Settings Panel state ---
    val settingsState: StateFlow<SettingsEntity> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())

    fun updateSettings(settings: SettingsEntity) {
        viewModelScope.launch {
            settingsRepo.saveSettings(settings)
        }
    }

    // --- URL Resolver states ---
    private val _resolvedMedia = MutableStateFlow<MediaDetails?>(null)
    val resolvedMedia: StateFlow<MediaDetails?> = _resolvedMedia.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    private val _resolveError = MutableStateFlow<String?>(null)
    val resolveError: StateFlow<String?> = _resolveError.asStateFlow()

    fun clearResolvedMedia() {
        _resolvedMedia.value = null
        _resolveError.value = null
    }

    fun resolveUrl(url: String) {
        viewModelScope.launch {
            _isResolving.value = true
            _resolveError.value = null
            _resolvedMedia.value = null

            resolver.resolveMedia(url)
                .onSuccess { details ->
                    _resolvedMedia.value = details
                    _isResolving.value = false
                }
                .onFailure { error ->
                    _resolveError.value = error.localizedMessage ?: "Unknown URL analysis error"
                    _isResolving.value = false
                }
        }
    }

    // --- Base Downloads states ---
    val downloadList: StateFlow<List<DownloadEntity>> = downloadRepo.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProgressMap: StateFlow<Map<Long, Int>> = downloadEngine.downloadProgressFlow
    val activeSpeedMap: StateFlow<Map<Long, String>> = downloadEngine.activeSpeeds

    fun createDownload(title: String, url: String, thumbnail: String?, selectedFormat: MediaFormatOption) {
        viewModelScope.launch {
            val extension = selectedFormat.ext
            val displayTitle = if (title.endsWith(".$extension", true)) title else "$title ($extension)"
            
            val entity = DownloadEntity(
                title = displayTitle,
                url = url,
                thumbnail = thumbnail,
                filePath = null,
                size = selectedFormat.sizeBytes,
                status = "PENDING",
                progress = 0
            )
            val downloadId = downloadRepo.insertDownload(entity)
            
            // Invoke the downloader engine job immediately
            downloadEngine.startDownload(downloadId)
            
            // Navigate directly to download lists index screen
            _currentScreen.value = "downloads"
        }
    }

    fun pauseDownload(id: Long) {
        downloadEngine.pauseDownload(id)
    }

    fun resumeDownload(id: Long) {
        downloadEngine.startDownload(id)
    }

    fun cancelDownload(id: Long) {
        downloadEngine.cancelDownload(id)
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch {
            val download = downloadRepo.getDownloadById(id)
            if (download != null) {
                download.filePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                downloadRepo.deleteDownloadById(id)
                refreshFileList()
            }
        }
    }

    // --- Completion Logs & Search history configurations ---
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    val rawHistoryList: StateFlow<List<HistoryEntity>> = historyRepo.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredHistoryList: StateFlow<List<HistoryEntity>> = combine(
        rawHistoryList,
        _historySearchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { it.title.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyRepo.deleteHistoryById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepo.clearHistory()
        }
    }

    // --- On-device Private File Indexer lists ---
    private val _localFiles = MutableStateFlow<List<File>>(emptyList())
    val localFiles: StateFlow<List<File>> = _localFiles.asStateFlow()

    fun refreshFileList() {
        val dir = File(app.filesDir, "downloads")
        if (dir.exists() && dir.isDirectory) {
            val list = dir.listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() }
            _localFiles.value = list ?: emptyList()
        } else {
            _localFiles.value = emptyList()
        }
    }

    fun renameFile(originalFile: File, newName: String): Boolean {
        if (newName.isBlank()) return false
        val extension = originalFile.extension
        val cleanName = if (newName.endsWith(".$extension", ignoreCase = true)) newName else "$newName.$extension"
        val target = File(originalFile.parentFile, cleanName)
        if (target.exists()) return false

        val success = originalFile.renameTo(target)
        if (success) {
            refreshFileList()
            viewModelScope.launch {
                val all = downloadRepo.allDownloads.first()
                val record = all.find { it.filePath == originalFile.absolutePath }
                if (record != null) {
                    downloadRepo.updateDownload(record.copy(filePath = target.absolutePath, title = newName.substringBeforeLast(".")))
                }
            }
        }
        return success
    }

    fun deleteFile(file: File): Boolean {
        val path = file.absolutePath
        val success = file.delete()
        if (success) {
            refreshFileList()
            viewModelScope.launch {
                val all = downloadRepo.allDownloads.first()
                val record = all.find { it.filePath == path }
                if (record != null) {
                    downloadRepo.deleteDownloadById(record.id)
                }
            }
        }
        return success
    }

    // --- Media Player Controller properties ---
    private val _playerMedia = MutableStateFlow<File?>(null)
    val playerMedia: StateFlow<File?> = _playerMedia.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun selectPlayerMedia(file: File) {
        _playerMedia.value = file
        _currentScreen.value = "player"
    }

    fun updatePlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun closePlayer() {
        _playerMedia.value = null
        _currentScreen.value = "file_manager"
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaHubViewModel(application) as T
            }
        }
    }
}
