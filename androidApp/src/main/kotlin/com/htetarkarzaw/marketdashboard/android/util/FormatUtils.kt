package com.htetarkarzaw.marketdashboard.android.util

import java.math.RoundingMode

fun Double.formatPrice(): String = when {
    this >= 1.0 -> "$${toBigDecimal().setScale(2, RoundingMode.HALF_UP)}"
    this >= 0.01 -> "$${toBigDecimal().setScale(4, RoundingMode.HALF_UP)}"
    this >= 0.0001 -> "$${toBigDecimal().setScale(6, RoundingMode.HALF_UP)}"
    else -> "$${toBigDecimal().setScale(8, RoundingMode.HALF_UP)}"
}

fun Double.formatPercent(): String =
    "${if (this >= 0) "+" else ""}${toBigDecimal().setScale(2, RoundingMode.HALF_UP)}%"
