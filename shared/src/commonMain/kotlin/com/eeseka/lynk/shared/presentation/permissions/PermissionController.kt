package com.eeseka.lynk.shared.presentation.permissions

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION

class PermissionController(
    private val mokoPermissionsController: PermissionsController
) {
    suspend fun requestPermission(permission: Permission): PermissionState {
        return try {
            mokoPermissionsController.providePermission(permission.toMokoPermission())
            PermissionState.GRANTED
        } catch (_: DeniedAlwaysException) {
            PermissionState.PERMANENTLY_DENIED
        } catch (_: DeniedException) {
            PermissionState.DENIED
        } catch (_: RequestCanceledException) {
            PermissionState.DENIED
        }
    }

    suspend fun getPermissionState(permission: Permission): PermissionState {
        return try {
            mokoPermissionsController.getPermissionState(permission.toMokoPermission())
                .toPermissionState()
        } catch (_: Exception) {
            PermissionState.DENIED
        }
    }

    fun openAppSettings() {
        mokoPermissionsController.openAppSettings()
    }
}

fun Permission.toMokoPermission(): dev.icerock.moko.permissions.Permission {
    return when (this) {
        Permission.NOTIFICATIONS -> dev.icerock.moko.permissions.Permission.REMOTE_NOTIFICATION
    }
}


fun dev.icerock.moko.permissions.PermissionState.toPermissionState(): PermissionState {
    return when (this) {
        dev.icerock.moko.permissions.PermissionState.Granted -> PermissionState.GRANTED
        dev.icerock.moko.permissions.PermissionState.DeniedAlways -> PermissionState.PERMANENTLY_DENIED
        else -> PermissionState.DENIED // Treat undetermined as denied for checking purposes
    }
}