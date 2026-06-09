package com.htetarkarzaw.marketdashboard.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.htetarkarzaw.marketdashboard.data.local.MarketDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(MarketDatabase.Schema, "market.db")
}
