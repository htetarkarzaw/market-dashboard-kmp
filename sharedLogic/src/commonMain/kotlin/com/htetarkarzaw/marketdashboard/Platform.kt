package com.htetarkarzaw.marketdashboard

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform