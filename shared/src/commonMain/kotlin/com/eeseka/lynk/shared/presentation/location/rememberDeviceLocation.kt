package com.eeseka.lynk.shared.presentation.location

import androidx.compose.runtime.Composable
import com.eeseka.lynk.shared.domain.location.DeviceLocation

@Composable
expect fun rememberDeviceLocation(): DeviceLocation
