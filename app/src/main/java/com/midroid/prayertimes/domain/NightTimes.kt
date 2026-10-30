package com.midroid.prayertimes.domain

import java.time.Duration
import java.time.ZonedDateTime

/** Midpoint and last-third boundary for the night from today's sunset to tomorrow's dawn. */
data class NightTimes(val midnight: ZonedDateTime, val lastThird: ZonedDateTime)
object NightTimesCalculator {
    fun calculate(maghrib: ZonedDateTime, nextFajr: ZonedDateTime): NightTimes {
        require(nextFajr.isAfter(maghrib))
        val span = Duration.between(maghrib, nextFajr)
        return NightTimes(maghrib.plusSeconds(span.seconds / 2), maghrib.plusSeconds(span.seconds * 2 / 3))
    }
}
