package com.htetarkarzaw.marketdashboard.android.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object CoinListRoute : NavKey

@Serializable
object WatchlistRoute : NavKey

@Serializable
data class CoinDetailRoute(val symbol: String) : NavKey
