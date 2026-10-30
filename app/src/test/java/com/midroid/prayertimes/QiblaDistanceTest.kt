package com.midroid.prayertimes

import com.midroid.prayertimes.domain.Qibla
import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaDistanceTest {
    @Test fun muscatToMakkahDistance() {
        assertEquals(1920.0, Qibla.distanceKm(23.588, 58.3829), 2.0)
    }
}
