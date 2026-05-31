package com.example.ui.screens

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.database.DownloadEntity
import com.example.presentation.MediaHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MediaHubViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val downloadList by viewModel.downloadList.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val resolvedMedia by viewModel.resolvedMedia.collectAsState()
    val resolveError by viewModel.resolveError.collectAsState()

    var urlInput by remember { mutableStateOf("") }
    var showPasteSuggestion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val clipText = clipboardManager.getText()?.text ?: ""
            if (clipText.startsWith("http://") || clipText.startsWith("https://")) {
                showPasteSuggestion = true
            }
        } catch (e: Exception) {
            // Absorb clipboard denial safely when app is not in focus at startup
        }
    }

    val storageStats = remember {
        val stat = android.os.StatFs(context.filesDir.absolutePath)
        val blockSize = stat.blockSizeLong
        val availableBytes = stat.availableBlocksLong * blockSize
        val totalBytes = stat.blockCountLong * blockSize
        val usedBytes = totalBytes - availableBytes
        object {
            val total = Formatter.formatShortFileSize(context, totalBytes)
            val available = Formatter.formatShortFileSize(context, availableBytes)
            val used = Formatter.formatShortFileSize(context, usedBytes)
            val ratio = if (totalBytes > 0) usedBytes.toFloat() / totalBytes else 0.0f
        }
    }

    val completedCount = downloadList.count { it.status == "COMPLETED" }
    val downloadingCount = downloadList.count { it.status == "DOWNLOADING" }
    val failedCount = downloadList.count { it.status == "FAILED" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text(
                    text = "MEDIAHUB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DOWNLOAD ANALYTICS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.25.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Completed", value = "$completedCount", color = MaterialTheme.colorScheme.primary)
                        StatItem(label = "Downloading", value = "$downloadingCount", color = MaterialTheme.colorScheme.secondary)
                        StatItem(label = "Failed", value = "$failedCount", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Storage Used",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = storageStats.used,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "${(storageStats.ratio * 100).toInt()}% FULL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { storageStats.ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Videos & Audio Streams",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Free: ${storageStats.available} / ${storageStats.total}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "IMPORT MEDIA LINK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.25.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Paste HTTP/HTTPS Media Link") },
                        leadingIcon = { Icon(Icons.Filled.Link, contentDescription = "Link") },
                        trailingIcon = {
                            if (urlInput.isNotEmpty()) {
                                IconButton(onClick = { urlInput = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )

                    AnimatedVisibility(visible = showPasteSuggestion) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .clickable {
                                    try {
                                        val clipText = clipboardManager.getText()?.text ?: ""
                                        if (clipText.isNotBlank()) {
                                            urlInput = clipText
                                            showPasteSuggestion = false
                                        }
                                    } catch (e: Exception) {
                                        // Absorb clipboard permission/focus issues gracefully
                                    }
                                }
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.ContentPaste, contentDescription = "Paste", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Found URL on clipboard. Tap to paste.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                viewModel.resolveUrl(urlInput.trim())
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = urlInput.isNotBlank() && !isResolving
                    ) {
                        if (isResolving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Analyzing Media Link...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Outlined.Search, contentDescription = "Analyze")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze URL", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "✨ SINGLE-TAP TEST LIBRARY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.25.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Don't have a video link handy? Tap any high-speed public stream below to test downloading and playback instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val testStreams = listOf(
                        Triple("📽️ Epic Fire Flame Video (1.7MB)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", "High-Speed Small MP4 Stream"),
                        Triple("📽️ Toy Escape Film (2.3MB)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4", "Highly compressed 2.3MB movie stream"),
                        Triple("📽️ Animation Fun Clip (1.1MB)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4", "Instant download 1.1MB video test"),
                        Triple("🎵 SoundHelix Synth Beat (6.1MB)", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "Dynamic High-Quality Audio Track")
                    )

                    testStreams.forEachIndexed { index, (title, url, description) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    urlInput = url
                                    viewModel.resolveUrl(url)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (url.endsWith(".mp3")) Icons.Filled.MusicNote else Icons.Filled.PlayCircleFilled,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "Load Link",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (index < testStreams.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        if (resolveError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Link validation failed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text(text = resolveError ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        if (resolvedMedia != null) {
            item {
                val media = resolvedMedia!!
                var selectedFormat by remember { mutableStateOf(media.formats.first()) }
                var expandedFormat by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "File Resource Discovered", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            IconButton(onClick = { viewModel.clearResolvedMedia() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Dismiss", modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (media.thumbnailUrl != null) {
                                    AsyncImage(
                                        model = media.thumbnailUrl,
                                        contentDescription = "Cover preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Filled.Movie, contentDescription = "Video preview", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = media.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Estimated Duration: ${formatDuration(media.durationSeconds)}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Configure Stream Quality", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedFormat = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${selectedFormat.quality} (${selectedFormat.resolution}) - ${Formatter.formatShortFileSize(context, selectedFormat.sizeBytes)}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Quality Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            DropdownMenu(
                                expanded = expandedFormat,
                                onDismissRequest = { expandedFormat = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                media.formats.forEach { format ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "${format.quality} (${format.resolution})")
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(text = Formatter.formatShortFileSize(context, format.sizeBytes), color = MaterialTheme.colorScheme.outline)
                                            }
                                        },
                                        onClick = {
                                            selectedFormat = format
                                            expandedFormat = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.createDownload(media.title, urlInput.trim(), media.thumbnailUrl, selectedFormat)
                                viewModel.clearResolvedMedia()
                                urlInput = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = "Start Download Thread")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download (${Formatter.formatShortFileSize(context, selectedFormat.sizeBytes)})")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Tracks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { viewModel.navigateTo("downloads") }) {
                    Text("View All")
                }
            }
        }

        val recents = downloadList.take(3)
        if (recents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = "Empty downloads list", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No media files downloaded yet.", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                        Text(text = "Paste a valid link above to begin downloading.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            items(recents, key = { it.id }) { item ->
                RecentDownloadCard(item = item, viewModel = viewModel)
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun RecentDownloadCard(item: DownloadEntity, viewModel: MediaHubViewModel) {
    val progressMap by viewModel.activeProgressMap.collectAsState()
    val speedMap by viewModel.activeSpeedMap.collectAsState()
    val context = LocalContext.current

    val liveProgress = progressMap[item.id] ?: item.progress
    val liveSpeed = speedMap[item.id]

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.thumbnail != null) {
                        AsyncImage(
                            model = item.thumbnail,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val isVideo = item.title.contains("mp4", ignoreCase = true)
                        Icon(
                            imageVector = if (isVideo) Icons.Filled.Movie else Icons.Filled.MusicNote,
                            contentDescription = "Cover item placeholder",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.status} • ${Formatter.formatShortFileSize(context, item.size)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Row {
                    if (item.status == "DOWNLOADING") {
                        IconButton(onClick = { viewModel.pauseDownload(item.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Pause, contentDescription = "Pause item", modifier = Modifier.size(18.dp))
                        }
                    } else if (item.status == "PAUSED" || item.status == "FAILED") {
                        IconButton(onClick = { viewModel.resumeDownload(item.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Resume progress", modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = { viewModel.deleteDownload(item.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete download", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (item.status == "DOWNLOADING") {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { liveProgress / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Downloading: $liveProgress%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (liveSpeed != null) {
                        Text(text = liveSpeed, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
