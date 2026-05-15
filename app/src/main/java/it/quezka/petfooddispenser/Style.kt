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
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    error = ErrorDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    outline = OutlineDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

val LightColorScheme = lightColorScheme(
    primary = PrimaryWhite,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerWhite,
    onPrimaryContainer = OnPrimaryContainerWhite,
    secondary = SecondaryWhite,
    onSecondary = OnSecondaryWhite,
    tertiary = TertiaryWhite,
    onTertiary = OnTertiaryWhite,
    error = ErrorWhite,
    background = BackgroundWhite,
    onBackground = OnBackgroundWhite,
    surface = SurfaceWhite,
    onSurface = OnSurfaceWhite,
    outline = OutlineWhite,
    onSurfaceVariant = OnSurfaceVariantWhite
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
        typography = Typography,
        content = content
    )
}
