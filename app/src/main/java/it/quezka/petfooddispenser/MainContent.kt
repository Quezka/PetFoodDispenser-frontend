package it.quezka.petfooddispenser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
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
    val haptic = LocalHapticFeedback.current

    if (uiState.isSetupRequired) {
        Column(
            modifier = modifier.padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.setup_required),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.setup_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenSettings() 
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.open_settings))
            }
        }
    } else {
        val pullToRefreshState = rememberPullToRefreshState()
        
        Column(modifier = modifier.fillMaxSize()) {
            // Scrollable Area
            PullToRefreshBox(
                isRefreshing = uiState.isProbing,
                onRefresh = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRefresh() 
                },
                state = pullToRefreshState,
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.welcome), 
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    Text(
                        stringResource(R.string.server_label, serverIP), 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.padding(bottom = 20.dp))

                    if (!uiState.isConnected && uiState.error != null) {
                        Text(
                            stringResource(R.string.not_connected), 
                            modifier = Modifier.padding(bottom = 10.dp), 
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(
                            onClick = onRefresh, 
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) { 
                            Text(stringResource(R.string.retry_connection)) 
                        }
                    } else {
                        val state = uiState.dispenserState
                        val isRemote = state.mode == "remote"
                        val currentModeIndex = if (isRemote) 1 else 0

                        ModeSelector(
                            selectedIndex = currentModeIndex,
                            onSelectionChange = { index ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

                        if (uiState.showDebug) {
                            Spacer(Modifier.height(24.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(stringResource(R.string.debug_info), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                                Text(stringResource(R.string.debug_mode, state.mode), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(stringResource(R.string.debug_test_mode, state.testMode), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(stringResource(R.string.debug_remote, state.cr1Remote, state.cr2Remote, state.cr3Remote), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(stringResource(R.string.debug_physical, state.cr1, state.cr2, state.cr3), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                uiState.lastRawJson?.let {
                                    Text(stringResource(R.string.debug_json, it), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            // Permanent Erogate Button at the very bottom
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onManualErogate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .height(64.dp),
                enabled = uiState.isConnected && !uiState.isErogating,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isErogating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        stringResource(R.string.manual_erogate),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Main Content - Disconnected")
@Composable
fun MainContentDisconnectedPreview() {
    PetFoodDispenserTheme {
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
    PetFoodDispenserTheme {
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
