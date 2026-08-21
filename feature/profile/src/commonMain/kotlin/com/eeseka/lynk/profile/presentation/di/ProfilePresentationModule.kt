package com.eeseka.lynk.profile.presentation.di

import com.eeseka.lynk.profile.presentation.profile.ProfileViewModel
import com.eeseka.lynk.profile.presentation.saved_spots.SavedSpotsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profilePresentationModule = module {
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SavedSpotsViewModel)
}