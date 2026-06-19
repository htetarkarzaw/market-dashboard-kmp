package com.htetarkarzaw.marketdashboard.domain.usecase

import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow

class GetCoinDetailUseCase(private val repository: CoinRepository) {
    operator fun invoke(symbol: String): Flow<Coin?> = repository.getCoinWithWatchlist(symbol)
}
