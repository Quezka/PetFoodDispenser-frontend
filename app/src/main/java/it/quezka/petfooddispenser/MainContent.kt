package it.quezka.petfooddispenser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface

@Composable
fun MainContent(
    uiState: UiState,
    serverIP: String,
    onRefresh: () -> Unit,
    onModeChange: (Boolean) -> Unit,
    onValueChange: (Int, Float) -> Unit,
    onOpenSettings: () -> Unit,
    onManualErogate: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isSetupRequired) {
        Column(
            modifier = modifier.padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.setup_required),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.setup_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = onOpenSettings) {
                Text(stringResource(R.string.open_settings))
            }
        }
    } else {
        val buttonColors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        )

        Column(
            modifier = modifier.padding(18.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.welcome), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.server_label, serverIP), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.padding(bottom = 20.dp))

            if (uiState.isProbing && !uiState.isConnected) {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(Modifier.weight(1f))
            } else if (!uiState.isConnected && uiState.error != null) {
                Text(stringResource(R.string.not_connected), modifier = Modifier.padding(bottom = 10.dp), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRefresh, colors = buttonColors) { Text(stringResource(R.string.retry_connection)) }
            } else {
                val state = uiState.dispenserState
                val isRemote = state.mode == "remote"
                val currentModeIndex = if (isRemote) 1 else 0

                ModeSelector(
                    selectedIndex = currentModeIndex,
                    onSelectionChange = { index ->
                        onModeChange(index == 1)
                    }
                )

                Spacer(Modifier.padding(bottom = 20.dp))

                SliderCR1(
                    value = if (isRemote) state.cr1Remote else state.cr1,
                    enabled = isRemote,
                    onValueChange = { newValue ->
                        onValueChange(1, newValue)
                    }
                )
                SliderCR2(
                    value = if (isRemote) state.cr2Remote else state.cr2,
                    enabled = isRemote,
                    onValueChange = { newValue ->
                        onValueChange(2, newValue)
                    }
                )
                SliderCR3(
                    value = if (isRemote) state.cr3Remote else state.cr3,
                    enabled = isRemote,
                    onValueChange = { newValue ->
                        onValueChange(3, newValue)
                    }
                )

                // Show Test Controls if server says testMode is active OR if local preference is set
                if (state.testMode || uiState.isTestModeEnabled) {
                    Spacer(Modifier.height(24.dp))
                    TestControls(
                        onManualErogate = onManualErogate
                    )
                }

                Spacer(Modifier.weight(1f))

                // Debug Info
                if (uiState.showDebug) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(stringResource(R.string.debug_info), style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        Text(stringResource(R.string.debug_mode, state.mode), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(stringResource(R.string.debug_test_mode, state.testMode), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(stringResource(R.string.debug_remote, state.cr1Remote, state.cr2Remote, state.cr3Remote), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(stringResource(R.string.debug_physical, state.cr1, state.cr2, state.cr3), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        uiState.lastRawJson?.let {
                            Text(stringResource(R.string.debug_json, it), style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestControls(
    onManualErogate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.erogation_test),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onManualErogate,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(stringResource(R.string.manual_erogate))
        }
    }
}

@Preview(showBackground = true, name = "Main Content - Disconnected")
@Composable
fun MainContentDisconnectedPreview() {
    MaterialTheme {
        Surface {
            MainContent(
                uiState = UiState(
                    isConnected = false,
                    error = "Connection Failed"
                ),
                serverIP = "192.168.1.100",
                onRefresh = {},
                onModeChange = {},
                onValueChange = { _, _ -> },
                onOpenSettings = {},
                onManualErogate = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Main Content - Connected (Local)")
@Composable
fun MainContentConnectedLocalPreview() {
    MaterialTheme {
        Surface {
            MainContent(
                uiState = UiState(
                    isConnected = true,
                    dispenserState = DispenserState(mode = "local", cr1 = 2f, cr2 = 3f, cr3 = 4f)
                ),
                serverIP = "192.168.1.100",
                onRefresh = {},
                onModeChange = {},
                onValueChange = { _, _ -> },
                onOpenSettings = {},
                onManualErogate = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Main Content - Connected (Remote + Test Mode)")
@Composable
fun MainContentConnectedRemotePreview() {
    MaterialTheme {
        Surface {
            MainContent(
                uiState = UiState(
                    isConnected = true,
                    isTestModeEnabled = true,
                    dispenserState = DispenserState(mode = "remote", cr1Remote = 3f, cr2Remote = 1f, cr3Remote = 5f, testMode = true)
                ),
                serverIP = "192.168.1.100",
                onRefresh = {},
                onModeChange = {},
                onValueChange = { _, _ -> },
                onOpenSettings = {},
                onManualErogate = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Setup Required")
@Composable
fun SetupRequiredPreview() {
    MaterialTheme {
        Surface {
            MainContent(
                uiState = UiState(isSetupRequired = true),
                serverIP = "",
                onRefresh = {},
                onModeChange = {},
                onValueChange = { _, _ -> },
                onOpenSettings = {},
                onManualErogate = {}
            )
        }
    }
}
