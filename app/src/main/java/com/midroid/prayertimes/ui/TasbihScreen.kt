package com.midroid.prayertimes.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.midroid.prayertimes.R
import com.midroid.prayertimes.data.TasbihRepo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** The only built-in tasbih phrases supported by the counter. */
val TasbihPresets = listOf(
    "سُبْحَانَ اللَّهِ",
    "الْحَمْدُ لِلَّهِ",
    "اللَّهُ أَكْبَرُ",
    "لَا إِلَهَ إِلَّا اللَّهُ",
    "أَسْتَغْفِرُ اللَّهَ",
    "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",
)

/** Interactive bead counter with daily and lifetime totals. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(onBack: () -> Unit, onVolumeTap: ((() -> Unit) -> Unit) = {}) {
    val context = LocalContext.current
    val repository = remember { TasbihRepo(context) }
    val haptics = LocalHapticFeedback.current
    var selectedPhrase by remember { mutableStateOf(TasbihPresets.first()) }
    var customPhrase by remember { mutableStateOf(repository.custom()) }
    var target by remember { mutableIntStateOf(33) }
    var count by remember { mutableIntStateOf(repository.count(selectedPhrase)) }
    var showResetDialog by remember { mutableStateOf(false) }
    var volumeCount by remember {
        mutableStateOf(context.getSharedPreferences("tasbih", 0).getBoolean("volume_count", false))
    }

    fun increment() {
        count += 1
        repository.add(selectedPhrase)
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (count % target == 0) vibrate(context)
    }
    SideEffect { onVolumeTap { increment() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tasbih)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = { showResetDialog = true }, modifier = Modifier.testTag("tasbih_reset")) {
                        Text(stringResource(R.string.reset))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PresetPicker(selectedPhrase, onSelect = { phrase ->
                selectedPhrase = phrase
                count = repository.count(phrase)
            })
            OutlinedTextField(
                value = customPhrase,
                onValueChange = { customPhrase = it; repository.saveCustom(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.custom_dhikr)) },
                singleLine = true,
            )
            if (customPhrase.isNotBlank()) {
                AssistChip(
                    onClick = { selectedPhrase = customPhrase; count = repository.count(customPhrase) },
                    label = { Text(customPhrase, fontFamily = ArabicText) },
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.volume_count), Modifier.weight(1f))
                Switch(
                    checked = volumeCount,
                    onCheckedChange = { enabled ->
                        volumeCount = enabled
                        context.getSharedPreferences("tasbih", 0).edit().putBoolean("volume_count", enabled).apply()
                    },
                )
            }
            Text(
                selectedPhrase,
                modifier = Modifier.padding(20.dp),
                fontFamily = ArabicText,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
            )
            TargetPicker(target, onTargetChange = { target = it })
            BeadCounter(count = count, onTap = ::increment, modifier = Modifier.weight(1f).fillMaxWidth())
            Text(
                "${stringResource(R.string.today_count)} ${repository.count(selectedPhrase)} · " +
                    "${stringResource(R.string.all_time)} ${repository.total(selectedPhrase)}",
            )
        }
    }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset)) },
            confirmButton = {
                TextButton(onClick = { count = 0; repository.resetToday(selectedPhrase); showResetDialog = false }) {
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun PresetPicker(selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(TasbihPresets) { phrase ->
            FilterChip(
                selected = phrase == selected,
                onClick = { onSelect(phrase) },
                label = { Text(phrase, fontFamily = ArabicText, maxLines = 1) },
            )
        }
    }
}

@Composable
private fun TargetPicker(target: Int, onTargetChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(33, 100).forEach { preset ->
            FilterChip(
                selected = target == preset,
                onClick = { onTargetChange(preset) },
                label = { Text(preset.toString()) },
            )
        }
        OutlinedTextField(
            value = target.toString(),
            onValueChange = { it.toIntOrNull()?.takeIf { value -> value > 0 }?.let(onTargetChange) },
            modifier = Modifier.width(90.dp),
            label = { Text(stringResource(R.string.target)) },
            singleLine = true,
        )
    }
}

@Composable
private fun BeadCounter(count: Int, onTap: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.clickable(onClick = onTap).testTag("tasbih_tap"), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(260.dp)) {
            val radius = size.minDimension * .44f
            for (index in 0 until 33) {
                val angle = (index * 2 * PI / 33 - PI / 2).toFloat()
                val point = Offset(size.width / 2 + radius * cos(angle), size.height / 2 + radius * sin(angle))
                val active = index == count % 33
                drawCircle(if (active) Color(0xFFE9B949) else Color(0xFF9A6B00), if (active) 11f else 7f, point)
            }
            drawCircle(Color(0x33207A72), radius, style = Stroke(3f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), fontSize = 56.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.count_tap))
        }
    }
}

private fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(VibratorManager::class.java).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (vibrator.hasVibrator()) {
        vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
