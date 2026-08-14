package com.eeseka.lynk.hangouts.presentation.di

import com.eeseka.lynk.hangouts.presentation.hangout_detail.HangoutDetailViewModel
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListViewModel
import com.eeseka.lynk.hangouts.presentation.hangouts_list_detail.HangoutsListDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val hangoutsPresentationModule = module {
    viewModelOf(::HangoutsListDetailViewModel)
    viewModelOf(::HangoutsListViewModel)
    viewModelOf(::HangoutDetailViewModel)
}