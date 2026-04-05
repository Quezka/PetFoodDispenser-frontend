package it.quezka.petfooddispenser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80, background = Color(0xFF000000),
    surface = Color(0xFF000000),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimaryContainer = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF8FDFF),
    surface = Color(0xFFF8FDFF),
    onPrimary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onPrimaryContainer = Color.White,
)

private val Typography = androidx.compose.material3.Typography(
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun PetFoodDispenserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // You can define this similarly
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    PetFoodDispenserTheme {
        FeedScreen(onFeedClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ModeKnobPreview() {
    PetFoodDispenserTheme {
        ModeKnob()
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetFoodDispenserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )
                {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        FeedScreen(
                            modifier = Modifier.padding(innerPadding),
                            onFeedClick = {
                                // Handle feed click
                            }
                        )
                    }
                }
            }
        }
    }
}
