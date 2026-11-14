package com.eeseka.lynk.core.domain.location

import com.eeseka.lynk.core.domain.util.Error

enum class LocationError : Error {
    PERMISSION_DENIED,
    REQUEST_CANCELLED,
    LOCATION_TURNED_OFF,
    NO_FIX_IN_TIME
}
