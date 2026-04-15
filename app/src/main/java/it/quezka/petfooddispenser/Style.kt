package it.quezka.petfooddispenser

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnPrimaryDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    onPrimaryContainer = OnPrimaryDark,
)

val LightColorScheme = lightColorScheme(
    primary = PrimaryWhite,
    secondary = SecondaryWhite,
    tertiary = TertiaryWhite,
    background = BackgroundWhite,
    surface = SurfaceWhite,
    onPrimary = OnPrimaryWhite,
    onBackground = OnBackgroundWhite,
    onSurface = OnSurfaceWhite,
    onPrimaryContainer = OnPrimaryWhite,
)

val Typography = androidx.compose.material3.Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
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