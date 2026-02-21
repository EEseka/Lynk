package com.eeseka.lynk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform