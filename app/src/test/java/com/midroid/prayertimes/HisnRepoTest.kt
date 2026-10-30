package com.midroid.prayertimes
import androidx.test.core.app.ApplicationProvider
import com.midroid.prayertimes.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import android.content.Context
import org.json.JSONObject
@RunWith(RobolectricTestRunner::class) @Config(sdk=[35]) class HisnRepoTest {
 @Test fun assetsLoadAndNormalize(){val c=ApplicationProvider.getApplicationContext<Context>();val cats=HisnRepo.load(c);assertTrue(cats.size>=120);val adh=Adhkar.load(c);assertEquals(adh.count{it.morning},cats[0].items.size);assertEquals(adh.count{it.evening},cats[1].items.size);assertFalse(cats.any{it.id=="h26"||it.id=="h27"});assertTrue(cats.flatMap{it.items}.all{it.text.isNotBlank()&&it.count>=1});val dues=HisnRepo.loadDuas(c);dues.sections.first{it.id=="quran"}.items.forEach{assertTrue(it.surah in 1..114);assertTrue(it.from!!<=it.to!!)};assertEquals("الكتاب",normalizeArabic("ٱلْكِتَابُـ"))}
}
