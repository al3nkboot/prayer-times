package com.midroid.prayertimes.ui

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import com.midroid.prayertimes.domain.SunPositionCalculator
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.Instant
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.midroid.prayertimes.R
import com.midroid.prayertimes.domain.Place
import com.midroid.prayertimes.domain.Qibla
import com.midroid.prayertimes.PrayerApp
import com.midroid.prayertimes.MainActivity
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class Heading(val trueDeg: Double, val lowAccuracy: Boolean)

/** Test hook for screenshots: when set, sensors are bypassed. */
object QiblaSensorOverride { @Volatile var heading: Heading? = null; @Volatile var forceNoSensor = false }

/** Device heading (true north) from rotation vector, falling back to accelerometer + magnetometer. null = no sensor. */
@Composable
fun rememberHeading(place: Place): State<Heading?>? {
    QiblaSensorOverride.heading?.let { return remember { mutableStateOf(it) } }
    val ctx = LocalContext.current
    val sm = remember { ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val rot = if (QiblaSensorOverride.forceNoSensor) null else sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    val acc = if (QiblaSensorOverride.forceNoSensor) null else sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val mag = if (QiblaSensorOverride.forceNoSensor) null else sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    if (rot == null && (acc == null || mag == null)) return null
    val state = remember { mutableStateOf<Heading?>(null) }
    val declination = remember(place) { GeomagneticField(place.lat.toFloat(), place.lon.toFloat(), 0f, System.currentTimeMillis()).declination.toDouble() }
    DisposableEffect(place) {
        val r = FloatArray(9); val o = FloatArray(3); val g = FloatArray(3); val m = FloatArray(3)
        var hasG = false; var hasM = false; var low = false
        var sx = 0.0; var sy = 0.0 // low-pass on the unit circle
        fun emit(azimuthRad: Float) {
            val a = 0.15
            sx = (1 - a) * sx + a * cos(azimuthRad.toDouble()); sy = (1 - a) * sy + a * sin(azimuthRad.toDouble())
            val mag0 = Math.toDegrees(atan2(sy, sx))
            state.value = Heading(((mag0 + declination) % 360 + 360) % 360, low)
        }
        val l = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                when (e.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> { SensorManager.getRotationMatrixFromVector(r, e.values); SensorManager.getOrientation(r, o); emit(o[0]) }
                    Sensor.TYPE_ACCELEROMETER -> { System.arraycopy(e.values, 0, g, 0, 3); hasG = true }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(e.values, 0, m, 0, 3); hasM = true
                        if (rot == null && hasG && SensorManager.getRotationMatrix(r, null, g, m)) { SensorManager.getOrientation(r, o); emit(o[0]) }
                    }
                }
            }
            override fun onAccuracyChanged(s: Sensor, accuracy: Int) {
                if (s.type == Sensor.TYPE_MAGNETIC_FIELD || s.type == Sensor.TYPE_ROTATION_VECTOR)
                    low = accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW
            }
        }
        if (rot != null) sm.registerListener(l, rot, SensorManager.SENSOR_DELAY_UI)
        else { sm.registerListener(l, acc, SensorManager.SENSOR_DELAY_UI) }
        if (mag != null) sm.registerListener(l, mag, SensorManager.SENSOR_DELAY_UI) // also gives accuracy status
        onDispose { sm.unregisterListener(l) }
    }
    return state
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(place: Place, onBack: () -> Unit) {
    val target = remember(place) { Qibla.bearing(place.lat, place.lon) }
    val heading = rememberHeading(place)
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.qibla)) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, modifier = Modifier.testTag("tab_compass"), text = { Text(stringResource(R.string.tab_compass)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, modifier = Modifier.testTag("tab_sun"), text = { Text(stringResource(R.string.tab_sun)) })
            }
            if (tab == 1) SunShadowTab(place, target)
            else CompassTab(place, target, heading)
        }
    }
}

