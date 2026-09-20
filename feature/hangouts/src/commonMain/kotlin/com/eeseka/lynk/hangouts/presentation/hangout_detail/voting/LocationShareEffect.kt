package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.location.LocationAccuracy
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.permissions.LocationPermissionEffect
import com.eeseka.lynk.shared.presentation.permissions.PermissionState

@Composable
fun LocationShareEffect(
    enabled: Boolean,
    isConnected: Boolean,
    onShareLocation: (Double, Double) -> Unit
) {
    // The lobby pins you on a shared map, so this is the one place the sharper fix is worth the wait.
    val locationController = rememberLocationController(LocationAccuracy.Precise)
    var permissionState by remember { mutableStateOf(PermissionState.NOT_DETERMINED) }

    LocationPermissionEffect(isEnabled = enabled) { resolvedPermissionState ->
        permissionState = resolvedPermissionState
    }

    LaunchedEffect(permissionState, enabled, isConnected) {
        if (!enabled || !isConnected) return@LaunchedEffect
        if (permissionState != PermissionState.GRANTED) return@LaunchedEffect

        locationController.observeCurrentLocation().collect { locationResult ->
            locationResult.onSuccess { coordinate ->
                onShareLocation(coordinate.latitude, coordinate.longitude)
            }
        }
    }
}
