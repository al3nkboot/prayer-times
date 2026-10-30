package com.midroid.prayertimes.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.midroid.prayertimes.R
import com.midroid.prayertimes.data.ThemeMode

val ArabicText = FontFamily(Font(R.font.amiri_regular), Font(R.font.amiri_bold, androidx.compose.ui.text.font.FontWeight.Bold))
val QuranText = FontFamily(Font(R.font.amiri_quran))

val Teal = Color(0xFF0F4C4A)
val TealLight = Color(0xFF1F7A72)
val Gold = Color(0xFFE9B949)
val Sand = Color(0xFFF7F3EA)

private val Light = lightColorScheme(
    primary = Teal, onPrimary = Color.White, primaryContainer = Color(0xFFCFE8E2), onPrimaryContainer = Color(0xFF062B29),
    secondary = Color(0xFF9A6B00), secondaryContainer = Color(0xFFFBE7B5), onSecondaryContainer = Color(0xFF3A2800),
    background = Sand, onBackground = Color(0xFF1B1C1A), surface = Color(0xFFFFFCF5), onSurface = Color(0xFF1B1C1A),
    surfaceVariant = Color(0xFFECE6D8), onSurfaceVariant = Color(0xFF5B5A52), outline = Color(0xFFCFC8B6),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF7FD3C6), onPrimary = Color(0xFF003733), primaryContainer = Color(0xFF14504B), onPrimaryContainer = Color(0xFFCFF3EC),
    secondary = Gold, secondaryContainer = Color(0xFF4D3A0A), onSecondaryContainer = Color(0xFFFBE7B5),
    background = Color(0xFF0E1A1A), onBackground = Color(0xFFE3E6E3), surface = Color(0xFF152424), onSurface = Color(0xFFE3E6E3),
    surfaceVariant = Color(0xFF1E3131), onSurfaceVariant = Color(0xFFB4C2BF), outline = Color(0xFF3A4F4D),
)

@Composable
fun PrayerTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true }
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
