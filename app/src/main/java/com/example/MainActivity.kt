package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.presentation.MediaHubViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MediaHubViewModel by viewModels {
        MediaHubViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settingsState.collectAsState()
            
            // Storage and Media Permissions Handler
            val context = androidx.compose.ui.platform.LocalContext.current
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.all { it.value }
                if (allGranted) {
                    android.widget.Toast.makeText(context, "Permissions granted! Ready to save to device downloads.", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Storage permissions omitted. Standard downloads will still function via backup.", android.widget.Toast.LENGTH_LONG).show()
                }
            }

            androidx.compose.runtime.LaunchedEffect(Unit) {
                val permissionsToRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_AUDIO,
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
                try {
                    permissionLauncher.launch(permissionsToRequest)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Failed launching permissions request", e)
                }
            }

            MyApplicationTheme(darkTheme = settings.darkMode) {
                val screen by viewModel.currentScreen.collectAsState()

                when (screen) {
                    "splash" -> SplashScreen()
                    "onboarding" -> OnboardingScreen(onFinished = { viewModel.completeOnboarding() })
                    "player" -> MediaPlayerScreen(viewModel = viewModel)
                    else -> MainContainer(screen = screen, viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(screen: String, viewModel: MediaHubViewModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (screen != "home") {
                TopAppBar(
                    title = { 
                        Text(
                            text = when(screen) {
                                "downloads" -> "Downloader Center"
                                "file_manager" -> "Sandbox Storage"
                                "history" -> "Download History"
                                "settings" -> "App Preferences"
                                else -> "MediaHub"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                val items = listOf(
                    NavigationItem("Home", "home", Icons.Filled.Home),
                    NavigationItem("Queue", "downloads", Icons.Filled.CloudDownload),
                    NavigationItem("Browser", "file_manager", Icons.Filled.FolderOpen),
                    NavigationItem("History", "history", Icons.Filled.History),
                    NavigationItem("Settings", "settings", Icons.Filled.Settings)
                )

                items.forEach { item ->
                    NavigationBarItem(
                        selected = screen == item.route,
                        onClick = { viewModel.navigateTo(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (screen) {
                "home" -> HomeScreen(viewModel = viewModel)
                "downloads" -> DownloadsScreen(viewModel = viewModel)
                "file_manager" -> FileManagerScreen(viewModel = viewModel)
                "history" -> HistoryScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

