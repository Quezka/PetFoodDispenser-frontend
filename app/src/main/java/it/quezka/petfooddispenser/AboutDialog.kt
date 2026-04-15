package it.quezka.petfooddispenser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val schoolUrl = "https://italessandrini.edu.it/"
    val colors = ButtonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        disabledContentColor = MaterialTheme.colorScheme.secondary,
        disabledContainerColor = MaterialTheme.colorScheme.secondary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss, colors = colors) {
                Text("Close")
            }
        },
        title = { Text("About") },
        text = {
            Column {
                Text(
                    text = "Pet Food Dispenser Controller",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "A Jetpack Compose application to control an Arduino-powered pet food dispenser via REST API.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.padding(bottom = 16.dp))
                Text(
                    text = "Version: 1.0.0",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "Developed by Quezka",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "for I.I.S. \"E. Alessandrini\" - Montesilvano",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = schoolUrl,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        uriHandler.openUri(schoolUrl)
                    }
                )
            }
        }
    )
}
