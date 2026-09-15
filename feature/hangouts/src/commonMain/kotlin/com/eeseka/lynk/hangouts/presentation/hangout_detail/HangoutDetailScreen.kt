package com.eeseka.lynk.hangouts.presentation.hangout_detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.ConnectionBanner
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.DetailEmptyState
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.HangoutDetailContent
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.HangoutDetailTopBar
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.HangoutHeroActions
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.InviteParticipantSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.ParticipantsSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.detailOverflowItems
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.HangoutPaymentsAction
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.HangoutPaymentsEvent
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.HangoutPaymentsState
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.HangoutPaymentsViewModel
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components.BankPickerSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components.CollectPaymentsSetup
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components.DeadlineDecisionSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components.PayConfirmSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components.PaymentCheckoutSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components.PaymentDeadlinePickerSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.HangoutVotingAction
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.HangoutVotingEvent
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.HangoutVotingState
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.HangoutVotingViewModel
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.LocationShareEffect
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components.ProposeSpotSheet
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_ATTENDEES
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MIN_ATTENDEES_FOR_PAYMENTS
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.PaymentState
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import com.eeseka.lynk.shared.presentation.components.LynkErrorState
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.coroutines.launch
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_address_copied
import lynk.feature.hangouts.generated.resources.detail_cancel_confirm_action
import lynk.feature.hangouts.generated.resources.detail_cancel_confirm_message
import lynk.feature.hangouts.generated.resources.detail_cancel_confirm_title
import lynk.feature.hangouts.generated.resources.detail_cancelled_message
import lynk.feature.hangouts.generated.resources.detail_complete_confirm_action
import lynk.feature.hangouts.generated.resources.detail_complete_confirm_message
import lynk.feature.hangouts.generated.resources.detail_complete_confirm_title
import lynk.feature.hangouts.generated.resources.detail_completed_message
import lynk.feature.hangouts.generated.resources.detail_dialog_dismiss
import lynk.feature.hangouts.generated.resources.detail_invited_message
import lynk.feature.hangouts.generated.resources.detail_leave_confirm_action
import lynk.feature.hangouts.generated.resources.detail_leave_confirm_message
import lynk.feature.hangouts.generated.resources.detail_leave_confirm_title
import lynk.feature.hangouts.generated.resources.detail_left_message
import lynk.feature.hangouts.generated.resources.detail_load_error_title
import lynk.feature.hangouts.generated.resources.detail_withdrawn_message
import lynk.feature.hangouts.generated.resources.payment_deadline_changed
import lynk.feature.hangouts.generated.resources.payment_decision_cancel_confirm_message
import lynk.feature.hangouts.generated.resources.payment_decision_cancel_confirm_title
import lynk.feature.hangouts.generated.resources.payment_decision_saved
import lynk.feature.hangouts.generated.resources.payment_enabled_message
import lynk.feature.hangouts.generated.resources.payment_not_completed
import lynk.feature.hangouts.generated.resources.payment_payout_queued
import lynk.feature.hangouts.generated.resources.payment_still_processing
import lynk.feature.hangouts.generated.resources.spot_suggested_message
import lynk.feature.hangouts.generated.resources.voting_tie_flash
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

private sealed interface DetailBodyState {
    data object Loading : DetailBodyState
    data class Content(val hangout: HangoutUi) : DetailBodyState
    data class Error(val message: UiText) : DetailBodyState
    data object Empty : DetailBodyState
}

