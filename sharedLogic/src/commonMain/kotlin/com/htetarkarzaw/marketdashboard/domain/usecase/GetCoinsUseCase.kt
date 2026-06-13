package com.htetarkarzaw.marketdashboard.domain.usecase

import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow

class GetCoinsUseCase(private val repository: CoinRepository) {
    operator fun invoke(page: Int, pageSize: Int = 20): Flow<List<Coin>> =
        repository.getCoins(page, pageSize)
}
