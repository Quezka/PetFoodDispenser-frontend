package it.quezka.petfooddispenser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
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
                Text(stringResource(R.string.close_button))
            }
        },
        title = { Text(stringResource(R.string.about)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = stringResource(R.string.app_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.padding(bottom = 16.dp))
                Text(
                    text = stringResource(R.string.app_version),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = stringResource(R.string.app_dev),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = stringResource(R.string.app_credits),
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
