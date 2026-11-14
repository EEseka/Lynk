package com.eeseka.lynk.core.presentation.location

import androidx.compose.runtime.Composable
import com.eeseka.lynk.core.domain.location.DeviceLocation

@Composable
expect fun rememberDeviceLocation(): DeviceLocation
