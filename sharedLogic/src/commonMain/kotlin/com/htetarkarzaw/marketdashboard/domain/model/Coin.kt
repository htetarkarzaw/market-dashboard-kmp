package com.htetarkarzaw.marketdashboard.domain.model

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val currentPrice: Double,
    val priceChangePercentage24h: Double,
    val marketCapRank: Int,
    val high24h: Double,
    val low24h: Double
)