@Composable
fun HangoutDetailRoot(
    hangoutId: String?,
    isDetailPaneFullScreen: Boolean,
    navigateBack: () -> Unit,
    onHangoutLeft: () -> Unit,
    onEditHangoutClick: (HangoutUi) -> Unit,
    viewModel: HangoutDetailViewModel = koinViewModel(),
    votingViewModel: HangoutVotingViewModel = koinViewModel(),
    paymentsViewModel: HangoutPaymentsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val votingState by votingViewModel.state.collectAsStateWithLifecycle()
    val paymentsState by paymentsViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HangoutDetailEvent.Error -> snackbarHostState.showFlashMessage(
                message = event.message.asStringAsync(),
                type = LynkFlashType.Error
            )

            HangoutDetailEvent.HangoutCompleted -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.detail_completed_message),
                type = LynkFlashType.Success
            )

            HangoutDetailEvent.HangoutCancelled -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.detail_cancelled_message),
                type = LynkFlashType.Success
            )

            HangoutDetailEvent.HangoutLeft -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.detail_left_message),
                type = LynkFlashType.Success
            )

            HangoutDetailEvent.InviteSent -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.detail_invited_message),
                type = LynkFlashType.Success
            )

            HangoutDetailEvent.InviteWithdrawn -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.detail_withdrawn_message),
                type = LynkFlashType.Success
            )

            is HangoutDetailEvent.LobbyAnnouncement -> snackbarHostState.showFlashMessage(
                message = event.message.asStringAsync(),
                type = event.type
            )

            HangoutDetailEvent.NavigateBack -> onHangoutLeft()
        }
    }

    ObserveAsEvents(votingViewModel.events) { event ->
        when (event) {
            is HangoutVotingEvent.Error -> snackbarHostState.showFlashMessage(
                message = event.message.asStringAsync(),
                type = LynkFlashType.Error
            )

            HangoutVotingEvent.SpotSuggested -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.spot_suggested_message),
                type = LynkFlashType.Success
            )

            HangoutVotingEvent.VotingTied -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.voting_tie_flash),
                type = LynkFlashType.Warning
            )
        }
    }

    ObserveAsEvents(paymentsViewModel.events) { event ->
        when (event) {
            is HangoutPaymentsEvent.Error -> snackbarHostState.showFlashMessage(
                message = event.message.asStringAsync(),
                type = LynkFlashType.Error
            )

            HangoutPaymentsEvent.PaymentsEnabled -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.payment_enabled_message),
                type = LynkFlashType.Success
            )

            HangoutPaymentsEvent.DeadlineChanged -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.payment_deadline_changed),
                type = LynkFlashType.Success
            )

            HangoutPaymentsEvent.DecisionSaved -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.payment_decision_saved),
                type = LynkFlashType.Success
            )

            HangoutPaymentsEvent.PayoutQueued -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.payment_payout_queued),
                type = LynkFlashType.Success
            )

            HangoutPaymentsEvent.PaymentPending -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.payment_still_processing),
                type = LynkFlashType.Info
            )

            HangoutPaymentsEvent.PaymentNotCompleted -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.payment_not_completed),
                type = LynkFlashType.Error
            )
        }
    }

    LaunchedEffect(hangoutId) {
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        votingViewModel.onAction(HangoutVotingAction.OnSelectHangout(hangoutId))
        paymentsViewModel.onAction(HangoutPaymentsAction.OnSelectHangout(hangoutId))
    }

    HangoutDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        votingState = votingState,
        onVotingAction = votingViewModel::onAction,
        paymentsState = paymentsState,
        onPaymentsAction = paymentsViewModel::onAction,
        snackbarHostState = snackbarHostState,
        isDetailPaneFullScreen = isDetailPaneFullScreen,
        navigateBack = navigateBack,
        onEditClick = { state.hangout?.let(onEditHangoutClick) }
    )
}

