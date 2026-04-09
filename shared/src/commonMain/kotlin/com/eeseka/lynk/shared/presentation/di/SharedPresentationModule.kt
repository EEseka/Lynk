package com.eeseka.lynk.shared.presentation.di

import com.eeseka.lynk.shared.presentation.util.ScopedStoreRegistryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedPresentationModule = module {
    viewModelOf(::ScopedStoreRegistryViewModel)
}