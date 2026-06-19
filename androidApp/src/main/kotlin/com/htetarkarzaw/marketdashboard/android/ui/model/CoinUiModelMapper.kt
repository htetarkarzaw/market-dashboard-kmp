package com.htetarkarzaw.marketdashboard.android.ui.model

import com.htetarkarzaw.marketdashboard.android.util.formatPercent
import com.htetarkarzaw.marketdashboard.android.util.formatPrice
import com.htetarkarzaw.marketdashboard.android.util.formatVolume
import com.htetarkarzaw.marketdashboard.domain.model.Coin

fun Coin.toUiModel(isWatchlisted: Boolean = false): CoinUiModel = CoinUiModel(
    symbol = symbol,
    baseAsset = baseAsset,
    iconUrl = iconUrl,
    priceFormatted = lastPrice.formatPrice(),
    priceChangeFormatted = priceChangePercent.formatPercent(),
    isPositiveChange = priceChangePercent >= 0,
    highFormatted = highPrice.formatPrice(),
    lowFormatted = lowPrice.formatPrice(),
    volumeFormatted = volume.formatVolume(),
    isWatchlisted = isWatchlisted,
)
