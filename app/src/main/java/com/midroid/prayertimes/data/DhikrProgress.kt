package com.midroid.prayertimes.data

import android.content.Context
import com.midroid.prayertimes.PrayerApp
import java.time.Clock
import java.time.LocalDate

/** Persists daily completion counts and favourites for dhikr entries. */
class DhikrProgress(context: Context, private val clock: Clock = PrayerApp.clock) {
    private val preferences = context.getSharedPreferences("dhikr_progress", Context.MODE_PRIVATE)

    private fun resetForNewDay() {
        val today = LocalDate.now(clock).toString()
        if (preferences.getString("date", null) != today) {
            preferences.edit().apply {
                preferences.all.keys.filter { it.startsWith("c_") }.forEach { key -> remove(key) }
                putString("date", today)
            }.apply()
        }
    }

    fun count(key: String): Int {
        resetForNewDay()
        return preferences.getInt("c_$key", 0)
    }

    fun increment(itemKey: String, max: Int): Int {
        resetForNewDay()
        val updated = (count(itemKey) + 1).coerceAtMost(max)
        preferences.edit().putInt("c_$itemKey", updated).apply()
        return updated
    }

    fun reset(category: HisnCategory) {
        resetForNewDay()
        preferences.edit().apply {
            category.items.forEach { remove("c_${it.id}") }
        }.apply()
    }

    fun progress(category: HisnCategory): Pair<Int, Int> =
        category.items.count { count(it.id) >= it.count } to category.items.size

    fun isFavorite(key: String): Boolean = favorites().contains(key)

    fun setFavorite(key: String, value: Boolean) {
        val updated = favorites().toMutableSet().apply {
            if (value) add(key) else remove(key)
        }
        preferences.edit().putStringSet("favorites", updated).apply()
    }

    fun favorites(): Set<String> = preferences.getStringSet("favorites", emptySet()).orEmpty().toSet()
}
