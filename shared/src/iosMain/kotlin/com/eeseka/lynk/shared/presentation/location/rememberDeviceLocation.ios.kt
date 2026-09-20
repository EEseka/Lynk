package com.eeseka.lynk.shared.presentation.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.eeseka.lynk.shared.data.location.CoreLocationDeviceLocation
import com.eeseka.lynk.shared.domain.location.DeviceLocation

@Composable
actual fun rememberDeviceLocation(): DeviceLocation {
    return remember { CoreLocationDeviceLocation() }
}
