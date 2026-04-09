package com.eeseka.lynk.auth.presentation.di

import com.eeseka.lynk.auth.presentation.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::AuthViewModel)
}