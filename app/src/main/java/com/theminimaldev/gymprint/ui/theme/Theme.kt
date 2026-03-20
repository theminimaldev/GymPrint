package com.theminimaldev.gymprint.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Fallback green-based seed palette for API 26–30
val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = White,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,
    secondary = PurpleGrey40,
    onSecondary = White,
    secondaryContainer = PurpleGrey90,
    onSecondaryContainer = PurpleGrey10,
    tertiary = Pink40,
    onTertiary = White,
    tertiaryContainer = Pink90,
    onTertiaryContainer = Pink10,
    surfaceVariant = PurpleGrey90,
    onSurfaceVariant = PurpleGrey30,
)

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple20,
    primaryContainer = Purple30,
    onPrimaryContainer = Purple90,
    secondary = PurpleGrey80,
    onSecondary = PurpleGrey20,
    secondaryContainer = PurpleGrey30,
    onSecondaryContainer = PurpleGrey90,
    tertiary = Pink80,
    onTertiary = Pink20,
    tertiaryContainer = Pink30,
    onTertiaryContainer = Pink90,
    surfaceVariant = PurpleGrey30,
    onSurfaceVariant = PurpleGrey80,
)

@Composable
fun GymprintTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
