package com.eeseka.lynk.main_shell.presentation

data class MainShellState(
    val canReceiveNotifications: Boolean = false,
    val unreadNotificationCount: Int = 0,
    val hasUnseenNotifications: Boolean = false,
    val isHangoutDetailPaneFullScreen: Boolean = false
)