@Composable
fun HangoutDetailScreen(
    state: HangoutDetailState,
    onAction: (HangoutDetailAction) -> Unit,
    votingState: HangoutVotingState,
    onVotingAction: (HangoutVotingAction) -> Unit,
    paymentsState: HangoutPaymentsState,
    onPaymentsAction: (HangoutPaymentsAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    isDetailPaneFullScreen: Boolean,
    navigateBack: () -> Unit,
    onEditClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = rememberAppHaptic()

    var showParticipantsSheet by remember { mutableStateOf(false) }
    var showChosenSpotSheet by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var pendingCancelDecision by remember { mutableStateOf(false) }

    val hangout = state.hangout
    val isHost = hangout != null && hangout.hostId == state.currentUserId
    val isUpcoming = hangout != null &&
            (hangout.status == HangoutStatus.VOTING || hangout.status == HangoutStatus.SCHEDULED)
    val showInvite = isHost && isUpcoming &&
            hangout.payment.let { it == null || it.state == PaymentState.COLLECTING }
    val inviteEnabled = run {
        if (hangout == null) return@run true
        val activeCount = hangout.participants.count {
            it.rsvpStatus == RsvpStatus.ATTENDING || it.rsvpStatus == RsvpStatus.PENDING
        }
        val capacity = hangout.maxAttendees ?: MAX_ATTENDEES
        val ceiling = hangout.payment?.splitHeadcount?.let { minOf(capacity, it) } ?: capacity
        activeCount < ceiling
    }
    val canCancel = hangout != null && isHost &&
            hangout.status != HangoutStatus.COMPLETED &&
            hangout.status != HangoutStatus.CANCELLED &&
            hangout.payment?.state != PaymentState.PAYING_OUT &&
            hangout.payment?.state != PaymentState.PAID_OUT
    val canLeave = hangout != null && !isHost && isUpcoming
    val canEdit = isHost && isUpcoming
    val canComplete = hangout != null && isHost && hangout.status == HangoutStatus.ONGOING

    val isWithinPaymentDeadline = hangout?.payment?.let { Clock.System.now() < it.deadline } == true

    val canPay = hangout != null &&
            !isHost &&
            hangout.payment?.state == PaymentState.COLLECTING &&
            hangout.status == HangoutStatus.SCHEDULED &&
            isWithinPaymentDeadline &&
            hangout.participants.any { it.user.userId == state.currentUserId && it.rsvpStatus == RsvpStatus.ATTENDING && !it.hasPaid }

    val canChangeDeadline = isHost &&
            hangout.payment?.state == PaymentState.COLLECTING &&
            hangout.status == HangoutStatus.SCHEDULED

    val unpaidCount = hangout?.participants?.count {
        it.rsvpStatus == RsvpStatus.ATTENDING && !it.hasPaid
    } ?: 0

    val hasCurrentUserPaid = hangout?.participants?.any {
        it.user.userId == state.currentUserId && it.hasPaid
    } == true

    val needsDeadlineDecision = isHost &&
            hangout.payment?.state == PaymentState.AWAITING_HOST_DECISION &&
            hangout.status == HangoutStatus.SCHEDULED &&
            unpaidCount > 0

    val canRetryPayout = isHost && hangout.payment?.state == PaymentState.PAYOUT_FAILED

    val canCopyAddress = hangout?.chosenSpot != null &&
            (hangout.status == HangoutStatus.SCHEDULED || hangout.status == HangoutStatus.ONGOING)
    val addressCopiedMessage = stringResource(Res.string.detail_address_copied)
    val onCopyAddressClick = {
        val spot = hangout?.chosenSpot
        if (spot != null) {
            clipboardManager.setText(
                AnnotatedString(spot.shortAddress ?: "${spot.latitude}, ${spot.longitude}")
            )
            coroutineScope.launch {
                snackbarHostState.showFlashMessage(
                    message = addressCopiedMessage,
                    type = LynkFlashType.Info
                )
            }
        }
    }

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            if (isDetailPaneFullScreen) {
                HangoutDetailTopBar(
                    showInvite = showInvite,
                    inviteEnabled = inviteEnabled,
                    canEdit = canEdit,
                    canCancel = canCancel,
                    canLeave = canLeave,
                    isCancelling = state.isCancelling,
                    isLeaving = state.isLeaving,
                    isOverflowExpanded = showOverflowMenu,
                    onOverflowExpandedChange = { showOverflowMenu = it },
                    onBackClick = navigateBack,
                    onInviteClick = { onAction(HangoutDetailAction.OnInviteClick) },
                    onEditClick = onEditClick,
                    onCancelClick = { showCancelDialog = true },
                    onLeaveClick = { showLeaveDialog = true }
                )
            }
        }
    ) { scaffoldPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            val bodyState = when {
                state.isLoading -> DetailBodyState.Loading
                state.hangout != null -> DetailBodyState.Content(state.hangout)
                state.error != null -> DetailBodyState.Error(state.error)
                else -> DetailBodyState.Empty
            }

            AnimatedContent(
                targetState = bodyState,
                contentKey = { it::class },
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "hangout_detail_body",
                modifier = Modifier.fillMaxSize()
            ) { target ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (target) {
                        DetailBodyState.Loading -> {
                            LynkProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        is DetailBodyState.Content -> {
                            HangoutDetailContent(
                                hangout = target.hangout,
                                isHost = isHost,
                                currentUserId = state.currentUserId,
                                presentUserIds = state.presentUserIds,
                                candidates = votingState.candidates,
                                votes = votingState.votes,
                                removingSpotIds = votingState.removingSpotIds,
                                tiedSpotIds = votingState.tiedSpotIds,
                                isClosingVoting = votingState.isClosingVoting,
                                isCompleting = state.isCompleting,
                                canCopyAddress = canCopyAddress,
                                hasUnpaidGuests = unpaidCount > 0,
                                hasCurrentUserPaid = hasCurrentUserPaid,
                                canPay = canPay && !paymentsState.isAwaitingPaymentReturn,
                                isInitializingPayment = paymentsState.isInitializingPayment,
                                isAwaitingPaymentReturn = paymentsState.isAwaitingPaymentReturn,
                                isVerifyingPayment = paymentsState.isVerifyingPayment,
                                canChangeDeadline = canChangeDeadline,
                                isChangingDeadline = paymentsState.isChangingDeadline,
                                needsDeadlineDecision = needsDeadlineDecision,
                                isDecidingAtDeadline = paymentsState.isDecidingAtDeadline,
                                canRetryPayout = canRetryPayout,
                                isRetryingPayout = paymentsState.isRetryingPayout,
                                onSeeAllParticipantsClick = { showParticipantsSheet = true },
                                onChosenSpotClick = { showChosenSpotSheet = true },
                                onCopyAddressClick = onCopyAddressClick,
                                heroActions = {
                                    if (!isDetailPaneFullScreen) {
                                        HangoutHeroActions(
                                            showInvite = showInvite,
                                            inviteEnabled = inviteEnabled,
                                            overflowItems = detailOverflowItems(
                                                canEdit = canEdit,
                                                canCancel = canCancel,
                                                canLeave = canLeave,
                                                isCancelling = state.isCancelling,
                                                isLeaving = state.isLeaving,
                                                onEditClick = onEditClick,
                                                onCancelClick = { showCancelDialog = true },
                                                onLeaveClick = { showLeaveDialog = true }
                                            ),
                                            isOverflowExpanded = showOverflowMenu,
                                            onOverflowExpandedChange = { showOverflowMenu = it },
                                            onInviteClick = {
                                                onAction(HangoutDetailAction.OnInviteClick)
                                            }
                                        )
                                    }
                                },
                                paymentSetup = {
                                    val attendingCount = target.hangout.participants.count {
                                        it.rsvpStatus == RsvpStatus.ATTENDING
                                    }
                                    if (isHost &&
                                        target.hangout.status == HangoutStatus.SCHEDULED &&
                                        target.hangout.payment == null &&
                                        attendingCount >= MIN_ATTENDEES_FOR_PAYMENTS
                                    ) {
                                        CollectPaymentsSetup(
                                            state = paymentsState,
                                            onAction = onPaymentsAction
                                        )
                                    }
                                },
                                onPayClick = { onPaymentsAction(HangoutPaymentsAction.OnPayClick) },
                                onCheckPaymentClick = {
                                    onPaymentsAction(HangoutPaymentsAction.OnCheckPaymentClick)
                                },
                                onChangeDeadlineClick = {
                                    onPaymentsAction(HangoutPaymentsAction.OnChangeDeadlineClick)
                                },
                                onDecideClick = {
                                    onPaymentsAction(HangoutPaymentsAction.OnDeadlineDecisionClick)
                                },
                                onRetryPayoutClick = {
                                    onPaymentsAction(HangoutPaymentsAction.OnRetryPayoutClick)
                                },
                                onCompleteClick = { showCompleteDialog = true },
                                onCastVote = { onVotingAction(HangoutVotingAction.OnCastVote(it)) },
                                onRemoveSpot = { onVotingAction(HangoutVotingAction.OnRemoveSpot(it)) },
                                onProposeClick = { onVotingAction(HangoutVotingAction.OnProposeSpotClick) },
                                onCloseVoting = { onVotingAction(HangoutVotingAction.OnCloseVotingClick) },
                                onBreakTie = { onVotingAction(HangoutVotingAction.OnBreakTie(it)) },
                                contentPadding = scaffoldPadding
                            )
                        }

                        is DetailBodyState.Error -> {
                            LynkErrorState(
                                title = stringResource(Res.string.detail_load_error_title),
                                message = target.message.asString(),
                                onRetry = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    onAction(HangoutDetailAction.OnRetryClick)
                                }
                            )
                        }

                        DetailBodyState.Empty -> DetailEmptyState()
                    }
                }
            }

            if (state.hangout != null &&
                state.hangout.status != HangoutStatus.COMPLETED &&
                state.hangout.status != HangoutStatus.CANCELLED
            ) {
                ConnectionBanner(
                    connectionState = state.connectionState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = scaffoldPadding.calculateTopPadding())
                )
            }
        }
    }

    if (showParticipantsSheet && state.hangout != null) {
        ParticipantsSheet(
            participants = state.hangout.participants,
            isHost = isHost,
            onDismiss = { showParticipantsSheet = false },
            onWithdraw = { onAction(HangoutDetailAction.OnWithdrawParticipantInvite(it)) },
            withdrawingUserIds = state.withdrawingUserIds,
            presentUserIds = state.presentUserIds,
            arePaymentsOn = state.hangout.payment != null,
            hostId = state.hangout.hostId
        )
    }

    val chosenSpot = state.hangout?.chosenSpot
    if (showChosenSpotSheet && chosenSpot != null) {
        SpotDetailSheet(
            spot = chosenSpot,
            userLat = votingState.center?.latitude ?: votingState.myLocation?.latitude,
            userLng = votingState.center?.longitude ?: votingState.myLocation?.longitude,
            onDismissRequest = { showChosenSpotSheet = false },
            onToggleSave = { spotId, isCurrentlySaved ->
                onAction(HangoutDetailAction.OnToggleSaveSpot(spotId, isCurrentlySaved))
            }
        )
    }

    if (state.isInviteSheetOpen) {
        val resultId = state.inviteResult?.userId
        val alreadyInvited = resultId != null && state.hangout?.participants?.any {
            it.user.userId == resultId && it.rsvpStatus != RsvpStatus.DECLINED
        } == true
        // Only the host opens this sheet, so a self-match is the host searching themselves.
        val isResultHost = resultId != null && resultId == state.hangout?.hostId

        InviteParticipantSheet(
            queryState = state.inviteQueryState,
            result = state.inviteResult,
            isSearching = state.isInviteSearching,
            notFound = state.inviteNotFound,
            isInviting = state.isInviting,
            alreadyInvited = alreadyInvited,
            isResultHost = isResultHost,
            onInvite = { onAction(HangoutDetailAction.OnInviteUser(it)) },
            onDismiss = { onAction(HangoutDetailAction.OnDismissInviteSheet) }
        )
    }

    if (votingState.isProposeSpotSheetOpen) {
        ProposeSpotSheet(
            state = votingState,
            onTabSelected = { onVotingAction(HangoutVotingAction.OnTabSelected(it)) },
            onPropose = { onVotingAction(HangoutVotingAction.OnProposeSpot(it)) },
            onLoadNextSpotPage = { onVotingAction(HangoutVotingAction.LoadNextSpotPage) },
            onLoadNextFavoritePage = { onVotingAction(HangoutVotingAction.LoadNextFavoriteSpotPage) },
            onDismiss = { onVotingAction(HangoutVotingAction.OnDismissProposeSpotSheet) }
        )
    }

    if (paymentsState.isPaymentDeadlinePickerOpen) {
        PaymentDeadlinePickerSheet(
            onDateSelected = { onPaymentsAction(HangoutPaymentsAction.OnPaymentDeadlineSelected(it)) },
            onDismiss = { onPaymentsAction(HangoutPaymentsAction.OnDismissPaymentDeadlinePicker) }
        )
    }

    if (paymentsState.isDeadlineDecisionSheetOpen && needsDeadlineDecision) {
        DeadlineDecisionSheet(
            unpaidCount = unpaidCount,
            onDecision = { decision ->
                // Refunding everybody is the one answer worth asking about twice.
                if (decision == DeadlineDecision.CANCEL) {
                    pendingCancelDecision = true
                } else {
                    onPaymentsAction(HangoutPaymentsAction.OnDeadlineDecisionSelected(decision))
                }
            },
            onDismiss = { onPaymentsAction(HangoutPaymentsAction.OnDismissDeadlineDecisionSheet) }
        )
    }

    if (pendingCancelDecision) {
        LynkDialog(
            onDismissRequest = { pendingCancelDecision = false },
            title = stringResource(Res.string.payment_decision_cancel_confirm_title),
            message = stringResource(Res.string.payment_decision_cancel_confirm_message),
            confirmText = stringResource(Res.string.detail_cancel_confirm_action),
            dismissText = stringResource(Res.string.detail_dialog_dismiss),
            isDestructive = true,
            onConfirm = {
                pendingCancelDecision = false
                onPaymentsAction(HangoutPaymentsAction.OnDeadlineDecisionSelected(DeadlineDecision.CANCEL))
            }
        )
    }

    if (paymentsState.pendingDeadlineChange != null) {
        PaymentDeadlinePickerSheet(
            onDateSelected = { onPaymentsAction(HangoutPaymentsAction.OnNewDeadlineSelected(it)) },
            onDismiss = { onPaymentsAction(HangoutPaymentsAction.OnDismissDeadlinePicker) }
        )
    }

    val paymentCheckoutUrl = paymentsState.paymentCheckoutUrl
    if (paymentCheckoutUrl != null) {
        PaymentCheckoutSheet(
            url = paymentCheckoutUrl,
            onDismiss = { onPaymentsAction(HangoutPaymentsAction.OnDismissPaymentCheckout) }
        )
    }

    val paymentQuote = paymentsState.paymentQuote
    if (paymentQuote != null) {
        PayConfirmSheet(
            shareLabel = paymentQuote.shareLabel,
            chargeLabel = paymentQuote.chargeLabel,
            onConfirm = { onPaymentsAction(HangoutPaymentsAction.OnConfirmPayment) },
            onDismiss = { onPaymentsAction(HangoutPaymentsAction.OnDismissPayConfirmSheet) }
        )
    }

    if (paymentsState.isBankPickerOpen) {
        BankPickerSheet(
            banks = paymentsState.bankResults,
            searchState = paymentsState.bankSearchState,
            isLoading = paymentsState.isLoadingBanks,
            errorMessage = paymentsState.bankLoadError?.asString(),
            onBankSelected = { onPaymentsAction(HangoutPaymentsAction.OnBankSelected(it)) },
            onDismiss = { onPaymentsAction(HangoutPaymentsAction.OnDismissBankPicker) }
        )
    }

    if (showCompleteDialog && canComplete) {
        LynkDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = stringResource(Res.string.detail_complete_confirm_title),
            message = stringResource(Res.string.detail_complete_confirm_message),
            confirmText = stringResource(Res.string.detail_complete_confirm_action),
            dismissText = stringResource(Res.string.detail_dialog_dismiss),
            onConfirm = { onAction(HangoutDetailAction.OnCompleteHangoutConfirmed) }
        )
    }

    if (showCancelDialog && canCancel) {
        LynkDialog(
            onDismissRequest = { showCancelDialog = false },
            title = stringResource(Res.string.detail_cancel_confirm_title),
            message = stringResource(Res.string.detail_cancel_confirm_message),
            confirmText = stringResource(Res.string.detail_cancel_confirm_action),
            dismissText = stringResource(Res.string.detail_dialog_dismiss),
            isDestructive = true,
            onConfirm = { onAction(HangoutDetailAction.OnCancelHangoutConfirmed) }
        )
    }

    if (showLeaveDialog && canLeave) {
        LynkDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = stringResource(Res.string.detail_leave_confirm_title),
            message = stringResource(Res.string.detail_leave_confirm_message),
            confirmText = stringResource(Res.string.detail_leave_confirm_action),
            dismissText = stringResource(Res.string.detail_dialog_dismiss),
            isDestructive = true,
            onConfirm = { onAction(HangoutDetailAction.OnLeaveHangoutConfirmed) }
        )
    }

    // One-off device location while voting.
    LocationShareEffect(
        enabled = state.hangout?.status == HangoutStatus.VOTING,
        isConnected = state.connectionState == ConnectionState.CONNECTED,
        onShareLocation = { lat, lng ->
            onVotingAction(HangoutVotingAction.OnShareLocation(lat, lng))
        }
    )
}