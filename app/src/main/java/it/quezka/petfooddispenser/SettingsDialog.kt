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
    onDismiss: () -> Unit
) {
    // Local state to hold values while editing
    var localIP by remember { mutableStateOf(serverIP) }
    var localShowDebug by remember { mutableStateOf(showDebug) }
    
    val colors = ButtonDefaults.textButtonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onServerIPChange(localIP)
                onDebugChange(localShowDebug)
                onDismiss()
            }, colors = colors) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = colors) {
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
            }
        }
    )
}
