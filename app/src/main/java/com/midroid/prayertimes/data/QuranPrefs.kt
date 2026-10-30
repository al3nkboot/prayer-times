package com.midroid.prayertimes.data

import android.content.Context

enum class QuranReadingTheme { DEFAULT, NIGHT, SEPIA }
data class QuranPosition(val surah: Int, val ayah: Int)

class QuranPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)
    fun bookmarks(): Set<String> = prefs.getStringSet("bookmarks", emptySet()).orEmpty().toSet()
    fun isBookmarked(surah: Int, ayah: Int) = "${surah}:$ayah" in bookmarks()
    fun toggleBookmark(surah: Int, ayah: Int): Boolean {
        val key = "$surah:$ayah"
        val updated = bookmarks().toMutableSet().apply { if (!add(key)) remove(key) }
        prefs.edit().putStringSet("bookmarks", updated).apply()
        return key in updated
    }
    fun lastRead(): QuranPosition? = if (prefs.contains("last_surah")) QuranPosition(prefs.getInt("last_surah", 1), prefs.getInt("last_ayah", 1)) else null
    fun saveLastRead(surah: Int, ayah: Int) { prefs.edit().putInt("last_surah", surah).putInt("last_ayah", ayah).apply() }
    var fontSize: Int
        get() = prefs.getInt("font_size", 26).coerceIn(18, 40)
        set(value) { prefs.edit().putInt("font_size", value.coerceIn(18, 40)).apply() }
    var readingTheme: QuranReadingTheme
        get() = runCatching { QuranReadingTheme.valueOf(prefs.getString("reading_theme", "DEFAULT")!!) }.getOrDefault(QuranReadingTheme.DEFAULT)
        set(value) { prefs.edit().putString("reading_theme", value.name).apply() }
}
