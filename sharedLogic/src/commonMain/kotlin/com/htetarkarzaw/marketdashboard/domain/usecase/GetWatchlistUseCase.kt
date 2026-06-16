package com.htetarkarzaw.marketdashboard.domain.usecase

import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow

class GetWatchlistUseCase(private val repository: WatchlistRepository) {
    operator fun invoke(): Flow<List<Coin>> = repository.getWatchlistCoins()
}
