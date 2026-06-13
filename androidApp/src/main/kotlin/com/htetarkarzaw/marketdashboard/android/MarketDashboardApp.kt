package com.htetarkarzaw.marketdashboard.android

import android.app.Application
import com.htetarkarzaw.marketdashboard.android.di.androidModule
import com.htetarkarzaw.marketdashboard.di.initKoin

class MarketDashboardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(additionalModules = listOf(androidModule))
    }
}
