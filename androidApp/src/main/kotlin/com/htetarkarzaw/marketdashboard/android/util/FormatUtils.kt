package com.htetarkarzaw.marketdashboard.android.util

import com.htetarkarzaw.marketdashboard.android.ui.model.MarketSummaryUiModel
import com.htetarkarzaw.marketdashboard.domain.model.MarketSummary
import java.math.RoundingMode

fun Double.formatPrice(): String = when {
    this >= 1.0 -> "$${toBigDecimal().setScale(2, RoundingMode.HALF_UP)}"
    this >= 0.01 -> "$${toBigDecimal().setScale(4, RoundingMode.HALF_UP)}"
    this >= 0.0001 -> "$${toBigDecimal().setScale(6, RoundingMode.HALF_UP)}"
    else -> "$${toBigDecimal().setScale(8, RoundingMode.HALF_UP)}"
}

fun Double.formatPercent(): String =
    "${if (this >= 0) "+" else ""}${toBigDecimal().setScale(2, RoundingMode.HALF_UP)}%"

fun Double.formatVolume(): String = when {
    this >= 1_000_000_000.0 -> "$${(this / 1_000_000_000.0).toBigDecimal().setScale(1, RoundingMode.HALF_UP)}B"
    this >= 1_000_000.0 -> "$${(this / 1_000_000.0).toBigDecimal().setScale(1, RoundingMode.HALF_UP)}M"
    else -> "$${toBigDecimal().setScale(0, RoundingMode.HALF_UP)}"
}

fun MarketSummary.toUiModel(): MarketSummaryUiModel = MarketSummaryUiModel(
    totalVolumeFormatted = totalVolume.formatVolume(),
    topGainerSymbol = topGainerSymbol.removeSuffix("USDT"),
    topGainerFormatted = topGainerPercent.formatPercent(),
    topLoserSymbol = topLoserSymbol.removeSuffix("USDT"),
    topLoserFormatted = topLoserPercent.formatPercent()
)
