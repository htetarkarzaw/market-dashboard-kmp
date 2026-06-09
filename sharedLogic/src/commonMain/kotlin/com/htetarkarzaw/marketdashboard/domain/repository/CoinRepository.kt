package com.htetarkarzaw.marketdashboard.domain.repository

import com.htetarkarzaw.marketdashboard.domain.model.Coin
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun getCoins(page: Int, pageSize: Int): Flow<List<Coin>>
    suspend fun refreshCoins()
}
