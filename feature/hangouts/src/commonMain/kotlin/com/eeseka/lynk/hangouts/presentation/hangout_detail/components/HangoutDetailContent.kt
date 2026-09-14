package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components.PaymentSection
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components.VotingSection
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.PaymentState
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_GUEST_ID
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_HOST_ID
import com.eeseka.lynk.shared.presentation.preview.previewHangout
import com.eeseka.lynk.shared.presentation.preview.previewSpots
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.toDateTimeLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_about
import lynk.feature.hangouts.generated.resources.detail_complete
import lynk.feature.hangouts.generated.resources.detail_completing
import org.jetbrains.compose.resources.stringResource

@Composable
fun HangoutDetailContent(
    hangout: HangoutUi,
    isHost: Boolean,
    currentUserId: String?,
    presentUserIds: ImmutableSet<String>,
    candidates: ImmutableList<SpotUi>,
    votes: ImmutableMap<String, String>,
    removingSpotIds: ImmutableSet<String>,
    tiedSpotIds: ImmutableList<String>,
    isClosingVoting: Boolean,
    isCompleting: Boolean,
    canCopyAddress: Boolean,
    hasUnpaidGuests: Boolean,
    hasCurrentUserPaid: Boolean,
    canPay: Boolean,
    isInitializingPayment: Boolean,
    isAwaitingPaymentReturn: Boolean,
    isVerifyingPayment: Boolean,
    canChangeDeadline: Boolean,
    isChangingDeadline: Boolean,
    needsDeadlineDecision: Boolean,
    isDecidingAtDeadline: Boolean,
    canRetryPayout: Boolean,
    isRetryingPayout: Boolean,
    onSeeAllParticipantsClick: () -> Unit,
    onChosenSpotClick: () -> Unit,
    onCopyAddressClick: () -> Unit,
    paymentSetup: @Composable () -> Unit,
    onPayClick: () -> Unit,
    onCheckPaymentClick: () -> Unit,
    onChangeDeadlineClick: () -> Unit,
    onDecideClick: () -> Unit,
    onRetryPayoutClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onCastVote: (String) -> Unit,
    onRemoveSpot: (String) -> Unit,
    onProposeClick: () -> Unit,
    onCloseVoting: () -> Unit,
    onBreakTie: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    heroActions: @Composable RowScope.() -> Unit = {}
) {
    val configuration = currentDeviceConfiguration()
    val contentMaxWidth = if (configuration.isMobile) Dp.Unspecified else 640.dp
    val hapticFeedback = rememberAppHaptic()

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .clearFocusOnTap()
            .padding(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = contentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HangoutHero(
                name = hangout.name,
                vibe = hangout.vibe,
                status = hangout.status,
                scheduledDate = hangout.scheduledAt.toDateTimeLabel(),
                isHost = isHost,
                actions = heroActions
            )

            hangout.description?.takeIf { it.isNotBlank() }?.let { description ->
                DetailSection(title = stringResource(Res.string.detail_about)) {
                    LynkText(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ParticipantsSection(
                participants = hangout.participants,
                participantCount = hangout.participantCount,
                maxAttendees = hangout.maxAttendees,
                presentUserIds = presentUserIds,
                onSeeAllClick = onSeeAllParticipantsClick
            )

            if (hangout.status == HangoutStatus.VOTING) {
                VotingSection(
                    candidates = candidates,
                    votes = votes,
                    removingSpotIds = removingSpotIds,
                    currentUserId = currentUserId,
                    isHost = isHost,
                    tiedSpotIds = tiedSpotIds,
                    isClosingVoting = isClosingVoting,
                    onCastVote = onCastVote,
                    onRemoveSpot = onRemoveSpot,
                    onProposeClick = onProposeClick,
                    onCloseVoting = onCloseVoting,
                    onBreakTie = onBreakTie
                )
            } else {
                ChosenSpotSection(
                    chosenSpot = hangout.chosenSpot,
                    onSpotClick = onChosenSpotClick,
                    canCopyAddress = canCopyAddress,
                    onCopyAddressClick = onCopyAddressClick
                )
            }

            paymentSetup()

            hangout.payment?.let { payment ->
                PaymentSection(
                    payment = payment,
                    isHost = isHost,
                    hasUnpaidGuests = hasUnpaidGuests,
                    hasCurrentUserPaid = hasCurrentUserPaid,
                    canPay = canPay,
                    isInitializingPayment = isInitializingPayment,
                    isAwaitingPaymentReturn = isAwaitingPaymentReturn,
                    isVerifyingPayment = isVerifyingPayment,
                    canChangeDeadline = canChangeDeadline,
                    isChangingDeadline = isChangingDeadline,
                    needsDeadlineDecision = needsDeadlineDecision,
                    isDecidingAtDeadline = isDecidingAtDeadline,
                    canRetryPayout = canRetryPayout,
                    isRetryingPayout = isRetryingPayout,
                    onPayClick = onPayClick,
                    onCheckPaymentClick = onCheckPaymentClick,
                    onChangeDeadlineClick = onChangeDeadlineClick,
                    onDecideClick = onDecideClick,
                    onRetryPayoutClick = onRetryPayoutClick
                )
            }

            if (isHost && hangout.status == HangoutStatus.ONGOING) {
                LynkButton(
                    text = stringResource(Res.string.detail_complete),
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onCompleteClick()
                    },
                    style = LynkButtonStyle.PRIMARY,
                    isLoading = isCompleting,
                    loadingText = stringResource(Res.string.detail_completing)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@PreviewLightDark
@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun HangoutDetailContentScheduledPreview() = HangoutDetailContentPreview()

@PreviewLightDark
@Composable
private fun HangoutDetailContentVotingPreview() = HangoutDetailContentPreview(
    hangout = previewHangout(status = HangoutStatus.VOTING, withSpot = false),
    candidates = previewSpots,
    votes = persistentMapOf("user-2" to "s1", "user-3" to "s1", "user-4" to "s2", PREVIEW_HOST_ID to "s3")
)

@PreviewLightDark
@Composable
private fun HangoutDetailContentGuestOwesPreview() = HangoutDetailContentPreview(
    hangout = previewHangout(paymentState = PaymentState.COLLECTING),
    isHost = false,
    canPay = true
)

@Composable
private fun HangoutDetailContentPreview(
    hangout: HangoutUi = previewHangout(),
    isHost: Boolean = true,
    candidates: ImmutableList<SpotUi> = persistentListOf(),
    votes: ImmutableMap<String, String> = persistentMapOf(),
    canPay: Boolean = false
) {
    val currentUserId = if (isHost) PREVIEW_HOST_ID else PREVIEW_GUEST_ID
    LynkTheme {
        HangoutDetailContent(
            hangout = hangout,
            isHost = isHost,
            currentUserId = currentUserId,
            presentUserIds = persistentSetOf(PREVIEW_HOST_ID, "user-2"),
            candidates = candidates,
            votes = votes,
            removingSpotIds = persistentSetOf(),
            tiedSpotIds = persistentListOf(),
            isClosingVoting = false,
            isCompleting = false,
            canCopyAddress = hangout.chosenSpot != null,
            hasUnpaidGuests = hangout.participants.any {
                it.rsvpStatus == RsvpStatus.ATTENDING && !it.hasPaid
            },
            hasCurrentUserPaid = hangout.participants.any {
                it.user.userId == currentUserId && it.hasPaid
            },
            canPay = canPay,
            isInitializingPayment = false,
            isAwaitingPaymentReturn = false,
            isVerifyingPayment = false,
            canChangeDeadline = false,
            isChangingDeadline = false,
            needsDeadlineDecision = false,
            isDecidingAtDeadline = false,
            canRetryPayout = false,
            isRetryingPayout = false,
            onSeeAllParticipantsClick = {},
            onChosenSpotClick = {},
            onCopyAddressClick = {},
            paymentSetup = {},
            onPayClick = {},
            onCheckPaymentClick = {},
            onChangeDeadlineClick = {},
            onDecideClick = {},
            onRetryPayoutClick = {},
            onCompleteClick = {},
            onCastVote = {},
            onRemoveSpot = {},
            onProposeClick = {},
            onCloseVoting = {},
            onBreakTie = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}
