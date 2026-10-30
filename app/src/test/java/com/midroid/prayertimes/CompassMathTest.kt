package com.midroid.prayertimes

import com.midroid.prayertimes.domain.*
import org.junit.Assert.*
import org.junit.Test

class CompassMathTest {
    @Test fun shortestDifferenceWrapsAndHandlesHalfTurn() {
        assertEquals(20.0, shortestDiff(350.0, 10.0), 1e-9)
        assertEquals(-20.0, shortestDiff(10.0, 350.0), 1e-9)
        assertEquals(180.0, shortestDiff(0.0, 180.0), 1e-9)
        assertEquals(180.0, shortestDiff(180.0, 0.0), 1e-9)
    }

    @Test fun unwrapsContinuouslyAcrossManyWraps() {
        val u = Unwrapper(350.0)
        assertEquals(370.0, u.update(10.0), 1e-9)
        assertEquals(370.0, u.update(10.0), 1e-9)
        var expected = 370.0
        var raw = 10.0
        repeat(48) { raw = normalize360(raw + 30.0); expected += 30.0; assertEquals(expected, u.update(raw), 1e-9) }
    }

    @Test fun circularFilterWrapsAroundZero() {
        val f = CircularLowPass(0.5)
        f.filter(359.0)
        val result = f.filter(1.0)
        assertTrue(result < 1.0 || result > 359.0)
    }

    @Test fun directionsAreRelativeAndClockwiseIsRight() {
        assertEquals(TurnDirection.RIGHT, turnDirection(350.0, 10.0))
        assertEquals(TurnDirection.LEFT, turnDirection(10.0, 350.0))
        assertEquals(TurnDirection.FACING, turnDirection(10.0, 12.0))
    }

    @Test fun facingHysteresisEntersOnceAndExitsBeyondSix() {
        val h = FacingHysteresis()
        assertTrue(h.update(2.9))
        assertTrue(h.isFacing)
        assertFalse(h.update(5.0))
        assertFalse(h.update(2.8))
        assertTrue(h.isFacing)
        assertFalse(h.update(6.5))
        assertFalse(h.isFacing)
        assertTrue(h.update(2.9))
    }
}
