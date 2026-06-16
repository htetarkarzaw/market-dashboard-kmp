package com.htetarkarzaw.marketdashboard.data.remote

import com.htetarkarzaw.marketdashboard.data.remote.dto.TickerDto
import kotlinx.coroutines.flow.Flow

interface BinanceWebSocketClient {
    fun observePrices(): Flow<List<TickerDto>>
}
