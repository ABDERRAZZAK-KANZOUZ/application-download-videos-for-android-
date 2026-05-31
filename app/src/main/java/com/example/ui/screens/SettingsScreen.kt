package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.SettingsEntity
import com.example.presentation.MediaHubViewModel

@Composable
fun SettingsScreen(
    viewModel: MediaHubViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settingsState.collectAsState()
    val scrollState = rememberScrollState()

    var showLanguageSheet by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English (US)") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Engine Preferences",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Download on Wi-Fi Only", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Prevent downloads on cellular networks to preserve standard data fees.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = settings.wifiOnly,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(wifiOnly = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Concurrent Download Limit", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Set the maximum amount of files that can download simultaneously (${settings.maxDownloads} threads).", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (settings.maxDownloads > 1) {
                                    viewModel.updateSettings(settings.copy(maxDownloads = settings.maxDownloads - 1))
                                }
                            },
                            enabled = settings.maxDownloads > 1
                        ) {
                            Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Decrease concurrent limit")
                        }
                        Text(text = "${settings.maxDownloads}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                        IconButton(
                            onClick = {
                                if (settings.maxDownloads < 5) {
                                    viewModel.updateSettings(settings.copy(maxDownloads = settings.maxDownloads + 1))
                                }
                            },
                            enabled = settings.maxDownloads < 5
                        ) {
                            Icon(Icons.Filled.AddCircleOutline, contentDescription = "Increase concurrent limit")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "App Appearance",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Dark Visual Theme", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Lower battery usage and eye tension with high-contrast night aesthetics.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = settings.darkMode,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(darkMode = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageSheet = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "App Language", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Configure translation packages throughout app navigation keys.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedLanguage, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next arrow icon")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "About & Compliance",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "MediaHub Applet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Version 1.2.0-Alpha (Production Mode)", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A modern high-speed sandbox media downloader built utilizing material design 3 tokens, Room database flows, foreground services, and coroutine execution threads.\n\nPlease ensure you download media only from streams which explicitly permit copying. All operations occur locally inside your app's protected context sandbox.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Active Sandbox Workspace Folder:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "/data/user/0/com.aistudio.mediahub.mrgpvk/files/downloads",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        if (showLanguageSheet) {
            AlertDialog(
                onDismissRequest = { showLanguageSheet = false },
                title = { Text("Select Language package") },
                text = {
                    Column {
                        listOf("English (US)", "Español (Latam)", "Français (Europe)", "Deutsch (Original)", "日本語 (Standard)").forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLanguage = lang
                                        showLanguageSheet = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = lang, fontSize = 15.sp)
                                if (selectedLanguage == lang) {
                                    Icon(Icons.Filled.Check, contentDescription = "Selected language checkmark icon", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageSheet = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
