package com.midroid.prayertimes.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.midroid.prayertimes.R
import com.midroid.prayertimes.data.AppSettings
import com.midroid.prayertimes.domain.*
import java.time.Duration
import java.time.ZonedDateTime

data class Perms(val notifications: Boolean, val exact: Boolean, val dnd: Boolean = true)

@Composable
fun HomeScreen(s: AppSettings, now: ZonedDateTime, perms: Perms, onCity: () -> Unit, onSettings: () -> Unit, onExact: () -> Unit, onGrantNotif: () -> Unit, onQibla: () -> Unit = {}, onAdhkar: () -> Unit = {}, onTasbih: () -> Unit = {}, onDuas: () -> Unit = {}, onAbout: () -> Unit = {}) {
    val st = remember(s, now.withSecond(0).withNano(0)) { PrayerEngine.now(s.place, now, s.calc) }
    val remaining = Duration.between(now, st.nextAt)
    val ar = isArabic()
    val local = now.withZoneSameInstant(s.place.zoneId)
    val nextFajr = remember(s, st.day.date) { PrayerEngine.day(s.place, st.day.date.plusDays(1), s.calc)[Prayer.FAJR] }
    val night = remember(st.day, nextFajr) { NightTimesCalculator.calculate(st.day[Prayer.MAGHRIB], nextFajr) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).clickable(onClick = onCity).padding(8.dp).testTag("city"), verticalAlignment = Alignment.CenterVertically) {
                Icon(AppIcons.City, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(7.dp)); Column { Text(placeName(s.place), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); if (countryName(s.place).isNotEmpty()) Text(countryName(s.place), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            IconButton(onClick = onQibla, modifier = Modifier.testTag("qibla")) { Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_qibla), stringResource(R.string.qibla)) }
            IconButton(onClick = onSettings, modifier = Modifier.testTag("settings")) { Icon(Icons.Filled.Settings, stringResource(R.string.settings)) }
        }
        val anyAlert = s.alerts.any { it != AlertMode.OFF }
        if (anyAlert && !perms.notifications) { NoticeCard(stringResource(R.string.notif_perm_off), stringResource(R.string.grant), onGrantNotif); Spacer(Modifier.height(8.dp)) }
        else if (anyAlert && !perms.exact) { NoticeCard(stringResource(R.string.exact_off), stringResource(R.string.fix), onExact, Modifier.testTag("exact_note")); Spacer(Modifier.height(8.dp)) }
        HeroCard(st, remaining, local, now.hour, s.place, onCity)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatGregorian(local.toLocalDate(), ar), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(Hijri.format(Hijri.at(s.place, now, s.calc, s.hijriAdjust), ar), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA).forEach { p ->
                val selected = st.next == p && st.nextAt.toLocalDate() == st.day.date
                val scheme = MaterialTheme.colorScheme
                val bg by animateColorAsState(if (selected) scheme.primaryContainer else scheme.surface, label = "prayer-card")
                Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(bg).padding(vertical = 10.dp, horizontal = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(prayerIcon(p), null, tint = if (selected) scheme.primary else scheme.secondary, modifier = Modifier.size(17.dp))
                    Text(prayerName(p, st.day.date), style = MaterialTheme.typography.labelSmall, maxLines = 1, fontSize = 9.sp)
                    Text(formatTime(st.day[p], ar).substringBefore(' '), style = MaterialTheme.typography.labelSmall, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.night_midnight, formatTime(night.midnight, ar)), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.night_last_third, formatTime(night.lastThird, ar)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.home_sections), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        val tiles = listOf(
            Tile(R.string.qibla, AppIcons.Kaaba, onQibla), Tile(R.string.adhkar_title, AppIcons.Adhkar, onAdhkar, "adhkar"),
            Tile(R.string.settings, Icons.Filled.Settings, onSettings), Tile(R.string.choose_city, AppIcons.City, onCity, "city_tile"),
            Tile(R.string.about_title, AppIcons.Info, onAbout, "about_tile"), Tile(R.string.soon_tasbih, AppIcons.Tasbih, onTasbih, "tasbih_tile"),
            Tile(R.string.soon_duas, AppIcons.Duas, onDuas, "duas_tile"), Tile(R.string.soon_quran, AppIcons.Quran, null))
        tiles.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { tile ->
            Card(Modifier.weight(1f).padding(bottom = 8.dp).then(if (tile.action != null) Modifier.clickable(onClick = tile.action).testTag(tile.tag ?: "") else Modifier), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(tile.icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Text(stringResource(tile.title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold); if (tile.action == null) Text(" · " + stringResource(R.string.coming_soon), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        } } }
        Spacer(Modifier.height(8.dp)); Text(methodName(s.calc.method), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally)); Spacer(Modifier.height(18.dp))
    }
}
private data class Tile(val title: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector, val action: (() -> Unit)?, val tag: String? = null)

