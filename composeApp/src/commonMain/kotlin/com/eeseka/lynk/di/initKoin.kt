package com.eeseka.lynk.di

import com.eeseka.lynk.auth.presentation.di.authPresentationModule
import com.eeseka.lynk.onboarding.presentation.di.onboardingPresentationModule
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
        )
    }
}