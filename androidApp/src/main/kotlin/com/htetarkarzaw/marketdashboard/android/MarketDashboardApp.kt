package com.htetarkarzaw.marketdashboard.android

import android.app.Application
import com.htetarkarzaw.marketdashboard.BuildConfig
import com.htetarkarzaw.marketdashboard.android.di.androidModule
import com.htetarkarzaw.marketdashboard.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext

class MarketDashboardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Napier.base(DebugAntilog())
        initKoin(additionalModules = listOf(androidModule)) {
            androidContext(this@MarketDashboardApp)
        }
    }
}
