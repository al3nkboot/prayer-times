package com.midroid.prayertimes

import com.midroid.prayertimes.domain.NightTimesCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZonedDateTime

class NightTimesTest {
    @Test fun midpointAndLastThirdFromSunsetToDawn() {
        val sunset = ZonedDateTime.parse("2026-10-30T17:45:00+04:00[Asia/Muscat]")
        val dawn = ZonedDateTime.parse("2026-10-31T05:15:00+04:00[Asia/Muscat]")
        val times = NightTimesCalculator.calculate(sunset, dawn)
        assertEquals("2026-10-30T23:30+04:00[Asia/Muscat]", times.midnight.toString())
        assertEquals("2026-10-31T01:25+04:00[Asia/Muscat]", times.lastThird.toString())
    }
}
