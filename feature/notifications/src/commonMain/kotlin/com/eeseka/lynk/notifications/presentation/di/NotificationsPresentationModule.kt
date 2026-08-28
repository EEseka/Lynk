package com.eeseka.lynk.notifications.presentation.di

import com.eeseka.lynk.notifications.presentation.invite_preview.InvitePreviewViewModel
import com.eeseka.lynk.notifications.presentation.notifications.NotificationsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notificationsPresentationModule = module {
    viewModelOf(::NotificationsViewModel)
    viewModelOf(::InvitePreviewViewModel)
}