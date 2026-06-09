package com.htetarkarzaw.marketdashboard.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinDto(
    @SerialName("symbol") val symbol: String,
    @SerialName("lastPrice") val lastPrice: String,
    @SerialName("priceChangePercent") val priceChangePercent: String,
    @SerialName("highPrice") val highPrice: String,
    @SerialName("lowPrice") val lowPrice: String,
    @SerialName("volume") val volume: String
)
