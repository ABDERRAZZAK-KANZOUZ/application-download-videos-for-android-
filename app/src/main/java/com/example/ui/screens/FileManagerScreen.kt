package com.example.ui.screens

import android.content.Intent
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.MediaHubViewModel
import java.io.File

@Composable
fun FileManagerScreen(
    viewModel: MediaHubViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val files by viewModel.localFiles.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshFileList()
    }

    var selectedFileForRename by remember { mutableStateOf<File?>(null) }
    var renameInputName by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "File Browser",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.refreshFileList() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh file list icon")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = "Empty file directory card symbol",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No downloads found",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Once media streams complete, they render in this folder.",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(files, key = { it.absolutePath }) { file ->
                    val fileLengthLabel = remember(file.length()) {
                        Formatter.formatShortFileSize(context, file.length())
                    }
                    val isVideo = file.name.endsWith(".mp4", ignoreCase = true) || file.name.endsWith(".mkv", ignoreCase = true)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    viewModel.selectPlayerMedia(file)
                                }
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isVideo) Icons.Filled.PlayCircle else Icons.Filled.Audiotrack,
                                    contentDescription = "Media category icon identifier",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name.substringBeforeLast("."),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${file.extension.uppercase()} • $fileLengthLabel",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Row {
                                IconButton(
                                    onClick = {
                                        selectedFileForRename = file
                                        renameInputName = file.name.substringBeforeLast(".")
                                        showRenameDialog = true
                                    }
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Rename file details icon", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_SUBJECT, "Shared Media asset from MediaHub")
                                            putExtra(Intent.EXTRA_TEXT, "Completed media downloaded via MediaHub: ${file.name.substringBeforeLast(".")}\nSize: $fileLengthLabel\nFile Type: ${file.extension.uppercase()}")
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Share Media Info")
                                        context.startActivity(shareIntent)
                                    }
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "Share file intent trigger", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { viewModel.deleteFile(file) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete physical item path", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showRenameDialog && selectedFileForRename != null) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Media File") },
                text = {
                    Column {
                        Text(text = "Specify the updated label name. The original extension (.${selectedFileForRename!!.extension}) is automatically appended.", fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                        OutlinedTextField(
                            value = renameInputName,
                            onValueChange = { renameInputName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter target filename") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (renameInputName.isNotBlank()) {
                                viewModel.renameFile(selectedFileForRename!!, renameInputName)
                                showRenameDialog = false
                            }
                        }
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
