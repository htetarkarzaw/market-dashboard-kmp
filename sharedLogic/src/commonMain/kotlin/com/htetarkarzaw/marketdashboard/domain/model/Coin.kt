package com.htetarkarzaw.marketdashboard.domain.model

import com.htetarkarzaw.marketdashboard.util.formatPercent
import com.htetarkarzaw.marketdashboard.util.formatPrice
import com.htetarkarzaw.marketdashboard.util.formatVolume

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
) {
    val priceFormatted: String get() = formatPrice(lastPrice)
    val priceChangeFormatted: String get() = formatPercent(priceChangePercent)
    val highFormatted: String get() = formatPrice(highPrice)
    val lowFormatted: String get() = formatPrice(lowPrice)
    val volumeFormatted: String get() = formatVolume(volume)
}
