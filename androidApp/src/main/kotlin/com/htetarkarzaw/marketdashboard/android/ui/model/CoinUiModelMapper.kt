package com.htetarkarzaw.marketdashboard.android.ui.model

import com.htetarkarzaw.marketdashboard.domain.model.Coin

fun Coin.toUiModel(isWatchlisted: Boolean = false): CoinUiModel = CoinUiModel(
    symbol = symbol,
    baseAsset = baseAsset,
    iconUrl = iconUrl,
    priceFormatted = priceFormatted,
    priceChangeFormatted = priceChangeFormatted,
    isPositiveChange = priceChangePercent >= 0,
    highFormatted = highFormatted,
    lowFormatted = lowFormatted,
    volumeFormatted = volumeFormatted,
    isWatchlisted = isWatchlisted,
)
