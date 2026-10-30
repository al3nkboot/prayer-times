package com.midroid.prayertimes.domain

import java.time.ZonedDateTime
import kotlin.math.*

data class SunPosition(val azimuth: Double, val altitude: Double)

/** NOAA solar position approximation, angles in degrees (azimuth clockwise from true north). */
object SunPositionCalculator {
    fun calculate(time: ZonedDateTime, latitude: Double, longitude: Double): SunPosition {
        val utc = time.toInstant().atZone(java.time.ZoneOffset.UTC)
        val jd = utc.toEpochSecond() / 86400.0 + 2440587.5
        val t = (jd - 2451545.0) / 36525.0
        val l0 = (280.46646 + t * (36000.76983 + t * 0.0003032)).mod(360.0)
        val m = Math.toRadians((357.52911 + t * (35999.05029 - 0.0001537 * t)).mod(360.0))
        val e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
        val c = sin(m) * (1.914602 - t * (0.004817 + 0.000014 * t)) + sin(2*m) * (0.019993 - 0.000101*t) + sin(3*m)*0.000289
        val trueLong = l0 + c
        val omega = 125.04 - 1934.136*t
        val lambda = Math.toRadians(trueLong - 0.00569 - 0.00478*sin(Math.toRadians(omega)))
        val eps0 = 23.0 + (26.0 + (21.448 - t*(46.815 + t*(0.00059 - t*0.001813)))/60.0)/60.0
        val eps = Math.toRadians(eps0 + 0.00256*cos(Math.toRadians(omega)))
        val decl = asin(sin(eps)*sin(lambda))
        val y = tan(eps/2).pow(2)
        val l0r = Math.toRadians(l0)
        val eq = 4.0 * Math.toDegrees(y*sin(2*l0r) - 2*e*sin(m) + 4*e*y*sin(m)*cos(2*l0r) - 0.5*y*y*sin(4*l0r) - 1.25*e*e*sin(2*m))
        val minutes = utc.hour*60.0 + utc.minute + utc.second/60.0
        val tst = (minutes + eq + 4*longitude).mod(1440.0)
        val ha = Math.toRadians(tst/4.0 - 180.0)
        val lat = Math.toRadians(latitude)
        val zenith = acos((sin(lat)*sin(decl) + cos(lat)*cos(decl)*cos(ha)).coerceIn(-1.0,1.0))
        val az = (Math.toDegrees(atan2(sin(ha), cos(ha)*sin(lat) - tan(decl)*cos(lat))) + 180.0).mod(360.0)
        return SunPosition(az, 90.0 - Math.toDegrees(zenith))
    }
}
