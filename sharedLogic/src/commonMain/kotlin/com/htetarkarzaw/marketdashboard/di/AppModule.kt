package com.htetarkarzaw.marketdashboard.di

import com.htetarkarzaw.marketdashboard.data.local.DatabaseDriverFactory
import com.htetarkarzaw.marketdashboard.data.local.MarketDatabase
import com.htetarkarzaw.marketdashboard.data.remote.BinanceApi
import com.htetarkarzaw.marketdashboard.data.remote.BinanceWebSocketClient
import com.htetarkarzaw.marketdashboard.data.remote.KtorBinanceApi
import com.htetarkarzaw.marketdashboard.data.remote.KtorBinanceWebSocketClient
import com.htetarkarzaw.marketdashboard.data.remote.createHttpClient
import com.htetarkarzaw.marketdashboard.data.repository.CoinRepositoryImpl
import com.htetarkarzaw.marketdashboard.data.repository.WatchlistRepositoryImpl
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import com.htetarkarzaw.marketdashboard.domain.repository.WatchlistRepository
import com.htetarkarzaw.marketdashboard.domain.usecase.AddToWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RefreshCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RemoveFromWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.StartPriceUpdatesUseCase
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { createHttpClient() }
    single<BinanceApi> { KtorBinanceApi(get()) }
    single<BinanceWebSocketClient> { KtorBinanceWebSocketClient(get()) }
    single { MarketDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { CoinRepositoryImpl(get(), get(), get()) } bind CoinRepository::class
    single<WatchlistRepository> { WatchlistRepositoryImpl(get()) }
    factory { GetCoinsUseCase(get()) }
    factory { RefreshCoinsUseCase(get()) }
    factory { StartPriceUpdatesUseCase(get()) }
    factory { GetWatchlistUseCase(get()) }
    factory { AddToWatchlistUseCase(get()) }
    factory { RemoveFromWatchlistUseCase(get()) }
}

fun initKoin(
    additionalModules: List<Module> = emptyList(),
    appDeclaration: KoinApplication.() -> Unit = {}
) {
    startKoin {
        appDeclaration()
        modules(listOf(appModule) + additionalModules)
    }
}
