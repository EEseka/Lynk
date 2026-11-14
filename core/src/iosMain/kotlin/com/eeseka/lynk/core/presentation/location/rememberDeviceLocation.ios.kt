package com.eeseka.lynk.core.presentation.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.eeseka.lynk.core.data.location.CoreLocationDeviceLocation
import com.eeseka.lynk.core.domain.location.DeviceLocation

@Composable
actual fun rememberDeviceLocation(): DeviceLocation {
    return remember { CoreLocationDeviceLocation() }
}
