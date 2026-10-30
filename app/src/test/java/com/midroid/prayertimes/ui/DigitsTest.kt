package com.midroid.prayertimes.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DigitsTest {
    @Test
    fun localizesDigits() {
        assertEquals("١٢٣", formatDigits(123, true))
        assertEquals("123", formatDigits(123, false))
    }
}
