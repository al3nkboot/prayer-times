package com.midroid.prayertimes.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun normalize360(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0

/** Signed shortest clockwise difference from [from] to [to], in [-180, 180]. */
fun shortestDiff(from: Double, to: Double): Double {
    val d = normalize360(to - from)
    return if (d > 180.0) d - 360.0 else d
}

enum class TurnDirection { RIGHT, LEFT, FACING }

fun turnDirection(heading: Double, target: Double): TurnDirection = when {
    kotlin.math.abs(shortestDiff(heading, target)) <= 3.0 -> TurnDirection.FACING
    shortestDiff(heading, target) > 0.0 -> TurnDirection.RIGHT
    else -> TurnDirection.LEFT
}

class CircularLowPass(private val alpha: Double = 0.15) {
    private var x = 0.0
    private var y = 0.0
    private var initialized = false

    fun filter(degrees: Double): Double {
        val r = Math.toRadians(normalize360(degrees))
        val nx = cos(r); val ny = sin(r)
        if (!initialized) { x = nx; y = ny; initialized = true }
        else { x = (1.0 - alpha) * x + alpha * nx; y = (1.0 - alpha) * y + alpha * ny }
        return normalize360(Math.toDegrees(atan2(y, x)))
    }
}

class Unwrapper(initial: Double? = null) {
    var value: Double? = initial
        private set

    fun update(raw: Double): Double {
        val normalized = normalize360(raw)
        value = value?.let { it + shortestDiff(normalize360(it), normalized) } ?: normalized
        return value!!
    }
}

class FacingHysteresis {
    var isFacing: Boolean = false
        private set

    /** Returns true only on the transition into the facing state. */
    fun update(absoluteDifference: Double): Boolean {
        val wasFacing = isFacing
        if (isFacing) {
            if (absoluteDifference > 6.0) isFacing = false
        } else if (absoluteDifference <= 3.0) isFacing = true
        return !wasFacing && isFacing
    }
}
