package it.quezka.petfooddispenser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@Composable
fun SettingsDialog(
    serverIP: String,
    onServerIPChange: (String) -> Unit,
    showDebug: Boolean,
    onDebugChange: (Boolean) -> Unit,
    testMode: Boolean,
    onTestModeChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogButtonColors = ButtonDefaults.textButtonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
    )

    // Local state to hold values while editing
    var localIP by remember { mutableStateOf(serverIP) }
    var localShowDebug by remember { mutableStateOf(showDebug) }
    var localTestMode by remember { mutableStateOf(testMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onServerIPChange(localIP)
                onDebugChange(localShowDebug)
                onTestModeChange(localTestMode)
                onDismiss()
            }, colors = dialogButtonColors) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = dialogButtonColors) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column {
                OutlinedTextField(
                    value = localIP,
                    onValueChange = { localIP = it },
                    label = { Text(stringResource(R.string.server_ip_address)) },
                    placeholder = { Text(stringResource(R.string.ip_placeholder)) },
                    singleLine = true,
                    // Changed from Decimal to Uri to allow colons (:) for port input
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                
                Spacer(Modifier.padding(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.show_debug_info), modifier = Modifier.weight(1f))
                    Switch(
                        checked = localShowDebug,
                        onCheckedChange = { localShowDebug = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    )
                }

                Spacer(Modifier.padding(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.test_mode), modifier = Modifier.weight(1f))
                    Switch(
                        checked = localTestMode,
                        onCheckedChange = { localTestMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    )
                }
            }
        }
    )
}
