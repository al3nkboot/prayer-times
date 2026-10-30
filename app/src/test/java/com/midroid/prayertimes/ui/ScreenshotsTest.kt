package com.midroid.prayertimes.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.midroid.prayertimes.MainActivity
import com.midroid.prayertimes.PrayerApp
import com.midroid.prayertimes.data.ThemeMode
import com.midroid.prayertimes.data.QuranPrefs
import com.midroid.prayertimes.data.QuranReadingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

abstract class ShotsBase {
    init {
        MainActivity.ticking = false
        MainActivity.askOnStart = false
        org.robolectric.shadows.ShadowAlarmManager.setCanScheduleExactAlarms(true)
        org.robolectric.Shadows.shadowOf(androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>())
            .grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        // Friday 16 Oct 2026, 14:25 Muscat (between Dhuhr/Jumuah and Asr)
        PrayerApp.clock = Clock.fixed(ZonedDateTime.of(2026, 10, 16, 14, 25, 37, 0, ZoneId.of("Asia/Muscat")).toInstant(), ZoneId.of("Asia/Muscat"))
    }
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val dir = File("build/screenshots").apply { mkdirs() }
    fun shot(name: String) {
        rule.waitForIdle()
        val v = rule.activity.window.decorView
        val bmp = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { v.draw(Canvas(bmp)) }
        File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    fun theme(t: ThemeMode) = rule.runOnUiThread { (rule.activity.application as PrayerApp).settings.update { it.copy(theme = t) } }
    fun waitForQuranIndex() {
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("surah_1").fetchSemanticsNodes().isNotEmpty() }
    }
    fun readingTheme(theme: QuranReadingTheme) {
        rule.runOnUiThread { QuranPrefs(rule.activity).readingTheme = theme }
    }
}

@RunWith(AndroidJUnit4::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "ar-w393dp-h852dp-xhdpi")
class ScreenshotsArTest : ShotsBase() {
    @Test fun render() {
        theme(ThemeMode.LIGHT); shot("01_home_ar_light"); shot("20_home_pro_ar_light")
        theme(ThemeMode.DARK); shot("02_home_ar_dark"); shot("21_home_pro_ar_dark")
        theme(ThemeMode.LIGHT)
        rule.onNodeWithTag("city").performClick(); shot("03_city_picker_ar")
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithTag("settings").performClick(); shot("04_settings_notifications_ar")
        rule.onNodeWithText("English").performScrollTo(); shot("18_settings_language_ar")
        rule.onNodeWithTag("choose_file").performScrollTo(); shot("15_settings_sound_ar")
        rule.onNodeWithTag("about").performScrollTo().performClick(); shot("16_about_ar")
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
    }
}

@RunWith(AndroidJUnit4::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "ar-w393dp-h852dp-xhdpi")
class ScreenshotsExactTest : ShotsBase() {
    @Test fun render() {
        org.robolectric.shadows.ShadowAlarmManager.setCanScheduleExactAlarms(false)
        theme(ThemeMode.LIGHT)
        rule.activity.runOnUiThread { rule.activity.recreate() }
        rule.waitForIdle()
        shot("06_home_inexact_note_ar")
        rule.onNodeWithText("تفعيل").performClick(); shot("07_exact_alarm_explainer_ar")
    }
}

@RunWith(AndroidJUnit4::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "en-w393dp-h852dp-xhdpi")
class ScreenshotsEnTest : ShotsBase() {
    @Test fun render() {
        theme(ThemeMode.LIGHT); shot("05_home_en_light"); shot("25_home_pro_en")
        rule.onNodeWithTag("qibla").performClick(); shot("26_qibla_en"); rule.onNodeWithTag("tab_sun").performClick(); shot("27_qibla_sun_en"); rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithTag("settings").performClick()
        rule.onNodeWithTag("about").performScrollTo().performClick(); shot("17_about_en")
    }
}

