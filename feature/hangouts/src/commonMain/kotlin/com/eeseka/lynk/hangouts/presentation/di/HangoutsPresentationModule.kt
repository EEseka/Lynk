package com.eeseka.lynk.hangouts.presentation.di

import com.eeseka.lynk.hangouts.presentation.hangout_album.HangoutAlbumViewModel
import com.eeseka.lynk.hangouts.presentation.hangout_detail.HangoutDetailViewModel
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.HangoutLiveLocationViewModel
import com.eeseka.lynk.hangouts.presentation.hangout_detail.memories.HangoutMemoriesViewModel
import com.eeseka.lynk.hangouts.presentation.hangout_detail.notes.HangoutNotesViewModel
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.HangoutPaymentsViewModel
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.HangoutVotingViewModel
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListViewModel
import com.eeseka.lynk.hangouts.presentation.hangouts_list_detail.HangoutsListDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val hangoutsPresentationModule = module {
    viewModelOf(::HangoutsListDetailViewModel)
    viewModelOf(::HangoutsListViewModel)
    viewModelOf(::HangoutDetailViewModel)
    viewModelOf(::HangoutVotingViewModel)
    viewModelOf(::HangoutLiveLocationViewModel)
    viewModelOf(::HangoutPaymentsViewModel)
    viewModelOf(::HangoutMemoriesViewModel)
    viewModelOf(::HangoutNotesViewModel)
    viewModelOf(::HangoutAlbumViewModel)
}