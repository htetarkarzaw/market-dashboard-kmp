package com.htetarkarzaw.marketdashboard.android.di

import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import okhttp3.Interceptor

fun debugInterceptors(): List<Interceptor> = listOf(OkHttpProfilerInterceptor())
