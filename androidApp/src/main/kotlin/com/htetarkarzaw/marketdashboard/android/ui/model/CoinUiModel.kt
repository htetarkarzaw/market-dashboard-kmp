package com.htetarkarzaw.marketdashboard.android.ui.model

data class CoinUiModel(
    val symbol: String,
    val baseAsset: String,
    val iconUrl: String,
    val priceFormatted: String,
    val priceChangeFormatted: String,
    val isPositiveChange: Boolean
)
