package com.htetarkarzaw.marketdashboard.data.remote.mapper

import com.htetarkarzaw.marketdashboard.data.remote.dto.CoinDto
import com.htetarkarzaw.marketdashboard.domain.model.Coin

private val QUOTE_CURRENCIES = listOf("USDT", "BUSD", "BNB", "BTC", "ETH")

fun CoinDto.toDomain(): Coin {
    val baseAsset = QUOTE_CURRENCIES
        .firstOrNull { symbol.endsWith(it) }
        ?.let { symbol.removeSuffix(it) }
        ?: symbol
    return Coin(
        symbol = symbol,
        baseAsset = baseAsset,
        lastPrice = lastPrice.toDoubleOrNull() ?: 0.0,
        priceChangePercent = priceChangePercent.toDoubleOrNull() ?: 0.0,
        highPrice = highPrice.toDoubleOrNull() ?: 0.0,
        lowPrice = lowPrice.toDoubleOrNull() ?: 0.0,
        volume = volume.toDoubleOrNull() ?: 0.0,
        iconUrl = "https://assets.coincap.io/assets/icons/${baseAsset.lowercase()}@2x.png"
    )
}
