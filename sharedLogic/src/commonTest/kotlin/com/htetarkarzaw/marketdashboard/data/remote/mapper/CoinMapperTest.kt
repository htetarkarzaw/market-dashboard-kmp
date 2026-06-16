package com.htetarkarzaw.marketdashboard.data.remote.mapper

import com.htetarkarzaw.marketdashboard.data.remote.dto.CoinDto
import kotlin.test.Test
import kotlin.test.assertEquals

class CoinMapperTest {

    private fun coinDto(
        symbol: String = "BTCUSDT",
        lastPrice: String = "45231.12",
        priceChangePercent: String = "2.34",
        highPrice: String = "46000.00",
        lowPrice: String = "44000.00",
        volume: String = "0.00",
        quoteVolume: String = "500000000.00"
    ) = CoinDto(
        symbol = symbol,
        lastPrice = lastPrice,
        priceChangePercent = priceChangePercent,
        highPrice = highPrice,
        lowPrice = lowPrice,
        volume = volume,
        quoteVolume = quoteVolume
    )

    @Test
    fun mapsAllFieldsCorrectly() {
        val coin = coinDto().toDomain()

        assertEquals("BTCUSDT", coin.symbol)
        assertEquals(45231.12, coin.lastPrice)
        assertEquals(2.34, coin.priceChangePercent)
        assertEquals(46000.00, coin.highPrice)
        assertEquals(44000.00, coin.lowPrice)
        assertEquals(500000000.00, coin.volume)
    }

    @Test
    fun extractsBaseAssetCorrectly() {
        assertEquals("BTC", coinDto(symbol = "BTCUSDT").toDomain().baseAsset)
        assertEquals("ETH", coinDto(symbol = "ETHUSDT").toDomain().baseAsset)
        assertEquals("BNB", coinDto(symbol = "BNBUSDT").toDomain().baseAsset)
    }

    @Test
    fun constructsIconUrlCorrectly() {
        val coin = coinDto(symbol = "BTCUSDT").toDomain()
        assertEquals("https://assets.coincap.io/assets/icons/btc@2x.png", coin.iconUrl)
    }

    @Test
    fun handlesInvalidPriceStrings() {
        val coin = coinDto(lastPrice = "invalid", quoteVolume = "").toDomain()

        assertEquals(0.0, coin.lastPrice)
        assertEquals(0.0, coin.volume)
    }
}
