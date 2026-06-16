package com.htetarkarzaw.marketdashboard.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = MarketDatabase.Schema,
        context = context,
        name = "market.db",
        callback = AndroidSqliteDriver.Callback(MarketDatabase.Schema)
    )
}
