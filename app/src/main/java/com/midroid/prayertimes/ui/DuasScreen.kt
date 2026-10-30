package com.midroid.prayertimes.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.midroid.prayertimes.R
import com.midroid.prayertimes.data.DhikrProgress
import com.midroid.prayertimes.data.DuasData
import com.midroid.prayertimes.data.HisnItem
import com.midroid.prayertimes.data.HisnRepo

/** Category browser and reader for Quranic and transmitted duas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuasScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val data = remember { HisnRepo.loadDuas(context) }
    val progress = remember { DhikrProgress(context) }
    var section by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf<HisnItem?>(null) }
    val titles = mapOf(
        "quran" to stringResource(R.string.duas_quran_title),
        "sunnah" to stringResource(R.string.duas_sunnah_title),
        "ruqyah" to stringResource(R.string.duas_ruqyah_title),
        "favorites" to stringResource(R.string.favorites),
    )
    val currentTitle = selectedItem?.let { duasReference(it, context) }
        ?: section?.let { titles[it] }
        ?: stringResource(R.string.duas_title)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            selectedItem != null -> selectedItem = null
                            section != null -> section = null
                            else -> onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = context.getString(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        when {
            section == null -> CategoryGrid(titles, onSelect = { section = it }, modifier = Modifier.padding(padding))
            selectedItem == null -> DuaList(
                data = data,
                section = section!!,
                progress = progress,
                onSelect = { selectedItem = it },
                modifier = Modifier.padding(padding),
            )
            else -> DuaDetail(
                item = selectedItem!!,
                progress = progress,
                context = context,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    titles: Map<String, String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sections = listOf("quran", "sunnah", "ruqyah", "favorites")
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(sections) { id ->
            Card(
                Modifier.fillMaxWidth().height(112.dp).clickable { onSelect(id) }.testTag("duas_$id"),
            ) {
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(titles[id].orEmpty(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DuaList(
    data: DuasData,
    section: String,
    progress: DhikrProgress,
    onSelect: (HisnItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = if (section == "favorites") {
        data.sections.flatMap { it.items }.filter { progress.isFavorite(it.id) }
    } else {
        data.sections.firstOrNull { it.id == section }?.items.orEmpty()
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(entries, key = { it.id }) { entry ->
            Card(Modifier.fillMaxWidth().clickable { onSelect(entry) }) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        entry.text,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = ArabicText,
                        fontSize = 20.sp,
                        lineHeight = 40.sp,
                        textAlign = TextAlign.Right,
                        maxLines = 4,
                    )
                    Text(duasReference(entry, LocalContext.current), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        if (section == "quran") {
            item { Text(data.notice, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall) }
            item { Text(stringResource(R.string.quran_credit), Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodySmall) }
            item { Text(data.source, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun DuaDetail(
    item: HisnItem,
    progress: DhikrProgress,
    context: Context,
    modifier: Modifier = Modifier,
) {
    var refresh by remember(item.id) { mutableIntStateOf(0) }
    val clipboard = LocalClipboardManager.current
    val favorite = progress.isFavorite(item.id)
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text(
            item.text,
            Modifier.fillMaxWidth(),
            fontFamily = ArabicText,
            fontSize = 26.sp,
            lineHeight = 48.sp,
            textAlign = TextAlign.Right,
        )
        Spacer(Modifier.height(16.dp))
        Text(duasReference(item, context), Modifier.fillMaxWidth(), textAlign = TextAlign.End)
        item.chapter?.let { Text(it, Modifier.fillMaxWidth(), textAlign = TextAlign.End) }
        Row {
            IconButton(onClick = { clipboard.setText(AnnotatedString(item.text)) }) {
                Icon(AppIcons.Copy, contentDescription = stringResource(R.string.copy))
            }
            IconButton(onClick = {
                val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, item.text)
                context.startActivity(Intent.createChooser(send, null))
            }) { Icon(Icons.Default.Share, contentDescription = null) }
            IconButton(
                onClick = { progress.setFavorite(item.id, !favorite); refresh++ },
                modifier = Modifier.testTag("fav_${item.id}"),
            ) {
                Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
            }
        }
    }
}

private fun duasReference(item: HisnItem, context: Context): String {
    val surah = item.surah ?: return item.ref
    val arabic = context.resources.configuration.locales[0].language == "ar"
    val names = if (arabic) SurahArabic else SurahEnglish
    val from = item.from ?: 0
    val to = item.to ?: from
    val name = names.getOrNull(surah - 1) ?: surah.toString()
    return if (from == to) context.getString(R.string.surah_ref_one, name, from) else context.getString(R.string.surah_ref, name, from, to)
}
