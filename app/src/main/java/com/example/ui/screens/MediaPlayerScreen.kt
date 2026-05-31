package com.example.ui.screens

import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.presentation.MediaHubViewModel

@Composable
fun MediaPlayerScreen(
    viewModel: MediaHubViewModel,
    modifier: Modifier = Modifier
) {
    val file by viewModel.playerMedia.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableStateOf(speed) }
    var scaleExpanded by remember { mutableStateOf(false) }
    var mediaPlayerRef by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    LaunchedEffect(currentSpeed, mediaPlayerRef) {
        val mp = mediaPlayerRef
        if (mp != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                mp.playbackParams = mp.playbackParams.setSpeed(currentSpeed)
            } catch (e: Exception) {
                // Graceful speed rate bounds catcher
            }
        }
    }

    if (file == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No playable media was selected.", color = MaterialTheme.colorScheme.outline)
        }
        return
    }

    val activeFile = file!!
    val isAudio = activeFile.name.endsWith(".mp3", ignoreCase = true) || activeFile.name.endsWith(".m4a", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (isAudio) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(80.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Audiotrack,
                        contentDescription = "Audio track visual icon symbol",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(74.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = activeFile.name.substringBeforeLast("."),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Playing local sandbox audio stream",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp)),
                factory = { context ->
                    VideoView(context).apply {
                        try {
                            setVideoPath(activeFile.absolutePath)
                        } catch (e: Exception) {
                            try {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    activeFile
                                )
                                setVideoURI(uri)
                            } catch (e2: Exception) {
                                // Final fallback
                            }
                        }
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            mediaPlayerRef = mp
                            start()
                            isPlaying = true
                        }
                    }
                },
                update = { view ->
                    if (isPlaying) {
                        try {
                            view.start()
                        } catch (e: Exception) {}
                    } else {
                        try {
                            view.pause()
                        } catch (e: Exception) {}
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.closePlayer() },
                modifier = Modifier
                    .background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Close player back icon button", tint = Color.White)
            }
            Text(
                text = activeFile.name.substringBeforeLast("."),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var sliderValue by remember { mutableFloatStateOf(0.3f) }
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.DarkGray,
                    thumbColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "01:24", fontSize = 11.sp, color = Color.LightGray)
                Text(text = "04:54", fontSize = 11.sp, color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    TextButton(
                        onClick = { scaleExpanded = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.SlowMotionVideo, contentDescription = "Adjust velocity")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${currentSpeed}x", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = scaleExpanded,
                        onDismissRequest = { scaleExpanded = false }
                    ) {
                        listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { sp ->
                            DropdownMenuItem(
                                text = { Text("${sp}x") },
                                onClick = {
                                    currentSpeed = sp
                                    viewModel.updatePlaybackSpeed(sp)
                                    scaleExpanded = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Trigger play pause control tracker icon",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { /* simulated Picture in Picture action */ }) {
                    Icon(Icons.Filled.PictureInPicture, contentDescription = "Picture-in-Picture window overlay symbol", tint = Color.White)
                }
            }
        }
    }
}
