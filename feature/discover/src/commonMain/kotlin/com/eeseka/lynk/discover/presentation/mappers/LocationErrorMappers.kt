package com.eeseka.lynk.discover.presentation.mappers

import com.eeseka.lynk.core.domain.location.LocationError
import com.eeseka.lynk.core.presentation.util.UiText
import lynk.feature.discover.generated.resources.Res
import lynk.feature.discover.generated.resources.location_not_found
import lynk.feature.discover.generated.resources.location_permission_needed
import lynk.feature.discover.generated.resources.location_turned_off

fun LocationError.toUiText(): UiText? = when (this) {
    LocationError.PERMISSION_DENIED -> UiText.Resource(Res.string.location_permission_needed)
    LocationError.LOCATION_TURNED_OFF -> UiText.Resource(Res.string.location_turned_off)
    LocationError.NO_FIX_IN_TIME -> UiText.Resource(Res.string.location_not_found)
    LocationError.REQUEST_CANCELLED -> null
}
