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
    prolungheSerbatoio: Int,
    onProlungheSerbatoioChange: (Int) -> Unit,
    volumeMin: Int,
    onVolumeMinChange: (Int) -> Unit,
    isFoodDispenser: Boolean,
    onTipoDispenserChange: (Boolean) -> Unit,
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
    var localProlunghe by remember { mutableStateOf(prolungheSerbatoio) }
    var localVolumeMin by remember { mutableStateOf(volumeMin.toString()) }
    var localIsFood by remember { mutableStateOf(isFoodDispenser) }

    // Stricter Validation
    val isIPValid = remember(localIP) {
        if (localIP.isBlank()) return@remember true
        
        val host = localIP.substringBefore(':')
        
        // Regex for IPv4: ensures 4 octets 0-255
        val ipRegex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex()
        
        // Regex for Hostname: allows dots but requires the last label to contain at least one letter
        val hostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9]|[a-zA-Z])$".toRegex()

        ipRegex.matches(host) || hostnameRegex.matches(host)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (isIPValid) {
                        onServerIPChange(localIP)
                        onDebugChange(localShowDebug)
                        onTestModeChange(localTestMode)
                        onProlungheSerbatoioChange(localProlunghe)
                        onVolumeMinChange(localVolumeMin.toIntOrNull() ?: volumeMin)
                        onTipoDispenserChange(localIsFood)
                        onDismiss()
                    }
                }, 
                colors = dialogButtonColors,
                enabled = isIPValid && localIP.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = dialogButtonColors) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.settings), color = MaterialTheme.colorScheme.primary) },
        text = {
            Column {
                OutlinedTextField(
                    value = localIP,
                    onValueChange = { localIP = it },
                    label = { Text(stringResource(R.string.server_ip_address)) },
                    placeholder = { Text(stringResource(R.string.ip_placeholder)) },
                    singleLine = true,
                    isError = !isIPValid,
                    supportingText = {
                        if (!isIPValid) {
                            Text(text = "Invalid IP or Hostname", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
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
                    text = stringResource(R.string.dispenser_type_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .selectable(
                                selected = localIsFood,
                                onClick = { localIsFood = true },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = localIsFood, onClick = null)
                        Text(text = stringResource(R.string.food_label), modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .selectable(
                                selected = !localIsFood,
                                onClick = { localIsFood = false },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = !localIsFood, onClick = null)
                        Text(text = stringResource(R.string.h2o_label), modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(Modifier.padding(8.dp))

                Text(
                    text = stringResource(R.string.prolunghe_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
