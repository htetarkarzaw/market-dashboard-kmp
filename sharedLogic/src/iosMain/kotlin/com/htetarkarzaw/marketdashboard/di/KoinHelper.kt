package com.htetarkarzaw.marketdashboard.di

import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RefreshCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.StartPriceUpdatesUseCase
import org.koin.mp.KoinPlatform

fun initKoin() {
    initKoin(additionalModules = listOf(iosAppModule))
}

fun makeGetCoinsUseCase(): GetCoinsUseCase = KoinPlatform.getKoin().get()
fun makeRefreshCoinsUseCase(): RefreshCoinsUseCase = KoinPlatform.getKoin().get()
fun makeStartPriceUpdatesUseCase(): StartPriceUpdatesUseCase = KoinPlatform.getKoin().get()
