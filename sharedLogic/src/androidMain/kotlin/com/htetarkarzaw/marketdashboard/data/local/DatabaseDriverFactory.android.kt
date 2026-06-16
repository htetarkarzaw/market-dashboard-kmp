package com.htetarkarzaw.marketdashboard.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.htetarkarzaw.marketdashboard.data.local.MarketDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(MarketDatabase.Schema, context, "market.db")
}
