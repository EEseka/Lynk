package com.eeseka.lynk.shared.presentation.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Asks for the notification permission once [isEnabled] turns true and reports where it landed,
 * so the caller can switch push off, open settings, or do nothing at all.
 */
@Composable
fun NotificationPermissionEffect(
    isEnabled: Boolean,
    onPermissionResolved: (PermissionState) -> Unit
) {
    val permissionController = rememberPermissionController()

    LaunchedEffect(isEnabled) {
        if (!isEnabled) return@LaunchedEffect

        var permissionState = permissionController.getPermissionState(Permission.NOTIFICATIONS)
        if (permissionState == PermissionState.NOT_DETERMINED || permissionState == PermissionState.DENIED) {
            permissionState = permissionController.requestPermission(Permission.NOTIFICATIONS)
        }
        onPermissionResolved(permissionState)
    }
}
