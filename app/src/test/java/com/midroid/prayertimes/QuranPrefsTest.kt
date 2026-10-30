package com.midroid.prayertimes

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.midroid.prayertimes.data.QuranPrefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QuranPrefsTest {
    @Test
    fun valuesPersistAcrossInstances() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        QuranPrefs(context).apply {
            toggleBookmark(2, 1)
            saveLastRead(2, 7)
        }
        val next = QuranPrefs(context)
        assertTrue(next.isBookmarked(2, 1))
        assertEquals(2, next.lastRead()?.surah)
        assertEquals(7, next.lastRead()?.ayah)
    }
}
