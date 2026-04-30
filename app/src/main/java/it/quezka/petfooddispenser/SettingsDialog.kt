package it.quezka.petfooddispenser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
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
    prolungheSerbatoi: Int,
    onProlungheSerbatoiChange: (Int) -> Unit,
    volumeMin: Int,
    onVolumeMinChange: (Int) -> Unit,
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
    var localProlunghe by remember { mutableStateOf(prolungheSerbatoi) }
    var localVolumeMin by remember { mutableStateOf(volumeMin.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onServerIPChange(localIP)
                onDebugChange(localShowDebug)
                onTestModeChange(localTestMode)
                onProlungheSerbatoiChange(localProlunghe)
                onVolumeMinChange(localVolumeMin.toIntOrNull() ?: volumeMin)
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
                        )
                    )
                }

                Spacer(Modifier.padding(8.dp))
                HorizontalDivider()
                Spacer(Modifier.padding(8.dp))

                OutlinedTextField(
                    value = localVolumeMin,
                    onValueChange = { if (it.all { char -> char.isDigit() }) localVolumeMin = it },
                    label = { Text(stringResource(R.string.volume_min_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.padding(12.dp))

                Text(
                    text = stringResource(R.string.prolunghe_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                val radioOptions = listOf(0, 1, 2, 3)
                radioOptions.forEach { count ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (count == localProlunghe),
                                onClick = { localProlunghe = count },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (count == localProlunghe),
                            onClick = null
                        )
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    )
}
