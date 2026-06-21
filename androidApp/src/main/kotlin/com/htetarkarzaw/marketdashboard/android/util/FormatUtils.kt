package com.htetarkarzaw.marketdashboard.android.util

import com.htetarkarzaw.marketdashboard.android.ui.model.MarketSummaryUiModel
import com.htetarkarzaw.marketdashboard.domain.model.MarketSummary
import com.htetarkarzaw.marketdashboard.util.formatPercent
import com.htetarkarzaw.marketdashboard.util.formatVolume

fun MarketSummary.toUiModel(): MarketSummaryUiModel = MarketSummaryUiModel(
    totalVolumeFormatted = formatVolume(totalVolume),
    topGainerSymbol = topGainerSymbol.removeSuffix("USDT"),
    topGainerFormatted = formatPercent(topGainerPercent),
    topLoserSymbol = topLoserSymbol.removeSuffix("USDT"),
    topLoserFormatted = formatPercent(topLoserPercent)
)
