package com.midroid.prayertimes.ui

fun formatDigits(value: Int, arabic: Boolean): String {
    val western = value.toString()
    if (!arabic) return western
    return western.map { char -> if (char in '0'..'9') ('٠'.code + char.code - '0'.code).toChar() else char }.joinToString("")
}

private val ayahOrnament = Regex("﴿(\\d+)﴾")

/** Display-only: localise the digits inside ayah-number ornaments ﴿12﴾ (the ornaments are added by our build script, not part of the Quran text). */
fun localizeAyahOrnaments(text: String, arabic: Boolean): String =
    if (!arabic) text else text.replace(ayahOrnament) { "﴿" + formatDigits(it.groupValues[1].toInt(), true) + "﴾" }
