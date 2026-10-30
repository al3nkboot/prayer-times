@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.midroid.prayertimes.ui

import android.content.Intent
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.midroid.prayertimes.R
import com.midroid.prayertimes.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(startSurah: Int = 0, startAyah: Int = 1, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { QuranPrefs(context) }
    var data by remember { mutableStateOf<QuranData?>(null) }
    var selectedSurah by remember { mutableIntStateOf(0) }
    var initialAyah by remember { mutableIntStateOf(1) }
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    var showJump by remember { mutableStateOf(false) }
    var jumpSurah by remember { mutableStateOf("1") }
    var jumpAyah by remember { mutableStateOf("1") }
    var refresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { data = withContext(Dispatchers.IO) { QuranRepo.load(context) } }
    LaunchedEffect(startSurah, startAyah, data) {
        if (startSurah in 1..114 && data != null) {
            selectedSurah = startSurah
            initialAyah = startAyah
        }
    }
    BackHandler(selectedSurah != 0) { selectedSurah = 0 }
    val isArabic = context.resources.configuration.locales[0].language == "ar"
    val clipboard = LocalClipboardManager.current
    val view = LocalView.current
    DisposableEffect(selectedSurah, view) {
        val window = (view.context as? android.app.Activity)?.window
        if (selectedSurah != 0) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    val content = data
    Scaffold(topBar = {
        TopAppBar(title = { Text(if (selectedSurah == 0) stringResource(R.string.soon_quran) else content?.surah(selectedSurah)?.nameAr.orEmpty()) }, navigationIcon = {
            IconButton(onClick = { if (selectedSurah != 0) selectedSurah = 0 else onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
        }, actions = {
            if (selectedSurah == 0) {
                IconButton(onClick = { showJump = true }) { Icon(Icons.Default.Search, null) }
                IconButton(onClick = { tab = 2 }) { Icon(Icons.Default.Star, null) }
            } else {
                TextButton(
                    onClick = { prefs.fontSize = prefs.fontSize - 1; refresh++ },
                    modifier = Modifier.testTag("quran_font_decrease")
                ) {
                    Text(stringResource(R.string.quran_font_decrease))
                }
                TextButton(
                    onClick = { prefs.fontSize = prefs.fontSize + 1; refresh++ },
                    modifier = Modifier.testTag("quran_font_increase")
                ) {
                    Text(stringResource(R.string.quran_font_increase))
                }
                TextButton(
                    onClick = {
                        prefs.readingTheme = when (prefs.readingTheme) {
                            QuranReadingTheme.DEFAULT -> QuranReadingTheme.NIGHT
                            QuranReadingTheme.NIGHT -> QuranReadingTheme.SEPIA
                            QuranReadingTheme.SEPIA -> QuranReadingTheme.DEFAULT
                        }
                        refresh++
                    },
                    modifier = Modifier.testTag("quran_theme_button")
                ) {
                    val themeLabel = when (prefs.readingTheme) {
                        QuranReadingTheme.DEFAULT -> R.string.quran_theme_default
                        QuranReadingTheme.NIGHT -> R.string.quran_theme_night
                        QuranReadingTheme.SEPIA -> R.string.quran_theme_sepia
                    }
                    Text(stringResource(themeLabel))
                }
            }
        })
    }) { padding ->
        if (content == null) return@Scaffold
        if (selectedSurah == 0) {
            Column(Modifier.padding(padding)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).testTag("quran_search"),
                    label = { Text(stringResource(R.string.quran_search)) },
                    singleLine = true
                )
                if (query.isBlank()) {
                    TabRow(selectedTabIndex = tab.coerceAtMost(2)) {
                        Tab(tab == 0, { tab = 0 }, modifier = Modifier.testTag("quran_tab_surahs"), text = { Text(stringResource(R.string.quran_tab_surahs)) })
                        Tab(tab == 1, { tab = 1 }, modifier = Modifier.testTag("quran_tab_juz"), text = { Text(stringResource(R.string.quran_tab_juz)) })
                        Tab(tab == 2, { tab = 2 }, modifier = Modifier.testTag("quran_tab_bookmarks"), text = { Text(stringResource(R.string.quran_tab_bookmarks)) })
                    }
                }
                val matches = if (query.isNotBlank()) content.search(query) else emptyList()
                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (query.isNotBlank()) items(matches, key = { "${it.surah}:${it.number}" }) { ayah ->
                        Card(Modifier.fillMaxWidth().clickable {
                            selectedSurah = ayah.surah
                            initialAyah = ayah.number
                        }) {
                            Column(Modifier.padding(14.dp)) {
                                Text(content.displayed(ayah), fontFamily = QuranText, fontSize = 20.sp, textAlign = TextAlign.End)
                                Text("${content.surah(ayah.surah)?.nameAr} ${formatDigits(ayah.number, isArabic)}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else if (tab == 2) {
                        items(prefs.bookmarks().sorted(), key = { it }) { key ->
                            val parts = key.split(":")
                            val surah = parts[0].toInt()
                            val ayah = parts[1].toInt()
                            Text(
                                "${content.surah(surah)?.nameAr} · ${formatDigits(ayah, isArabic)}",
                                Modifier.fillMaxWidth().clickable {
                                    selectedSurah = surah
                                    initialAyah = ayah
                                }.padding(14.dp)
                            )
                        }
                    } else if (tab == 0) {
                        items(content.surahs, key = { it.index }) { surah ->
                            Card(Modifier.fillMaxWidth().clickable { selectedSurah = surah.index; initialAyah = 1 }.testTag("surah_${surah.index}")) {
                                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(formatDigits(surah.index, isArabic))
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                        Text(surah.nameAr, fontWeight = FontWeight.Bold, fontFamily = QuranText)
                                        val placeName = if (isArabic) {
                                            stringResource(if (surah.type == "Meccan") R.string.quran_meccan else R.string.quran_medinan)
                                        } else {
                                            "${surah.tname} · ${surah.type}"
                                        }
                                        Text(placeName, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(stringResource(R.string.quran_ayah_count, formatDigits(surah.ayahCount, isArabic)))
                                }
                            }
                        }
                    } else {
                        items(content.juz, key = { it.index }) { juz ->
                            Card(Modifier.fillMaxWidth().clickable { selectedSurah = juz.surah; initialAyah = juz.ayah }) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(stringResource(R.string.quran_juz_fmt, formatDigits(juz.index, isArabic)), fontWeight = FontWeight.Bold)
                                    Text("${content.surah(juz.surah)?.nameAr} : ${formatDigits(juz.ayah, isArabic)}")
                                    content.quarters.filter { it.index in (juz.index - 1) * 8 + 1..juz.index * 8 }.forEach {
                                        Text(
                                            "◦ ${content.surah(it.surah)?.nameAr} ${formatDigits(it.ayah, isArabic)}",
                                            Modifier.padding(start = 12.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Reader(content, selectedSurah, initialAyah, prefs, isArabic, refresh, clipboard) { surah, ayah -> selectedSurah = surah; initialAyah = ayah }
        }
    }
    if (showJump) Dialog(onDismissRequest = { showJump = false }) {
        var error by remember { mutableStateOf(false) }
        Card { Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.quran_jump_title), fontWeight = FontWeight.Bold)
            OutlinedTextField(jumpSurah, { jumpSurah = it }, label = { Text(stringResource(R.string.quran_surah)) })
            OutlinedTextField(jumpAyah, { jumpAyah = it }, label = { Text(stringResource(R.string.quran_ayah)) })
            if (error) Text(stringResource(R.string.quran_invalid_reference), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = {
                val s = jumpSurah.toIntOrNull()
                val a = jumpAyah.toIntOrNull()
                if (s != null && a != null && content?.surah(s)?.let { a in 1..it.ayahCount } == true) {
                    selectedSurah = s
                    initialAyah = a
                    showJump = false
                } else error = true
            }) { Text(stringResource(R.string.quran_go)) }
        } }
    }
}

@Composable
private fun Reader(
    data: QuranData,
    surahIndex: Int,
    initial: Int,
    prefs: QuranPrefs,
    arabic: Boolean,
    refresh: Int,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    open: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val surah = data.surah(surahIndex) ?: return
    val ayahs = data.ayahs(surahIndex)
    val listState = rememberLazyListState()
    val theme = prefs.readingTheme
    val bg = when (theme) {
        QuranReadingTheme.DEFAULT -> MaterialTheme.colorScheme.background
        QuranReadingTheme.NIGHT -> Color(0xFF0E1412)
        QuranReadingTheme.SEPIA -> Color(0xFFF4ECD8)
    }
    val fg = when (theme) {
        QuranReadingTheme.DEFAULT -> MaterialTheme.colorScheme.onBackground
        QuranReadingTheme.NIGHT -> Color(0xFFE6E1D3)
        QuranReadingTheme.SEPIA -> Color(0xFF3B2F2F)
    }
    LaunchedEffect(surahIndex, initial) {
        listState.scrollToItem(if (initial <= 1) 0 else initial.coerceIn(1, ayahs.size))
    }
    LaunchedEffect(listState, surahIndex) {
        snapshotFlow { listState.firstVisibleItemIndex }.collectLatest { index ->
            delay(500)
            prefs.saveLastRead(surahIndex, if (index == 0) 1 else index.coerceIn(1, ayahs.size))
        }
    }
    Column(Modifier.fillMaxSize().background(bg).testTag("quran_reader")) {
        LazyColumn(state = listState, modifier = Modifier.weight(1f), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                val headerColor = if (theme == QuranReadingTheme.DEFAULT) {
                    MaterialTheme.colorScheme.surface
                } else {
                    bg
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = headerColor),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(surah.nameAr, fontFamily = QuranText, fontSize = 30.sp, color = fg)
                        val location = if (arabic) stringResource(if (surah.type == "Meccan") R.string.quran_meccan else R.string.quran_medinan) else surah.type
                        Text(stringResource(R.string.quran_ayah_count, formatDigits(surah.ayahCount, arabic)).let { "$location · $it" }, color = fg)
                        if (data.hasBasmalaHeader(surahIndex)) {
                            val basmala = data.ayah(1, 1)
                                ?.text
                                ?.split(Regex("\\s+"), limit = 5)
                                ?.take(4)
                                ?.joinToString(" ")
                                .orEmpty()
                            Text(
                                basmala,
                                fontFamily = QuranText,
                                fontSize = 27.sp,
                                textAlign = TextAlign.Center,
                                color = fg,
                                modifier = Modifier.padding(top = 14.dp)
                            )
                        }
                    }
                }
            }
        items(ayahs, key = { it.number }) { ayah ->
                val shown = data.displayed(ayah)
                var menu by remember(ayah.number) { mutableStateOf(false) }
                Column(Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = { menu = true }).padding(vertical = 6.dp).testTag("ayah_${ayah.number}")) {
                    androidx.compose.material3.Text(
                        "$shown ﴿${formatDigits(ayah.number, arabic)}﴾",
                        Modifier.fillMaxWidth(),
                        fontFamily = QuranText,
                        fontSize = prefs.fontSize.sp,
                        lineHeight = (prefs.fontSize * 1.9).sp,
                        textAlign = TextAlign.Justify,
                        style = androidx.compose.material3.LocalTextStyle.current.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Rtl),
                        color = fg
                    )
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.quran_copy)) }, onClick = { clipboard.setText(AnnotatedString(shown)); menu = false })
                        DropdownMenuItem(text = { Text(stringResource(R.string.quran_share)) }, onClick = {
                            val intent = Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(
                                    Intent.EXTRA_TEXT,
                                    "$shown — ${context.getString(R.string.quran_surah)} ${surah.nameAr}: ${formatDigits(ayah.number, arabic)}"
                                )
                            context.startActivity(Intent.createChooser(intent, null))
                            menu = false
                        })
                        val bookmarkLabel = if (prefs.isBookmarked(surahIndex, ayah.number)) {
                            R.string.quran_remove_bookmark
                        } else {
                            R.string.quran_bookmark
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(bookmarkLabel)) },
                            onClick = {
                                prefs.toggleBookmark(surahIndex, ayah.number)
                                menu = false
                            }
                        )
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(enabled = surahIndex > 1, onClick = { open(surahIndex - 1, 1) }) { Text(stringResource(R.string.quran_previous_surah)) }
                    TextButton(enabled = surahIndex < 114, onClick = { open(surahIndex + 1, 1) }) { Text(stringResource(R.string.quran_next_surah)) }
                }
            }
        }
    }
}