@Composable
private fun CompassTab(place: Place, target: Double, heading: State<Heading?>?) {
    val feedback = LocalHapticFeedback.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(placeName(place), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.qibla_bearing, target.roundToInt(), Qibla.distanceKm(place.lat, place.lon).roundToInt()), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        if (heading == null) {
            Card(Modifier.fillMaxWidth().testTag("no_sensor")) { Text(stringResource(R.string.qibla_no_sensor, target.roundToInt()), Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge) }
            return@Column
        }
        val h = heading.value
        val facing = h != null && kotlin.math.abs(Qibla.delta(target, h.trueDeg)) <= 3.0
        var wasFacing by remember { mutableStateOf(false) }
        LaunchedEffect(facing) { if (facing && !wasFacing) feedback.performHapticFeedback(HapticFeedbackType.LongPress); wasFacing = facing }
        val raw = h?.trueDeg?.toFloat() ?: 0f
        var previous by remember { mutableFloatStateOf(raw) }
        LaunchedEffect(raw) { previous += Qibla.delta(raw.toDouble(), previous.toDouble()).toFloat() }
        val unwrapped = previous
        val smooth by animateFloatAsState(unwrapped, label = "compass-heading")
        Compass(smooth.toDouble(), target, facing, listOf(stringResource(R.string.north), stringResource(R.string.east), stringResource(R.string.south), stringResource(R.string.west)), Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp))
        Spacer(Modifier.height(10.dp))
        Text(when { h == null -> stringResource(R.string.qibla_waiting); facing -> stringResource(R.string.qibla_facing); else -> stringResource(R.string.qibla_turn, kotlin.math.abs(Qibla.delta(target, h.trueDeg)).roundToInt()) }, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("qibla_status"), color = if (facing) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
        if (h?.lowAccuracy == true) { Spacer(Modifier.height(10.dp)); NoticeCard(stringResource(R.string.qibla_calibrate), "", {}) }
        Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.qibla_tip), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SunShadowTab(place: Place, target: Double) {
    var now by remember { mutableStateOf(ZonedDateTime.now(PrayerApp.clock)) }
    LaunchedEffect(Unit) {
        while (MainActivity.ticking) {
            now = ZonedDateTime.now(PrayerApp.clock)
            delay(30_000L)
        }
    }
    val sun = remember(now, place) { SunPositionCalculator.calculate(now, place.lat, place.lon) }
    val diff = kotlin.math.abs(Qibla.delta(target, sun.azimuth))
    val moments = listOf(5 to 27, 5 to 28, 7 to 15, 7 to 16).mapIndexed { index, md ->
        val minute = if (index < 2) 18 else 27
        ZonedDateTime.of(now.year, md.first, md.second, 12, minute, 0, 0, ZoneId.of("Asia/Riyadh")).withZoneSameInstant(place.zoneId)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.sun_angle, diff.roundToInt()), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.sun_altitude, sun.altitude.roundToInt(), sun.azimuth.roundToInt()), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(stringResource(R.string.zenith_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                moments.forEach { Text("• " + it.format(java.time.format.DateTimeFormatter.ofPattern("d MMM, h:mm a z", java.util.Locale.getDefault())), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp)) }
                Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.zenith_explain), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun Compass(heading: Double, target: Double, facing: Boolean, cardinal: List<String>, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Canvas(modifier) {
        val r = size.minDimension / 2; val ctr = Offset(size.width / 2, size.height / 2)
        drawCircle(c.surface, r, ctr)
        if (facing) {
            drawCircle(Color(0x553CCB78), r + 5f, ctr)
            drawCircle(Color(0x333CCB78), r + 12f, ctr)
        }
        drawCircle(if (facing) Color(0xFF37B66B) else c.outline, r, ctr, style = Stroke(if (facing) 8f else 4f))
        // fixed top marker = direction the phone points
        drawLine(c.primary, Offset(ctr.x, ctr.y - r - 4f), Offset(ctr.x, ctr.y - r + 34f), 8f)
        rotate(-heading.toFloat(), ctr) {
            for (i in 0 until 72) {
                val a = Math.toRadians(i * 5.0); val len = if (i % 18 == 0) 30f else if (i % 2 == 0) 16f else 8f
                val s = Offset(ctr.x + (r - 12f) * sin(a).toFloat(), ctr.y - (r - 12f) * cos(a).toFloat())
                val e = Offset(ctr.x + (r - 12f - len) * sin(a).toFloat(), ctr.y - (r - 12f - len) * cos(a).toFloat())
                drawLine(if (i == 0) Color(0xFFC0392B) else c.onSurfaceVariant, s, e, if (i % 18 == 0) 5f else 2f)
            }
            val native = drawContext.canvas.nativeCanvas
            val labelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = c.onSurface.toArgb(); textAlign = android.graphics.Paint.Align.CENTER; textSize = r * .105f; typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            cardinal.forEachIndexed { index, label ->
                val a = Math.toRadians(index * 90.0)
                native.drawText(label, ctr.x + (r - 57f) * sin(a).toFloat(), ctr.y - (r - 57f) * cos(a).toFloat() + labelPaint.textSize / 3f, labelPaint)
            }
            // north triangle
            val n = Path().apply { moveTo(ctr.x, ctr.y - r + 50f); lineTo(ctr.x - 14f, ctr.y - r + 78f); lineTo(ctr.x + 14f, ctr.y - r + 78f); close() }
            drawPath(n, Color(0xFFC0392B))
            rotate(target.toFloat(), ctr) {
                // qibla needle with original Kaaba-cube marker
                drawLine(if (facing) Gold else c.primary, ctr, Offset(ctr.x, ctr.y - r + 96f), 10f)
                val k = 26f; val top = ctr.y - r + 96f - k * 2
                drawRect(Color(0xFF1B1C1A), Offset(ctr.x - k, top), androidx.compose.ui.geometry.Size(k * 2, k * 2))
                drawRect(Gold, Offset(ctr.x - k, top + k * 0.45f), androidx.compose.ui.geometry.Size(k * 2, k * 0.25f))
            }
        }
        drawCircle(c.primary, 14f, ctr)
    }
}
