package com.midroid.prayertimes

import com.midroid.prayertimes.domain.Qibla
import com.midroid.prayertimes.domain.SunPositionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class SunPositionTest {
    @Test fun noaaExampleWithinOneDegree() {
        // NOAA solar calculator worked example: 2003-10-17 12:30:30 MST, Boulder, Colorado.
        val time = Instant.parse("2003-10-17T19:30:30Z").atZone(ZoneOffset.UTC)
        val p = SunPositionCalculator.calculate(time, 39.742476, -105.1786)
        assertEquals(194.34, p.azimuth, 1.0)
        assertEquals(39.89, p.altitude, 1.0)
    }

    @Test fun kaabaSunNearZenithAtMayMoment() {
        val time = Instant.parse("2026-05-27T09:18:00Z").atZone(ZoneOffset.UTC)
        assertTrue(SunPositionCalculator.calculate(time, Qibla.KAABA_LAT, Qibla.KAABA_LON).altitude > 88.0)
    }
}
