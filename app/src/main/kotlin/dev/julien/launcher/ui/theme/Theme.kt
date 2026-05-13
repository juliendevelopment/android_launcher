package dev.julien.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFB6C8FF),
    onPrimary = Color(0xFF0A1B3D),
    background = Color(0x00000000),
    surface = Color(0xCC101014),
    onSurface = Color(0xFFEDEDF2),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF3F5BCB),
    onPrimary = Color.White,
    background = Color(0x00000000),
    surface = Color(0xCCFFFFFF),
    onSurface = Color(0xFF0D0D10),
)

@Composable
fun LauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
