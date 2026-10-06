package com.eeseka.lynk.shared.domain.location

interface AreaNameResolver {

    // The name people use for the area around a point, e.g. "Lagos" or "London"; null when the device can't tell
    suspend fun getAreaName(latitude: Double, longitude: Double): String?
}