@RunWith(AndroidJUnit4::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "ar-w393dp-h852dp-xhdpi")
class ScreenshotsQiblaTest : ShotsBase() {
    @Test fun render() {
        theme(ThemeMode.LIGHT)
        QiblaSensorOverride.heading = Heading(220.0, lowAccuracy = true)
        rule.onNodeWithTag("qibla").performClick(); shot("08_qibla_turn_lowaccuracy_ar"); shot("22_qibla_ar_light")
        rule.onNodeWithTag("tab_sun").performClick(); shot("24_qibla_sun_ar"); rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        QiblaSensorOverride.heading = Heading(261.0, lowAccuracy = false)
        rule.onNodeWithTag("qibla").performClick(); shot("09_qibla_facing_ar")
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        QiblaSensorOverride.heading = null; QiblaSensorOverride.forceNoSensor = true
        rule.onNodeWithTag("qibla").performClick(); shot("10_qibla_no_sensor_ar")
        QiblaSensorOverride.forceNoSensor = false
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        theme(ThemeMode.DARK)
        QiblaSensorOverride.heading = Heading(261.0, lowAccuracy = false)
        rule.onNodeWithTag("qibla").performClick(); shot("11_qibla_facing_ar_dark"); shot("23_qibla_ar_dark")
        rule.onNodeWithTag("tab_sun").performClick()
        QiblaSensorOverride.heading = null
    }
}

@RunWith(AndroidJUnit4::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "ar-w393dp-h852dp-xhdpi")
class ScreenshotsAdhkarTest : ShotsBase() {
    @Test fun render() {
        theme(ThemeMode.LIGHT)
        rule.onNodeWithTag("adhkar").performScrollTo(); shot("22_home_adhkar_button_ar")
        rule.onNodeWithTag("adhkar").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hisn_cat_morning").performClick(); rule.onNodeWithTag("hisn_counter").performClick(); shot("19_adhkar_morning_ar")
        rule.onNodeWithContentDescription("رجوع").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hisn_cat_evening").performScrollTo()
        rule.onNodeWithTag("hisn_cat_evening").performClick(); shot("20_adhkar_evening_ar")
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle(); rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }; rule.waitForIdle()
        rule.onNodeWithTag("settings").performClick()
        rule.onNodeWithTag("auto_silent").performScrollTo().performClick()
        rule.onNodeWithTag("adhkar_am").performScrollTo().performClick()
        rule.onNodeWithTag("adhkar_pm").performScrollTo().performClick()
        rule.onNodeWithTag("auto_silent").performScrollTo(); shot("21_settings_auto_silent_ar")
    }
}

@RunWith(AndroidJUnit4::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "ar-w393dp-h852dp-xhdpi")
class Phase2ArabicShots : ShotsBase() {
 @Test fun render(){
  theme(ThemeMode.LIGHT);shot("p2_home_ar_light")
  rule.onNodeWithTag("adhkar").performScrollTo().performClick();rule.waitForIdle();shot("p2_hisn_list_ar_light")
  rule.onNodeWithTag("hisn_cat_morning").performScrollTo().performClick();rule.waitForIdle();shot("p2_hisn_detail_ar_light")
  rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle()
  rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle()
  theme(ThemeMode.DARK);rule.onNodeWithTag("adhkar").performScrollTo().performClick();rule.waitForIdle();shot("p2_hisn_list_ar_dark");rule.onNodeWithTag("hisn_cat_morning").performScrollTo().performClick();rule.waitForIdle();shot("p2_hisn_detail_ar_dark")
  rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle();rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle();rule.onNodeWithTag("tasbih_tile").performScrollTo().performClick();rule.waitForIdle();shot("p2_tasbih_ar_dark")
  rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle();theme(ThemeMode.LIGHT);rule.onNodeWithTag("tasbih_tile").performScrollTo().performClick();rule.waitForIdle();shot("p2_tasbih_ar_light")
  rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle();rule.onNodeWithTag("duas_tile").performScrollTo().performClick();rule.waitForIdle();shot("p2_duas_ar_light");rule.onNodeWithTag("duas_quran").performScrollTo().performClick();rule.waitForIdle();shot("p2_duas_quran_ar_light");rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle();rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle();theme(ThemeMode.DARK);rule.onNodeWithTag("duas_tile").performScrollTo().performClick();rule.waitForIdle();shot("p2_duas_ar_dark")
 }
}
@RunWith(AndroidJUnit4::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "en-w393dp-h852dp-xhdpi")
class Phase2EnglishShots : ShotsBase() {
 @Test fun render(){theme(ThemeMode.LIGHT);shot("p2_home_en");rule.onNodeWithTag("adhkar").performScrollTo().performClick();rule.waitForIdle();shot("p2_hisn_list_en");rule.onNodeWithTag("hisn_cat_morning").performScrollTo().performClick();rule.waitForIdle();shot("p2_hisn_detail_en");rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle();rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle();rule.onNodeWithTag("tasbih_tile").performScrollTo().performClick();rule.waitForIdle();shot("p2_tasbih_en");rule.activity.runOnUiThread{rule.activity.onBackPressedDispatcher.onBackPressed()};rule.waitForIdle();rule.onNodeWithTag("duas_tile").performScrollTo().performClick();rule.waitForIdle();shot("p2_duas_en");rule.onNodeWithTag("duas_quran").performScrollTo().performClick();rule.waitForIdle();shot("p2_duas_quran_en")}
}

