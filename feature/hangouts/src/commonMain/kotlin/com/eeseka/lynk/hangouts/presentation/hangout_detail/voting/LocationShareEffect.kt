package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController

@Composable
fun LocationShareEffect(
    enabled: Boolean,
    isConnected: Boolean,
    onShareLocation: (Double, Double) -> Unit
) {
    val permissionController = rememberPermissionController()
    val locationController = rememberLocationController()
    var permissionState by remember { mutableStateOf(PermissionState.NOT_DETERMINED) }

    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect

        permissionState = permissionController.getPermissionState(Permission.LOCATION)
        if (permissionState == PermissionState.NOT_DETERMINED || permissionState == PermissionState.DENIED) {
            permissionState = permissionController.requestPermission(Permission.LOCATION)
        }
    }

    LaunchedEffect(permissionState, enabled, isConnected) {
        if (!enabled || !isConnected) return@LaunchedEffect
        if (permissionState != PermissionState.GRANTED) return@LaunchedEffect

        val coordinate = locationController.getCurrentLocation() ?: return@LaunchedEffect
        onShareLocation(coordinate.latitude, coordinate.longitude)
    }
}
