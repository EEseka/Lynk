package com.eeseka.lynk.core.presentation.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.eeseka.lynk.core.data.location.PlayServicesDeviceLocation
import com.eeseka.lynk.core.domain.location.DeviceLocation

@Composable
actual fun rememberDeviceLocation(): DeviceLocation {
    val context = LocalContext.current

    return remember { PlayServicesDeviceLocation(context) }
}
