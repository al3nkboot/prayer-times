package com.midroid.prayertimes
import com.midroid.prayertimes.domain.*
import org.junit.Assert.*
import org.junit.Test
class TasbihCounterTest{@Test fun roundsAndReset(){val c=TasbihCounter(33);repeat(32){assertEquals(TapEvent.NONE,c.tap())};assertEquals(TapEvent.ROUND_COMPLETE,c.tap());repeat(32){c.tap()};assertEquals(TapEvent.ROUND_COMPLETE,c.tap());c.reset();assertEquals(0,c.count);c.target=5;repeat(4){c.tap()};assertEquals(TapEvent.ROUND_COMPLETE,c.tap())}}