private fun prayerIcon(p: Prayer) = when (p) {
    Prayer.FAJR -> AppIcons.Fajr
    Prayer.SUNRISE -> AppIcons.Sunrise
    Prayer.DHUHR -> AppIcons.Dhuhr
    Prayer.ASR -> AppIcons.Asr
    Prayer.MAGHRIB -> AppIcons.Maghrib
    Prayer.ISHA -> AppIcons.Isha
}

@Composable
private fun HeroCard(st: NowState, remaining: Duration, local: ZonedDateTime, hour: Int, place: Place, onCity: () -> Unit) {
    val palette = when (hour) { in 4..6 -> listOf(Color(0xFF533B69), Color(0xFFE79A72)); in 7..13 -> listOf(Color(0xFF177E89), Color(0xFF8DC7B6)); in 14..16 -> listOf(Color(0xFF385A7C), Color(0xFFDEA66B)); in 17..19 -> listOf(Color(0xFF7D4167), Color(0xFFF19A69)); else -> listOf(Color(0xFF111E3B), Color(0xFF354B71)) }
    val nextName = prayerName(st.next, st.nextAt.toLocalDate())
    Box(Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(palette))) {
        MosqueSilhouette(Modifier.fillMaxSize(), Color(0xFF101827).copy(alpha = .35f))
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onCity) { Icon(AppIcons.City, null, tint = Color.White); Text(placeName(place), color = Color.White) } }
            Text(stringResource(R.string.next_prayer), color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.labelLarge)
            Text(nextName, color = Color(0xFFFFE3A1), fontSize = 29.sp, fontWeight = FontWeight.Bold)
            AnimatedContent(targetState = PrayerEngine.formatCountdown(remaining), label = "countdown") { value -> Text(value, color = Color.White, fontSize = 45.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace, style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr), modifier = Modifier.testTag("countdown")) }
            Text(stringResource(R.string.remaining, nextName) + " · " + formatTime(st.nextAt, isArabic()), color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
@Composable private fun MosqueSilhouette(modifier: Modifier, color: Color) { Canvas(modifier) {
    val w=size.width; val h=size.height
    val p=Path().apply { moveTo(w*.08f,h); lineTo(w*.08f,h*.80f); lineTo(w*.15f,h*.80f); lineTo(w*.15f,h*.57f); lineTo(w*.18f,h*.49f); lineTo(w*.21f,h*.57f); lineTo(w*.21f,h*.8f); lineTo(w*.32f,h*.8f); lineTo(w*.32f,h*.67f); quadraticTo(w*.5f,h*.35f,w*.68f,h*.67f); lineTo(w*.68f,h*.8f); lineTo(w*.79f,h*.8f); lineTo(w*.79f,h*.57f); lineTo(w*.82f,h*.49f); lineTo(w*.85f,h*.57f); lineTo(w*.85f,h*.8f); lineTo(w*.92f,h*.8f); lineTo(w*.92f,h); close() }; drawPath(p,color)
} }
