package com.midroid.prayertimes
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.midroid.prayertimes.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.*
@RunWith(RobolectricTestRunner::class) @Config(sdk=[35]) class DhikrProgressTest{
 @Test fun capsResetsAndFavoritesPersist(){val c=ApplicationProvider.getApplicationContext<Context>();c.getSharedPreferences("dhikr_progress",0).edit().clear().apply();var clock=Clock.fixed(Instant.parse("2026-10-30T00:00:00Z"),ZoneOffset.UTC);val a=DhikrProgress(c,clock);assertEquals(1,a.increment("x",1));assertEquals(1,a.increment("x",1));a.setFavorite("x",true);clock=Clock.fixed(Instant.parse("2026-10-31T00:00:00Z"),ZoneOffset.UTC);val b=DhikrProgress(c,clock);assertEquals(0,b.count("x"));assertTrue(b.isFavorite("x"))}
}
