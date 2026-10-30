package com.midroid.prayertimes

import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

@org.junit.runner.RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
@org.robolectric.annotation.Config(sdk = [35])
class TextCorrectionsTest {
    @Test
    fun approvedCorrectionsAreApplied() {
        val correctionsFile = File("../docs/text-corrections.json").takeIf { it.isFile }
            ?: File("docs/text-corrections.json")
        val hisnFile = File("src/main/assets/hisn.json").takeIf { it.isFile }
            ?: File("app/src/main/assets/hisn.json")
        val corrections = JSONObject(correctionsFile.readText()).getJSONArray("corrections")
        val categories = JSONObject(hisnFile.readText()).getJSONArray("categories")
        val items = buildMap<String, JSONObject> {
            for (categoryIndex in 0 until categories.length()) {
                val entries = categories.getJSONObject(categoryIndex).getJSONArray("items")
                for (itemIndex in 0 until entries.length()) {
                    val item = entries.getJSONObject(itemIndex)
                    put(item.getString("id"), item)
                }
            }
        }
        for (index in 0 until corrections.length()) {
            val correction = corrections.getJSONObject(index)
            val item = items[correction.getString("item")]
            val text = item?.optString("text").orEmpty()
            assertTrue(text.contains(correction.getString("replace")))
            assertTrue(!text.contains(correction.getString("find")))
            assertTrue(item?.optBoolean("corrected", false) == true)
        }
    }
}
