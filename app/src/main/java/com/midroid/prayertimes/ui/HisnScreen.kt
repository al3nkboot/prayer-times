package com.midroid.prayertimes.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.vector.ImageVector
import com.midroid.prayertimes.R
import com.midroid.prayertimes.data.DhikrProgress
import com.midroid.prayertimes.data.HisnCategory
import com.midroid.prayertimes.data.HisnItem
import com.midroid.prayertimes.data.HisnRepo
import com.midroid.prayertimes.data.normalizeArabic

/** Hisn category browser and repetition counter. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisnScreen(
    initialCategory: Int? = null,
    onInitialCategoryConsumed: () -> Unit = {},
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val categories = remember { HisnRepo.load(context) }
    val progress = remember { DhikrProgress(context) }
    var selected by remember { mutableStateOf<HisnCategory?>(null) }
    var query by remember { mutableStateOf("") }
    var fontSize by remember {
        mutableIntStateOf(context.getSharedPreferences("ui", 0).getInt("arabic_size", 24))
    }

    LaunchedEffect(initialCategory, categories) {
        initialCategory?.let { selected = categories.getOrNull(it) }
        if (initialCategory != null) onInitialCategoryConsumed()
    }
    BackHandler { if (selected != null) selected = null else onBack() }
    val screenTitle = if (selected == null) {
        stringResource(R.string.hisn_title)
    } else {
        categoryTitle(selected!!)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(screenTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = { if (selected != null) selected = null else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = context.getString(R.string.back))
                    }
                },
                actions = {
                    if (selected != null) {
                        TextButton(onClick = { fontSize = (fontSize - 1).coerceAtLeast(16); saveFontSize(context, fontSize) }) { Text("A−") }
                        TextButton(onClick = { fontSize = (fontSize + 1).coerceAtMost(34); saveFontSize(context, fontSize) }) { Text("A+") }
                    }
                },
            )
        },
    ) { padding ->
        if (selected == null) {
            CategoryList(
                categories = categories,
                progress = progress,
                query = query,
                onQueryChange = { query = it },
                onFavoritesClick = {
                    selected = HisnCategory(
                        id = "favorites",
                        title = context.getString(R.string.favorites),
                        icon = "dua",
                        items = categories.flatMap { it.items }.filter { progress.isFavorite(it.id) },
                    )
                },
                onCategoryClick = { selected = it },
                modifier = Modifier.padding(padding),
            )
        } else {
            CategoryDetail(
                category = selected!!,
                progress = progress,
                fontSize = fontSize,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

private fun saveFontSize(context: android.content.Context, value: Int) {
    context.getSharedPreferences("ui", 0).edit().putInt("arabic_size", value).apply()
}

@Composable
private fun CategoryList(
    categories: List<HisnCategory>,
    progress: DhikrProgress,
    query: String,
    onQueryChange: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onCategoryClick: (HisnCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val favoriteItems = categories.flatMap { it.items }.filter { progress.isFavorite(it.id) }
    val filteredCategories = categories.filter { category ->
        normalizeArabic(category.title).contains(normalizeArabic(query)) ||
            category.items.any { normalizeArabic(it.text).contains(normalizeArabic(query)) }
    }
    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).testTag("hisn_search"),
            label = { Text(stringResource(R.string.search_content)) },
            singleLine = true,
        )
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FavoriteEntryCard(favoriteItems, onFavoritesClick)
            }
            itemsIndexed(filteredCategories, key = { _, category -> category.id }) { _, category ->
                CategoryCard(category, progress) { onCategoryClick(category) }
            }
        }
    }
}

@Composable
private fun FavoriteEntryCard(items: List<HisnItem>, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("hisn_favorites")) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.favorites), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Text(items.size.toString(), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CategoryCard(category: HisnCategory, progress: DhikrProgress, onClick: () -> Unit) {
    val completed = progress.progress(category)
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("hisn_cat_${category.id}")) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(iconFor(category.icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(categoryTitle(category), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text("${completed.first}/${completed.second}")
            }
            LinearProgressIndicator(
                progress = { if (completed.second == 0) 0f else completed.first.toFloat() / completed.second },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun CategoryDetail(
    category: HisnCategory,
    progress: DhikrProgress,
    fontSize: Int,
    modifier: Modifier = Modifier,
) {
    val items = category.items
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    var refresh by remember(category.id) { mutableIntStateOf(0) }
    val currentIndex = remember(refresh, items) {
        items.indexOfFirst { progress.count(it.id) < it.count }
            .let { if (it < 0) items.lastIndex else it }
    }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(items, key = { _, entry -> entry.id }) { index, entry ->
                DhikrCard(
                    item = entry,
                    count = progress.count(entry.id),
                    fontSize = fontSize,
                    isCurrent = index == currentIndex,
                    progress = progress,
                    onFavoriteChanged = { refresh++ },
                    onTap = {
                        val nextCount = progress.increment(entry.id, entry.count)
                        refresh++
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (nextCount >= entry.count && index < items.lastIndex) {
                            coroutineScope.launch { listState.animateScrollToItem(index + 1) }
                        }
                    },
                )
            }
        }
        val current = items.getOrNull(currentIndex)
        if (current != null) {
            FilledTonalButton(
                onClick = {
                    val nextCount = progress.increment(current.id, current.count)
                    refresh++
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (nextCount >= current.count && currentIndex < items.lastIndex) {
                        coroutineScope.launch { listState.animateScrollToItem(currentIndex + 1) }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                    .size(88.dp).testTag("hisn_counter"),
                shape = CircleShape,
                contentPadding = PaddingValues(8.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(progress.count(current.id).toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(stringResource(R.string.count_of_fmt, current.count), fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }

}

@Composable
private fun DhikrCard(
    item: HisnItem,
    count: Int,
    fontSize: Int,
    isCurrent: Boolean,
    progress: DhikrProgress,
    onTap: () -> Unit,
    onFavoriteChanged: () -> Unit,
) {
    var referenceExpanded by remember(item.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val favorite = progress.isFavorite(item.id)
    Card(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onTap)
            .testTag("hisn_item_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                item.text,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = ArabicText,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.8f).sp,
                textAlign = TextAlign.Right,
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { referenceExpanded = !referenceExpanded }) {
                    Text(stringResource(R.string.reference))
                    Icon(
                        if (referenceExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { clipboard.setText(AnnotatedString(item.text)) }) {
                    Icon(AppIcons.Copy, contentDescription = stringResource(R.string.copy))
                }
                IconButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, item.text)
                    context.startActivity(Intent.createChooser(send, null))
                }) { Icon(Icons.Default.Share, contentDescription = null) }
                IconButton(onClick = { progress.setFavorite(item.id, !favorite); onFavoriteChanged() }, modifier = Modifier.testTag("fav_${item.id}")) {
                    Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
                }
            }
            if (referenceExpanded) Text(item.ref, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun categoryTitle(category: HisnCategory): String = when (category.id) {
    "morning" -> stringResource(R.string.morning)
    "evening" -> stringResource(R.string.evening)
    else -> category.title
}

private fun iconFor(key: String): ImageVector = when (key) {
    "morning" -> AppIcons.Morning
    "evening" -> AppIcons.Evening
    "sleep" -> AppIcons.Sleep
    "wake" -> AppIcons.Wake
    "mosque" -> AppIcons.Mosque
    "prayer" -> AppIcons.Prayer
    "water" -> AppIcons.Water
    "travel" -> AppIcons.Travel
    "food" -> AppIcons.Food
    "repent" -> AppIcons.Repent
    "rain" -> AppIcons.Rain
    "health" -> AppIcons.Health
    "grave" -> AppIcons.Grave
    "cloth" -> AppIcons.Cloth
    "home" -> AppIcons.Home
    "adhan" -> AppIcons.Adhkar
    "hajj" -> AppIcons.Kaaba
    else -> AppIcons.Duas
}
