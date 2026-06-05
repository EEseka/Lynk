package com.eeseka.lynk.hangouts.presentation.di

import com.eeseka.lynk.hangouts.presentation.HangoutsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val hangoutsPresentationModule = module {
    viewModelOf(::HangoutsViewModel)
}