package com.eeseka.lynk.hangouts.presentation.hangout_detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.DeadlineChangeIntent
import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.PaymentQuoteUi
import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.SearchTab
import com.eeseka.lynk.hangouts.presentation.model.BankUi
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.datetime.LocalDate

@Stable
data class HangoutDetailState(
    val hangout: HangoutUi? = null,
    val currentUserId: String? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val presentUserIds: Set<String> = emptySet(),
    val isCompleting: Boolean = false,
    val isCancelling: Boolean = false,
    val isLeaving: Boolean = false,
    val withdrawingUserIds: Set<String> = emptySet(),

    // Invite search sheet
    val isInviteSheetOpen: Boolean = false,
    val inviteQueryState: TextFieldState = TextFieldState(),
    val inviteResult: HangoutUserUi? = null,
    val isInviteSearching: Boolean = false,
    val inviteNotFound: Boolean = false,
    val isInviting: Boolean = false,

    // Voting
    val candidates: List<SpotUi> = emptyList(),
    val votes: Map<String, String> = emptyMap(), // userId -> spotId
    val center: LocationCoordinates? = null,
    val myLocation: LocationCoordinates? = null,
    val tiedSpotIds: List<String> = emptyList(),
    val isClosingVoting: Boolean = false,

    // Propose-spot sheet
    val isProposeSpotSheetOpen: Boolean = false,
    val activeProposeSpotSheetSearchTab: SearchTab = SearchTab.ALL_SPOTS,
    val proposeSpotSheetSearchTextState: TextFieldState = TextFieldState(),

    val trendingSpots: List<SpotUi> = emptyList(),
    val isTrendingLoading: Boolean = false,

    val spotSearchResults: List<SpotUi> = emptyList(),
    val isSpotSearchLoading: Boolean = false,
    val spotSearchError: UiText? = null,
    val spotSearchEndReached: Boolean = false,
    val spotSearchResetEpoch: Int = 0,

    val favoriteSpotSearchResults: List<SpotUi> = emptyList(),
    val isFavoriteSpotSearchLoading: Boolean = false,
    val favoriteSpotSearchError: UiText? = null,
    val favoriteSpotSearchEndReached: Boolean = false,
    val favoriteSearchResetEpoch: Int = 0,

    val proposingSpotIds: Set<String> = emptySet(),
    val removingSpotIds: Set<String> = emptySet(),

    val isCollectPaymentsOn: Boolean = false,
    val totalCostState: TextFieldState = TextFieldState(),
    val totalCostError: UiText? = null,
    val paymentDeadlineDate: LocalDate? = null,
    val paymentDeadlineError: UiText? = null,
    val isPaymentDeadlinePickerOpen: Boolean = false,
    val isEnablingPayments: Boolean = false,

    // Bank picker, opened from the inline setup form
    val isBankPickerOpen: Boolean = false,
    val bankSearchState: TextFieldState = TextFieldState(),
    val allBanks: List<BankUi> = emptyList(),
    val bankResults: List<BankUi> = emptyList(),
    val isLoadingBanks: Boolean = false,
    val bankLoadError: UiText? = null,
    val selectedBank: BankUi? = null,

    // Account resolution, which runs on its own once a bank and 10 digits are in
    val accountNumberState: TextFieldState = TextFieldState(),
    val resolvedAccountName: String? = null,
    val isResolvingAccount: Boolean = false,
    val accountResolutionError: UiText? = null,

    val canEnablePayments: Boolean = false,

    val pendingDeadlineChange: DeadlineChangeIntent? = null,

    // The four-way answer the server waits for once a deadline lapses with people unpaid.
    val isDeadlineDecisionSheetOpen: Boolean = false,

    val isRetryingPayout: Boolean = false,

    val isInitializingPayment: Boolean = false,
    val paymentQuote: PaymentQuoteUi? = null,

    // Paystack's checkout page.
    val paymentCheckoutUrl: String? = null,

    val isAwaitingPaymentReturn: Boolean = false,
    val isVerifyingPayment: Boolean = false
)