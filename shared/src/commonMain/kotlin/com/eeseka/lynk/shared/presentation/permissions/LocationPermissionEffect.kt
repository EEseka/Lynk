package com.eeseka.lynk.shared.presentation.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Asks for the location permission once [isEnabled] turns true and reports where it landed,
 * so the caller can fetch a location, show a settings dialog, or do nothing at all.
 */
@Composable
fun LocationPermissionEffect(
    isEnabled: Boolean,
    onPermissionResolved: (PermissionState) -> Unit
) {
    val permissionController = rememberPermissionController()

    LaunchedEffect(isEnabled) {
        if (!isEnabled) return@LaunchedEffect

        var permissionState = permissionController.getPermissionState(Permission.LOCATION)
        if (permissionState == PermissionState.NOT_DETERMINED || permissionState == PermissionState.DENIED) {
            permissionState = permissionController.requestPermission(Permission.LOCATION)
        }
        onPermissionResolved(permissionState)
    }
}
