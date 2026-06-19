package com.htetarkarzaw.marketdashboard.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient

fun createAndroidHttpClient(interceptors: List<Interceptor> = emptyList()): HttpClient {
    val okHttpClient = OkHttpClient.Builder().apply {
        interceptors.forEach { addInterceptor(it) }
    }.build()
    return HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(Logging) { level = LogLevel.ALL }
        install(HttpTimeout) { requestTimeoutMillis = 30_000 }
        install(WebSockets)
        engine { preconfigured = okHttpClient }
    }
}
