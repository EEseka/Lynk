package com.eeseka.lynk.main_shell.presentation.di

import com.eeseka.lynk.main_shell.presentation.MainShellViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainShellPresentationModule = module {
    viewModelOf(::MainShellViewModel)
}