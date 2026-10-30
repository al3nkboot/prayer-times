package com.midroid.prayertimes

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.midroid.prayertimes.data.QuranRepo
import com.midroid.prayertimes.data.normalizeQuranSearch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QuranRepoTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun parsesTextAndMetadata() {
        val data = QuranRepo.load(context)
        assertEquals(114, data.surahs.size)
        assertEquals(6236, data.ayahs.size)
        assertEquals(6236, data.surahs.sumOf { it.ayahCount })
        assertEquals(30, data.juz.size)
        data.surahs.forEach { surah ->
            assertEquals(surah.ayahCount, data.ayahs(surah.index).size)
        }
    }

    @Test
    fun basmalaDisplayRules() {
        val data = QuranRepo.load(context)
        val basmala = data.ayah(1, 1)!!.text
        assertFalse(data.hasBasmalaHeader(1))
        assertEquals(basmala, data.displayed(data.ayah(1, 1)!!))
        assertFalse(data.hasBasmalaHeader(9))
        assertTrue(data.hasBasmalaHeader(2))
        assertEquals(data.ayah(2, 1)!!.text.split(" ", limit = 5)[4], data.displayed(data.ayah(2, 1)!!))
        assertFalse(data.displayed(data.ayah(2, 1)!!).startsWith(basmala.split(" ")[0]))
        listOf(95, 97).forEach { surah ->
            assertTrue(data.hasBasmalaHeader(surah))
            assertEquals(data.ayah(surah, 1)!!.text.split(" ", limit = 5)[4], data.displayed(data.ayah(surah, 1)!!))
            assertEquals(4, data.ayah(surah, 1)!!.text.split(" ").size - data.displayed(data.ayah(surah, 1)!!).split(" ").size)
        }
    }

    @Test
    fun searchNormalizesArabicAndFindsAyah() {
        val data = QuranRepo.load(context)
        val expectedAyah = data.ayah(1, 2)!!
        val query = expectedAyah.text.split(Regex("\\s+")).take(4).joinToString(" ")
        assertTrue(data.search(query).any { it.surah == 1 && it.number == 2 })
        val unmarked = normalizeQuranSearch(query)
        assertTrue(data.search(unmarked).any { it.surah == 1 && it.number == 2 })
    }
}
