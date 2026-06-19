package com.htetarkarzaw.marketdashboard.android.di

import com.htetarkarzaw.marketdashboard.android.ui.coindetail.CoinDetailViewModel
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListViewModel
import com.htetarkarzaw.marketdashboard.android.ui.watchlist.WatchlistViewModel
import com.htetarkarzaw.marketdashboard.data.local.DatabaseDriverFactory
import com.htetarkarzaw.marketdashboard.data.remote.createAndroidHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { createAndroidHttpClient(debugInterceptors()) }
    viewModel { CoinListViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { WatchlistViewModel(get(), get()) }
    viewModel { CoinDetailViewModel(get(), get(), get(), get(), get()) }
}
