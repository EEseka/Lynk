package com.eeseka.lynk.discover.presentation.di

import com.eeseka.lynk.discover.presentation.DiscoverViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val discoverPresentationModule = module {
    viewModelOf(::DiscoverViewModel)
}