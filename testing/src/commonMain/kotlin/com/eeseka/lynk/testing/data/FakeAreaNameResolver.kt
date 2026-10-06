package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.location.AreaNameResolver

class FakeAreaNameResolver : AreaNameResolver {
    var areaName: String? = "Lagos"

    override suspend fun getAreaName(latitude: Double, longitude: Double): String? = areaName
}
