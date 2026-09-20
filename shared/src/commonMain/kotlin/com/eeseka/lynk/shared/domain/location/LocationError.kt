package com.eeseka.lynk.shared.domain.location

import com.eeseka.lynk.shared.domain.util.Error

enum class LocationError : Error {
    PERMISSION_DENIED,
    REQUEST_CANCELLED,
    LOCATION_TURNED_OFF,
    NO_FIX_IN_TIME
}
