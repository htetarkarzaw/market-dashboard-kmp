package com.htetarkarzaw.marketdashboard.domain.model

data class MarketSummary(
    val totalVolume: Double,
    val topGainerSymbol: String,
    val topGainerPercent: Double,
    val topLoserSymbol: String,
    val topLoserPercent: Double
)
