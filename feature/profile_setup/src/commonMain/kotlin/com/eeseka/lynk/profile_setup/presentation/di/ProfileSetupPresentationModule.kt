package com.eeseka.lynk.profile_setup.presentation.di

import com.eeseka.lynk.profile_setup.presentation.ProfileSetupViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileSetupPresentationModule = module {
    viewModelOf(::ProfileSetupViewModel)
}