package com.htetarkarzaw.marketdashboard.android.ui.model

data class MarketSummaryUiModel(
    val totalVolumeFormatted: String,
    val topGainerSymbol: String,
    val topGainerFormatted: String,
    val topLoserSymbol: String,
    val topLoserFormatted: String
)