@RunWith(AndroidJUnit4::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "ar-w393dp-h852dp-xhdpi")
class Phase3ArabicShots : ShotsBase() {
    @Test fun render() {
        theme(ThemeMode.LIGHT)
        rule.onNodeWithTag("adhkar").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hisn_cat_morning").performClick()
        rule.waitForIdle()
        shot("p3_hisn_detail_ar_light")
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        theme(ThemeMode.DARK)
        rule.onNodeWithTag("adhkar").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("hisn_cat_morning").performClick()
        rule.waitForIdle()
        shot("p3_hisn_detail_ar_dark")
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        theme(ThemeMode.LIGHT)

        rule.onNodeWithTag("quran_tile").performScrollTo().performClick()
        rule.waitForIdle()
        waitForQuranIndex()
        shot("p3_quran_index_ar_light")
        rule.onNodeWithTag("quran_search").performClick()
        val quran = com.midroid.prayertimes.data.QuranRepo.load(rule.activity)
        rule.onNodeWithTag("quran_search").performTextInput(quran.ayah(1, 2)!!.text.split(Regex("\\s+")).take(4).joinToString(" "))
        rule.waitForIdle()
        shot("p3_quran_search_ar")
        rule.onNodeWithTag("quran_search").performTextClearance()
        rule.onNodeWithTag("quran_tab_surahs").assertExists()
        rule.onNodeWithTag("quran_tab_surahs").performClick()
        rule.onNodeWithTag("quran_tab_juz").performClick()
        rule.waitForIdle()
        shot("p3_quran_juz_ar_light"); rule.onNodeWithTag("quran_tab_surahs").performClick(); rule.waitForIdle()
        waitForQuranIndex()
        readingTheme(QuranReadingTheme.DEFAULT)
        rule.onNodeWithTag("surah_1").performScrollTo().performClick()
        rule.waitForIdle()
        shot("p3_quran_reader_fatiha_ar_light")
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        readingTheme(QuranReadingTheme.DEFAULT)
        rule.onNodeWithTag("surah_2").performScrollTo().performClick()
        rule.waitForIdle()
        rule.waitUntil(3000) {
            com.midroid.prayertimes.data.QuranPrefs(rule.activity).lastRead() != null
        }
        shot("p3_quran_reader_baqarah_ar_light")
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        shot("p3_home_continue_ar_light")
        rule.onNodeWithTag("duas_tile").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("duas_quran").performClick()
        rule.waitForIdle()
        shot("p3_duas_quran_ar_light")

        theme(ThemeMode.DARK)
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.onNodeWithTag("quran_tile").performScrollTo().performClick()
        rule.waitForIdle()
        waitForQuranIndex()
        shot("p3_quran_index_ar_dark")
        readingTheme(QuranReadingTheme.NIGHT)
        rule.onNodeWithTag("surah_1").performScrollTo().performClick()
        rule.waitForIdle()
        shot("p3_quran_reader_ar_dark")
        rule.activity.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        theme(ThemeMode.LIGHT)
        readingTheme(QuranReadingTheme.SEPIA)
        rule.onNodeWithTag("surah_1").performScrollTo().performClick()
        rule.waitForIdle()
        shot("p3_quran_reader_ar_sepia")
    }
}

@RunWith(AndroidJUnit4::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "en-w393dp-h852dp-xhdpi")
class Phase3EnglishShots : ShotsBase() {
    @Test fun render() {
        theme(ThemeMode.LIGHT)
        shot("p3_home_en")
        rule.onNodeWithTag("quran_tile").performScrollTo().performClick()
        rule.waitForIdle()
        waitForQuranIndex()
        shot("p3_quran_index_en")
        readingTheme(QuranReadingTheme.DEFAULT)
        rule.onNodeWithTag("surah_1").performScrollTo().performClick()
        rule.waitForIdle()
        shot("p3_quran_reader_en")
    }
}
