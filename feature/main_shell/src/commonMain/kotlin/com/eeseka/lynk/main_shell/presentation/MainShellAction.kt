package com.eeseka.lynk.main_shell.presentation

sealed interface MainShellAction {
    data object OnNotificationPermissionDenied : MainShellAction
    data class OnHangoutsTabActiveChanged(val isActive: Boolean) : MainShellAction
    data class OnHangoutDetailPaneFullScreenChanged(val isFullScreen: Boolean) : MainShellAction
}