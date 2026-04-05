package it.quezka.petfooddispenser

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
fun FeedScreen(
    onFeedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Alessandrini Pet Food Dispenser",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Pet Image",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = onFeedClick,
            colors = ButtonColors(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.onPrimary,
                MaterialTheme.colorScheme.onPrimaryContainer),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Feed My Pet")
        }
    }
}
