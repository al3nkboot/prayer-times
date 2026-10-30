package com.midroid.prayertimes.data

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

data class Surah(val index: Int, val nameAr: String, val tname: String, val ename: String, val type: String, val ayahCount: Int)
data class Ayah(val surah: Int, val number: Int, val text: String)
data class QuranStart(val index: Int, val surah: Int, val ayah: Int)
data class QuranData(val surahs: List<Surah>, val ayahs: List<Ayah>, val juz: List<QuranStart>, val quarters: List<QuranStart>) {
    private val byRef = ayahs.associateBy { it.surah to it.number }
    private val bySurah = ayahs.groupBy { it.surah }
    fun ayah(surah: Int, number: Int): Ayah? = byRef[surah to number]
    fun ayahs(surah: Int): List<Ayah> = bySurah[surah].orEmpty()
    fun surah(index: Int): Surah? = surahs.getOrNull(index - 1)
    fun displayed(ayah: Ayah): String {
        if (ayah.number != 1 || ayah.surah == 1 || ayah.surah == 9) return ayah.text
        return ayah.text.split(Regex("\\s+"), limit = 5).drop(4).joinToString(" ")
    }
    fun hasBasmalaHeader(surah: Int): Boolean = surah != 1 && surah != 9
    fun search(query: String, limit: Int = 200): List<Ayah> {
        val normalized = normalizeQuranSearch(query)
        if (normalized.isBlank()) return emptyList()
        return ayahs.asSequence().filter { normalizeQuranSearch(it.text).contains(normalized) }.take(limit).toList()
    }
}

object QuranRepo {
    @Volatile private var cached: QuranData? = null
    fun load(context: Context): QuranData = cached ?: synchronized(this) {
        cached ?: parse(context).also { cached = it }
    }
    private fun parse(context: Context): QuranData {
        val ayahs = context.assets.open("quran/quran-uthmani.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter { it.isNotBlank() && !it.startsWith("#") }.map { line ->
                val parts = line.split('|', limit = 3)
                Ayah(parts[0].toInt(), parts[1].toInt(), parts[2])
            }.toList()
        }
        val parser = Xml.newPullParser()
        val xml = context.assets.open("quran/quran-data.xml").bufferedReader(Charsets.UTF_8).use { it.readText() }
        parser.setInput(java.io.StringReader(xml))
        val surahs = mutableListOf<Surah>()
        val juz = mutableListOf<QuranStart>()
        val quarters = mutableListOf<QuranStart>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "sura" -> surahs += Surah(parser.getAttributeValue(null, "index").toInt(), parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "tname"), parser.getAttributeValue(null, "ename"), parser.getAttributeValue(null, "type"), parser.getAttributeValue(null, "ayas").toInt())
                    "juz" -> juz += QuranStart(parser.getAttributeValue(null, "index").toInt(), parser.getAttributeValue(null, "sura").toInt(), parser.getAttributeValue(null, "aya").toInt())
                    "quarter" -> quarters += QuranStart(parser.getAttributeValue(null, "index").toInt(), parser.getAttributeValue(null, "sura").toInt(), parser.getAttributeValue(null, "aya").toInt())
                }
            }
            event = parser.next()
        }
        return QuranData(surahs, ayahs, juz, quarters)
    }
}

fun normalizeQuranSearch(value: String): String = buildString {
    value.lowercase(Locale.ROOT).forEach { char ->
        val code = char.code
        if (code == 0x0640 || code in 0x0610..0x061A || code in 0x064B..0x065F || code == 0x0670 || code in 0x06D6..0x06ED) return@forEach
        append(when (char) {
            'أ', 'إ', 'آ', 'ٱ' -> 'ا'
            'ى' -> 'ي'
            'ة' -> 'ه'
            else -> char
        })
    }
}.replace(Regex("\\s+"), " ").trim()
