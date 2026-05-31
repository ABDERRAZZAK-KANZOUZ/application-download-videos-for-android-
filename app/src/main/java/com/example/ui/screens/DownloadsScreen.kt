package com.example.ui.screens

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.database.DownloadEntity
import com.example.presentation.MediaHubViewModel

@Composable
fun DownloadsScreen(
    viewModel: MediaHubViewModel,
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.downloadList.collectAsState()
    val progressMap by viewModel.activeProgressMap.collectAsState()
    val speedMap by viewModel.activeSpeedMap.collectAsState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Active Queue", "Completed")

    val activeQueue = downloads.filter { it.status in listOf("PENDING", "DOWNLOADING", "PAUSED", "QUEUED") }
    val completedList = downloads.filter { it.status in listOf("COMPLETED", "FAILED") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
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
                text = "Downloads Manager",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                val count = if (index == 0) activeQueue.size else completedList.size
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text("$title ($count)", fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentList = if (selectedTabIndex == 0) activeQueue else completedList

        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedTabIndex == 0) Icons.Filled.Inbox else Icons.Filled.CheckCircle,
                        contentDescription = "Empty status icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedTabIndex == 0) "No active downloads" else "No history logs yet",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (selectedTabIndex == 0) "Your live active downloading index is empty." else "Download a link to see high-performance outcomes.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentList, key = { it.id }) { item ->
                    val liveProgress = progressMap[item.id] ?: item.progress
                    val liveSpeed = speedMap[item.id]

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
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
                                            contentDescription = "Resource thumbnail type icon",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = getStatusBadgeColor(item.status),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(
                                                text = item.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = Formatter.formatShortFileSize(context, item.size),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Row {
                                    if (item.status == "DOWNLOADING") {
                                        IconButton(onClick = { viewModel.pauseDownload(item.id) }) {
                                            Icon(Icons.Filled.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    } else if (item.status in listOf("PAUSED", "PENDING", "QUEUED", "FAILED")) {
                                        IconButton(onClick = { viewModel.resumeDownload(item.id) }) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = "Resume", tint = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteDownload(item.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete item", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            if (item.status == "DOWNLOADING") {
                                Spacer(modifier = Modifier.height(14.dp))
                                LinearProgressIndicator(
                                    progress = { liveProgress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Downloaded: $liveProgress%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (liveSpeed != null) {
                                        Text(text = liveSpeed, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (item.status == "PAUSED") {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { liveProgress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.outline,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Paused at: $liveProgress%", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getStatusBadgeColor(status: String): Color {
    return when (status) {
        "DOWNLOADING" -> MaterialTheme.colorScheme.primary
        "PAUSED" -> MaterialTheme.colorScheme.outline
        "QUEUED" -> MaterialTheme.colorScheme.secondary
        "COMPLETED" -> MaterialTheme.colorScheme.tertiary
        "FAILED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

