package com.eeseka.lynk.assistant.presentation.di

import com.eeseka.lynk.assistant.presentation.AssistantViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val assistantPresentationModule = module {
    viewModelOf(::AssistantViewModel)
}
