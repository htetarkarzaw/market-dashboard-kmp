package com.htetarkarzaw.marketdashboard.di

import com.htetarkarzaw.marketdashboard.data.local.DatabaseDriverFactory
import com.htetarkarzaw.marketdashboard.data.local.MarketDatabase
import com.htetarkarzaw.marketdashboard.data.remote.BinanceApi
import com.htetarkarzaw.marketdashboard.data.remote.createHttpClient
import com.htetarkarzaw.marketdashboard.data.repository.CoinRepositoryImpl
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RefreshCoinsUseCase
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { createHttpClient() }
    single { BinanceApi(get()) }
    single { MarketDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { CoinRepositoryImpl(get(), get()) } bind CoinRepository::class
    factory { GetCoinsUseCase(get()) }
    factory { RefreshCoinsUseCase(get()) }
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
