package com.htetarkarzaw.marketdashboard.domain.model

data class Coin(
    val symbol: String,
    val baseAsset: String,
    val lastPrice: Double,
    val priceChangePercent: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val volume: Double,
    val iconUrl: String,
    val isWatchlisted: Boolean = false,
)
