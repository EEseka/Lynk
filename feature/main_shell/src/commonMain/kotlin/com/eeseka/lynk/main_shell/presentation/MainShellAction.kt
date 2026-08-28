package com.eeseka.lynk.main_shell.presentation

sealed interface MainShellAction {
    data object RefreshUnreadCount : MainShellAction
    data object HangoutsTabSeen : MainShellAction
}