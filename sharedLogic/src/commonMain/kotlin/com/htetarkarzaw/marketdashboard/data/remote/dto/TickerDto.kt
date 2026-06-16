package com.htetarkarzaw.marketdashboard.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TickerDto(
    @SerialName("s") val symbol: String,
    @SerialName("c") val lastPrice: String,
    @SerialName("o") val openPrice: String,
    @SerialName("h") val highPrice: String,
    @SerialName("l") val lowPrice: String,
    @SerialName("v") val volume: String,
    @SerialName("q") val quoteVolume: String
)
