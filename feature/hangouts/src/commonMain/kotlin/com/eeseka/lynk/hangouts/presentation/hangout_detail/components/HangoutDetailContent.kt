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
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.PaymentState
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutPaymentUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
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
import kotlinx.collections.immutable.toImmutableList
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_about
import lynk.feature.hangouts.generated.resources.detail_complete
import lynk.feature.hangouts.generated.resources.detail_completing
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

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

private val previewSpot = SpotUi(
    id = "s1",
    name = "The Rooftop Lounge",
    description = "Skyline views",
    photoUrls = persistentListOf(),
    category = SpotCategory.RESTAURANT,
    tags = persistentListOf(),
    priceLevel = null,
    rating = 4.6,
    reviewCount = 214,
    isOpenNow = true,
    shortAddress = "12 Admiralty Way, Lekki",
    latitude = 6.4,
    longitude = 3.4,
    websiteUrl = null,
    googleMapsUrl = null,
    isSaved = false
)

private val previewCandidates = persistentListOf(
    previewSpot,
    previewSpot.copy(
        id = "s2",
        name = "Nomad Beach Bar",
        shortAddress = "8 Elegushi Rd, Lekki"
    ),
    previewSpot.copy(
        id = "s3",
        name = "Craft & Co",
        shortAddress = "3 Karimu Kotun St, VI"
    )
)

private const val PREVIEW_HOST_ID = "host-1"
private const val PREVIEW_GUEST_ID = "guest-1"

private fun previewHangout(
    status: HangoutStatus = HangoutStatus.SCHEDULED,
    withSpot: Boolean = true,
    paymentState: PaymentState? = null,
    paidGuestCount: Int = 0
) = HangoutUi(
    id = "1",
    hostId = PREVIEW_HOST_ID,
    name = "Rooftop Party in Lekki",
    description = "Bring your best vibes. We'll sort drinks and music, you just show up.",
    vibe = HangoutVibe.PARTY,
    status = status,
    scheduledAt = Instant.fromEpochSeconds(1_800_000_000L),
    maxAttendees = 10,
    participantCount = 6,
    chosenSpot = if (withSpot) previewSpot else null,
    participants = List(6) { index ->
        HangoutParticipantUi(
            user = HangoutUserUi(
                userId = when (index) {
                    0 -> PREVIEW_HOST_ID
                    1 -> PREVIEW_GUEST_ID
                    else -> "user-$index"
                },
                username = if (index == 0) "the.host" else "guest$index",
                displayName = if (index == 0) "Ada Obi" else "Guest $index",
                initials = if (index == 0) "AO" else "G$index",
                profilePictureUrl = null
            ),
            rsvpStatus = RsvpStatus.ATTENDING,
            // The host is created already paid, exactly as the server does it.
            hasPaid = index == 0 || index <= paidGuestCount
        )
    }.toImmutableList(),
    payment = paymentState?.let {
        HangoutPaymentUi(
            totalCostKobo = 2_400_000L,
            costPerPersonKobo = 400_000L,
            splitHeadcount = 6,
            deadline = Instant.fromEpochSeconds(1_799_000_000L),
            state = it
        )
    },
    createdAt = Instant.fromEpochSeconds(1_790_000_000L)
)

@Composable
private fun HangoutDetailContentPreview(
    hangout: HangoutUi = previewHangout(),
    isHost: Boolean = true,
    candidates: ImmutableList<SpotUi> = persistentListOf(),
    votes: ImmutableMap<String, String> = persistentMapOf(),
    tiedSpotIds: ImmutableList<String> = persistentListOf(),
    isClosingVoting: Boolean = false,
    isCompleting: Boolean = false,
    canPay: Boolean = false,
    isAwaitingPaymentReturn: Boolean = false,
    canChangeDeadline: Boolean = false,
    needsDeadlineDecision: Boolean = false,
    canRetryPayout: Boolean = false
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
            tiedSpotIds = tiedSpotIds,
            isClosingVoting = isClosingVoting,
            isCompleting = isCompleting,
            canCopyAddress = hangout.chosenSpot != null,
            hasUnpaidGuests = hangout.participants.any {
                it.rsvpStatus == RsvpStatus.ATTENDING && !it.hasPaid
            },
            hasCurrentUserPaid = hangout.participants.any {
                it.user.userId == currentUserId && it.hasPaid
            },
            canPay = canPay,
            isInitializingPayment = false,
            isAwaitingPaymentReturn = isAwaitingPaymentReturn,
            isVerifyingPayment = false,
            canChangeDeadline = canChangeDeadline,
            isChangingDeadline = false,
            needsDeadlineDecision = needsDeadlineDecision,
            isDecidingAtDeadline = false,
            canRetryPayout = canRetryPayout,
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

@PreviewLightDark
@Composable
private fun HangoutDetailContentScheduledPreview() = HangoutDetailContentPreview()

@PreviewLightDark
@Composable
private fun HangoutDetailContentAttendeePreview() = HangoutDetailContentPreview(isHost = false)

@PreviewLightDark
@Composable
private fun HangoutDetailContentVotingPreview() = HangoutDetailContentPreview(
    hangout = previewHangout(status = HangoutStatus.VOTING, withSpot = false),
    candidates = previewCandidates,
    votes = persistentMapOf("user-2" to "s1", "user-3" to "s1", "user-4" to "s2", PREVIEW_HOST_ID to "s3")
)

@PreviewLightDark
@Composable
private fun HangoutDetailContentVotingEmptyPreview() = HangoutDetailContentPreview(
    hangout = previewHangout(status = HangoutStatus.VOTING, withSpot = false)
)

@PreviewLightDark
@Composable
private fun HangoutDetailContentVotingTiePreview() = HangoutDetailContentPreview(
    hangout = previewHangout(status = HangoutStatus.VOTING, withSpot = false),
    candidates = previewCandidates,
    votes = persistentMapOf("user-2" to "s1", "user-3" to "s2"),
    tiedSpotIds = persistentListOf("s1", "s2")
)

@PreviewLightDark
@Composable
private fun HangoutDetailContentOngoingPreview() = HangoutDetailContentPreview(
    hangout = previewHangout(status = HangoutStatus.ONGOING)
)

@PreviewLightDark
@Composable
private fun HangoutDetailContentCompletedPreview() = HangoutDetailContentPreview(
    hangout = previewHangout(status = HangoutStatus.COMPLETED)
)

@PreviewLightDark
@Composable
private fun HangoutDetailContentCancelledNoSpotPreview() = HangoutDetailContentPreview(
    hangout = previewHangout(status = HangoutStatus.CANCELLED, withSpot = false)
)

@PreviewLightDark
@Composable
private fun PaymentHostCollectingPreview() = HangoutDetailContentPreview(
    hangout = previewHangout(paymentState = PaymentState.COLLECTING, paidGuestCount = 2),
    canChangeDeadline = true
)

@PreviewLightDark
@Composable
private fun PaymentGuestOwesPreview() = HangoutDetailContentPreview(
    hangout = previewHangout(paymentState = PaymentState.COLLECTING, paidGuestCount = 0),
    isHost = false,
    canPay = true
)

@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun HangoutDetailContentTabletPreview() = HangoutDetailContentPreview()
