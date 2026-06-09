package com.htetarkarzaw.marketdashboard.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinDto(
    @SerialName("id") val id: String,
    @SerialName("symbol") val symbol: String,
    @SerialName("name") val name: String,
    @SerialName("image") val image: String,
    @SerialName("current_price") val currentPrice: Double,
    @SerialName("price_change_percentage_24h") val priceChangePercentage24h: Double,
    @SerialName("market_cap_rank") val marketCapRank: Int,
    @SerialName("high_24h") val high24h: Double,
    @SerialName("low_24h") val low24h: Double
)
