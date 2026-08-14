package com.eeseka.lynk.create_hangout.presentation.di

import com.eeseka.lynk.create_hangout.presentation.CreateHangoutViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val createHangoutPresentationModule = module {
    viewModelOf(::CreateHangoutViewModel)
}
