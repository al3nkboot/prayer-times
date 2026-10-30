package com.midroid.prayertimes.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector.Builder

/** Small original line icons used throughout the home screen. */
object AppIcons {
    private fun icon(name: String, paths: List<String>): ImageVector = Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        paths.forEachIndexed { index, data ->
            addPath(pathData = PathParser().parsePathString(data).toNodes(),
                fill = SolidColor(if (index == 0) Color.Black else Color(0xFFC99C48)),
                stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }.build()

    val Kaaba = icon("Kaaba", listOf("M3 8 12 3l9 5-9 5-9-5Z M3 8v9l9 5 9-5V8 M12 13v9", "M3 11l9 5 9-5 M9 11v3"))
    val Tasbih = Builder("Tasbih", 24.dp, 24.dp, 24f, 24f).apply {
        // Fine circular cord with nine individually readable beads and a hanging tassel.
        addPath(pathData = PathParser().parsePathString("M12 3a8 8 0 1 0 0 16 8 8 0 0 0 0-16Z M12 19v1.5 M12 20.5l-2 2 M12 20.5l2 2").toNodes(), fill = SolidColor(Color.Transparent), stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        listOf("M12 2.5a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z", "M17.5 4.5a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z", "M20 10a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z", "M18 16a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z", "M12 18.5a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z", "M6 16a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z", "M4 10a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z", "M6.5 4.5a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z", "M12 3a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z").forEach { d ->
            addPath(pathData = PathParser().parsePathString(d).toNodes(), fill = SolidColor(Color(0xFFC99C48)), stroke = SolidColor(Color.Black), strokeLineWidth = 1f)
        }
    }.build()
    val Duas = icon("Duas", listOf("M12 21v-6 M12 15 8 10c-1-1-2 0-1 1l3 5 M12 15l4-5c1-1 2 0 1 1l-3 5 M8 10 6 6c-.5-1.2-2-.5-1.5.7L7 13 M16 10l2-4c.5-1.2 2-.5 1.5.7L17 13 M9 21h6"))
    val Quran = icon("Quran", listOf("M12 6c-3-2-6-2-9-1v14c3-1 6-1 9 1 3-2 6-2 9-1V5c-3-1-6-1-9 1Z M12 6v14 M5 9c2-.5 3-.3 5 .3 M19 9c-2-.5-3-.3-5 .3"))
    val City = icon("City", listOf("M3 21V8l6-3v16 M9 10h6v11 M15 4l6 3v14 M5 11h2 M5 15h2 M11 13h2 M11 17h2 M18 11h1 M18 15h1 M2 21h20"))
    val Info = Builder("Info", 24.dp, 24.dp, 24f, 24f).apply {
        addPath(pathData = PathParser().parsePathString("M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18Z").toNodes(), fill = SolidColor(Color.Transparent), stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f)
        addPath(pathData = PathParser().parsePathString("M12 11v5 M12 7v.01").toNodes(), fill = SolidColor(Color.Transparent), stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round)
    }.build()
    val Adhkar = icon("Adhkar", listOf("M18.5 4.5A8 8 0 1 0 20 17 8 8 0 0 1 18.5 4.5Z M17 7v4 M15 9h4"))
    val Fajr = icon("Fajr", listOf("M3 19h18 M5 19a7 7 0 0 1 14 0 M12 3v3 M4.5 8l2 2 M19.5 8l-2 2"))
    val Sunrise = icon("Sunrise", listOf("M3 19h18 M6 19a6 6 0 0 1 12 0 M12 4v3 M5 10l2 2 M19 10l-2 2"))
    val Dhuhr = icon("Dhuhr", listOf("M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10Z M12 2v2 M12 20v2 M4.9 4.9l1.4 1.4 M17.7 17.7l1.4 1.4 M2 12h2 M20 12h2 M4.9 19.1l1.4-1.4 M17.7 6.3l1.4-1.4"))
    val Asr = icon("Asr", listOf("M3 19h18 M6 19a6 6 0 0 1 12 0 M12 3v3 M4.5 8l2 2 M19.5 8l-2 2 M3 13h3 M18 13h3"))
    val Maghrib = icon("Maghrib", listOf("M3 17h18 M6 17a6 6 0 0 1 12 0 M12 3v3 M4.5 8l2 2 M19.5 8l-2 2 M4 21h16"))
    val Isha = icon("Isha", listOf("M19 4a8 8 0 1 0 1 14A8 8 0 0 1 19 4Z M5 5l.5 1.5L7 7l-1.5.5L5 9l-.5-1.5L3 7l1.5-.5L5 5Z"))
}
