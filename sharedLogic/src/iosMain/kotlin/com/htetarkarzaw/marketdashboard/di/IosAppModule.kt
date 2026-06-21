package com.htetarkarzaw.marketdashboard.di

import com.htetarkarzaw.marketdashboard.data.local.DatabaseDriverFactory
import com.htetarkarzaw.marketdashboard.data.remote.createHttpClient
import org.koin.dsl.module

val iosAppModule = module {
    single { DatabaseDriverFactory() }
    single { createHttpClient() }
}
