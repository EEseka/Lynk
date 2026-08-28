package com.eeseka.lynk.di

import com.eeseka.lynk.auth.presentation.di.authPresentationModule
import com.eeseka.lynk.create_hangout.presentation.di.createHangoutPresentationModule
import com.eeseka.lynk.discover.presentation.di.discoverPresentationModule
import com.eeseka.lynk.hangouts.presentation.di.hangoutsPresentationModule
import com.eeseka.lynk.main_shell.presentation.di.mainShellPresentationModule
import com.eeseka.lynk.notifications.presentation.di.notificationsPresentationModule
import com.eeseka.lynk.onboarding.presentation.di.onboardingPresentationModule
import com.eeseka.lynk.profile.presentation.di.profilePresentationModule
import com.eeseka.lynk.profile_setup.presentation.di.profileSetupPresentationModule
import com.eeseka.lynk.shared.data.di.sharedDataModule
import com.eeseka.lynk.shared.presentation.di.sharedPresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            appModule,
            sharedDataModule,
            sharedPresentationModule,
            onboardingPresentationModule,
            authPresentationModule,
            profileSetupPresentationModule,
            discoverPresentationModule,
            createHangoutPresentationModule,
            hangoutsPresentationModule,
            profilePresentationModule,
            notificationsPresentationModule,
            mainShellPresentationModule
        )
    }
}