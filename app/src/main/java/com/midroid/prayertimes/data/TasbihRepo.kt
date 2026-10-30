package com.midroid.prayertimes.data

import android.content.Context
import com.midroid.prayertimes.PrayerApp
import org.json.JSONObject
import java.time.LocalDate

/** Stores per-day and lifetime counts for the selected tasbih. */
class TasbihRepo(context: Context) {
    private val preferences = context.getSharedPreferences("tasbih", Context.MODE_PRIVATE)

    private fun resetForNewDay() {
        val today = LocalDate.now(PrayerApp.clock).toString()
        if (preferences.getString("date", null) != today) {
            preferences.edit().putString("date", today).putString("today_counts", "{}").apply()
        }
    }

    fun count(text: String): Int {
        resetForNewDay()
        return runCatching { JSONObject(preferences.getString("today_counts", "{}")).optInt(text, 0) }
            .getOrDefault(0)
    }

    fun total(text: String): Int = preferences.getInt("total_${text.hashCode()}", 0)

    fun add(text: String) {
        val counts = JSONObject(preferences.getString("today_counts", "{}") ?: "{}")
            .put(text, count(text) + 1)
        preferences.edit()
            .putString("today_counts", counts.toString())
            .putInt("total_${text.hashCode()}", total(text) + 1)
            .apply()
    }

    fun resetToday(text: String) {
        val counts = JSONObject(preferences.getString("today_counts", "{}") ?: "{}").put(text, 0)
        preferences.edit().putString("today_counts", counts.toString()).apply()
    }

    fun custom(): String = preferences.getString("custom", "").orEmpty()

    fun saveCustom(value: String) {
        preferences.edit().putString("custom", value).apply()
    }
}
