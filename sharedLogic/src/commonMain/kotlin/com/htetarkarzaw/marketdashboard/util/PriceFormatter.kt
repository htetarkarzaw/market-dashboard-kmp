package com.htetarkarzaw.marketdashboard.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

private fun addThousandsSeparator(intPart: String): String =
    intPart.reversed().chunked(3).joinToString(",").reversed()

private fun Double.formatFixed(decimals: Int, thousands: Boolean = false): String {
    val factor = 10.0.pow(decimals).toLong()
    val negative = this < 0
    val absVal = abs(this)
    val totalUnits = (absVal * factor).roundToLong()
    val intPart = totalUnits / factor
    val intStr = if (thousands) addThousandsSeparator(intPart.toString()) else intPart.toString()
    if (decimals == 0) return if (negative) "-$intStr" else intStr
    val fracPart = totalUnits % factor
    val fracStr = fracPart.toString().padStart(decimals, '0')
    return if (negative) "-$intStr.$fracStr" else "$intStr.$fracStr"
}

fun formatPrice(price: Double): String = "$" + when {
    price >= 1.0    -> price.formatFixed(2, thousands = true)
    price >= 0.01   -> price.formatFixed(4, thousands = true)
    price >= 0.0001 -> price.formatFixed(6, thousands = true)
    else            -> price.formatFixed(8, thousands = true)
}

fun formatPercent(percent: Double): String {
    val formatted = percent.formatFixed(2)
    return if (percent >= 0) "+$formatted%" else "$formatted%"
}

fun formatVolume(volume: Double): String = when {
    volume >= 1_000_000_000.0 -> "$" + (volume / 1_000_000_000.0).formatFixed(1) + "B"
    volume >= 1_000_000.0     -> "$" + (volume / 1_000_000.0).formatFixed(1) + "M"
    else                      -> "$" + volume.formatFixed(0, thousands = true)
}
