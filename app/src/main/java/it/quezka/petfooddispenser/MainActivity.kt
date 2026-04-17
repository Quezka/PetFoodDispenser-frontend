package it.quezka.petfooddispenser

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: DispenserViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    
    var menuExpanded by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    // Lifecycle Observer to refresh on Resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Only auto-refresh if we aren't waiting for a manual action (like fixing an IP)
            if (event == Lifecycle.Event.ON_RESUME && !uiState.waitingForManualAction) {
                Log.d("MainScaffold", "App Resumed - Triggering refresh")
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    if (uiState.isProbing) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(horizontal = 12.dp).size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(R.string.refresh)
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = stringResource(R.string.menu))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) }, 
                                onClick = { menuExpanded = false; showSettingsDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.about)) }, 
                                onClick = { menuExpanded = false; showAboutDialog = true }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        MainContent(
            uiState = uiState,
            serverIP = uiState.currentServerIp,
            onRefresh = { viewModel.refresh() },
            onModeChange = { viewModel.setMode(it) },
            onValueChange = { index, value -> viewModel.updateRemoteValue(index, value) },
            onOpenSettings = { showSettingsDialog = true },
            onToggleTestTime = { viewModel.toggleTestTime(it) },
            onManualErogate = { viewModel.manualErogate() },
            modifier = Modifier.padding(innerPadding)
        )
    }
    
    // Connection Error Dialog
    // We only show this if there's an error AND we are waiting for a manual action
    if (uiState.error != null && uiState.waitingForManualAction) {
        val dialogButtonColors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        )

        AlertDialog(
            onDismissRequest = { /* Don't auto-refresh on dismiss */ },
            title = { Text(stringResource(R.string.connection_failed)) },
            text = { Text(stringResource(R.string.connection_error_msg, uiState.currentServerIp.ifBlank { "Server" })) },
            confirmButton = { 
                // This just opens settings. The actual refresh happens when SettingsDialog calls updateServerIp
                TextButton(onClick = { showSettingsDialog = true }, colors = dialogButtonColors) {
                    Text(stringResource(R.string.settings)) 
                } 
            },
            dismissButton = { 
                TextButton(onClick = { viewModel.refresh() }, colors = dialogButtonColors) {
                    Text(stringResource(R.string.retry_connection))
                } 
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            serverIP = uiState.currentServerIp,
            onServerIPChange = { viewModel.updateServerIp(it) },
            showDebug = uiState.showDebug,
            onDebugChange = { viewModel.setDebug(it) },
            testMode = uiState.isTestModeEnabled,
            onTestModeChange = { viewModel.updateTestMode(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { 
            PetFoodDispenserTheme { 
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScaffold() 
                }
            } 
        }
    }
}
