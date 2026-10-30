package com.midroid.prayertimes.data

import android.content.Context
import org.json.JSONObject

data class HisnItem(
    val id: String,
    val text: String,
    val count: Int,
    val ref: String,
    val surah: Int? = null,
    val from: Int? = null,
    val to: Int? = null,
    val chapter: String? = null,
)

data class HisnCategory(val id: String, val title: String, val icon: String, val items: List<HisnItem>)
data class DuasSection(val id: String, val items: List<HisnItem>)
data class DuasData(val notice: String, val source: String, val sections: List<DuasSection>)

/** Loads Hisn and dua entries from the bundled, authoritative JSON assets. */
object HisnRepo {
    fun load(context: Context): List<HisnCategory> {
        val adhkar = Adhkar.load(context)
        val morning = adhkar.filter { it.morning }.map { HisnItem(it.id.toString(), it.text, it.count, it.ref) }
        val evening = adhkar.filter { it.evening }.map { HisnItem(it.id.toString(), it.text, it.count, it.ref) }
        val root = JSONObject(context.assets.open("hisn.json").bufferedReader().use { it.readText() })
        val categories = root.getJSONArray("categories")
        val otherCategories = List(categories.length()) { categories.getJSONObject(it) }
            .filterNot { it.optString("id") in setOf("h26", "h27") }
            .map { category ->
                val entries = category.getJSONArray("items")
                val items = List(entries.length()) { index ->
                    entries.getJSONObject(index).let { entry ->
                        HisnItem(
                            id = entry.getString("id"),
                            text = entry.getString("text"),
                            count = entry.optInt("count", 1),
                            ref = entry.optString("ref"),
                        )
                    }
                }
                HisnCategory(category.getString("id"), category.getString("title"), category.optString("icon", "dua"), items)
            }
        return listOf(
            HisnCategory("morning", "الصباح", "morning", morning),
            HisnCategory("evening", "المساء", "evening", evening),
        ) + otherCategories
    }

    fun loadDuas(context: Context): DuasData {
        val root = JSONObject(context.assets.open("duas.json").bufferedReader().use { it.readText() })
        val sections = root.getJSONArray("sections")
        val parsedSections = List(sections.length()) { sections.getJSONObject(it) }.map { section ->
            val entries = section.getJSONArray("items")
            val items = List(entries.length()) { index ->
                entries.getJSONObject(index).let { entry ->
                    HisnItem(
                        id = entry.getString("id"),
                        text = entry.getString("text"),
                        count = entry.optInt("count", 1),
                        ref = entry.optString("ref"),
                        surah = entry.optInt("surah").takeIf { entry.has("surah") },
                        from = entry.optInt("from").takeIf { entry.has("from") },
                        to = entry.optInt("to").takeIf { entry.has("to") },
                        chapter = entry.optString("chapter").takeIf { entry.has("chapter") },
                    )
                }
            }
            DuasSection(section.getString("id"), items)
        }
        return DuasData(root.optString("quran_notice"), root.optString("quran_source"), parsedSections)
    }
}

/** Normalizes Arabic text for diacritic-insensitive search. */
fun normalizeArabic(value: String): String = value.lowercase()
    .filterNot { it in '\u064b'..'\u0652' || it == '\u0670' || it == 'ـ' }
    .map { character ->
        when (character) {
            'أ', 'إ', 'آ', 'ٱ' -> 'ا'
            'ى' -> 'ي'
            else -> character
        }
    }
    .joinToString("")
