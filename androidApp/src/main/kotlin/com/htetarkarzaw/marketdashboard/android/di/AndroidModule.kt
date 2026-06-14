package com.htetarkarzaw.marketdashboard.android.di

import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListViewModel
import com.htetarkarzaw.marketdashboard.data.local.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    viewModel { CoinListViewModel(get(), get()) }
}
