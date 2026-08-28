package com.eeseka.lynk.main_shell.presentation

data class MainShellState(
    val unreadNotificationCount: Int = 0,
    val hasUnseenNotifications: Boolean = false
)