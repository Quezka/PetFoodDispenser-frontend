package it.quezka.petfooddispenser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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

@Composable
fun MainContent(
    uiState: UiState,
    serverIP: String,
    onRefresh: () -> Unit,
    onModeChange: (Boolean) -> Unit,
    onValueChange: (Int, Float) -> Unit,
    onOpenSettings: () -> Unit,
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
                Text(stringResource(R.string.not_connected), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRefresh) { Text(stringResource(R.string.retry_connection)) }
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

                Spacer(Modifier.weight(1f))

                // Debug Info
                if (uiState.showDebug) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(stringResource(R.string.debug_info), style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        Text(stringResource(R.string.debug_mode, state.mode), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
