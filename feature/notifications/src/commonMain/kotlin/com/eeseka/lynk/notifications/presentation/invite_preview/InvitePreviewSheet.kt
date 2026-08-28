package com.eeseka.lynk.notifications.presentation.invite_preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Users
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.hangout.components.ParticipantStack
import com.eeseka.lynk.shared.presentation.hangout.components.StatusChip
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutPreviewUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.spot.util.getPriceLevelSymbol
import com.eeseka.lynk.shared.presentation.util.DialogSheetScopedViewModel
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.toHangoutDisplayDate
import kotlinx.coroutines.flow.Flow
import lynk.feature.notifications.generated.resources.Res
import lynk.feature.notifications.generated.resources.invite_preview_accept
import lynk.feature.notifications.generated.resources.invite_preview_accepting
import lynk.feature.notifications.generated.resources.invite_preview_attendees
import lynk.feature.notifications.generated.resources.invite_preview_decline
import lynk.feature.notifications.generated.resources.invite_preview_declining
import lynk.feature.notifications.generated.resources.invite_preview_going
import lynk.feature.notifications.generated.resources.invite_preview_going_capped
import lynk.feature.notifications.generated.resources.invite_preview_spot_pending
import lynk.feature.notifications.generated.resources.invite_preview_title
import lynk.feature.notifications.generated.resources.invite_preview_withdrawn
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@Composable
fun InvitePreviewRoot(
    visible: Boolean,
    hangoutId: String?,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onAccepted: (hangoutId: String) -> Unit,
    onAlreadyAnswered: (hangoutId: String) -> Unit
) {
    DialogSheetScopedViewModel(visible = visible) {
        val viewModel = koinViewModel<InvitePreviewViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(hangoutId) {
            hangoutId?.let { viewModel.onAction(InvitePreviewAction.Init(it)) }
        }

        InvitePreviewSheet(
            state = state,
            events = viewModel.events,
            onAction = viewModel::onAction,
            snackbarHostState = snackbarHostState,
            onAccepted = onAccepted,
            onAlreadyAnswered = onAlreadyAnswered,
            onDismissRequest = onDismiss
        )
    }
}

@Composable
fun InvitePreviewSheet(
    state: InvitePreviewState,
    events: Flow<InvitePreviewEvent>,
    onAction: (InvitePreviewAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    onAccepted: (hangoutId: String) -> Unit,
    onAlreadyAnswered: (hangoutId: String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    ObserveAsEvents(events) { event ->
        when (event) {
            is InvitePreviewEvent.Error -> {
                hapticFeedback(AppHaptic.Error)
                snackbarHostState.showFlashMessage(
                    message = event.message.asStringAsync(),
                    type = LynkFlashType.Error
                )
                onDismissRequest()
            }

            is InvitePreviewEvent.Accepted -> {
                hapticFeedback(AppHaptic.Success)
                onAccepted(event.hangoutId)
            }

            is InvitePreviewEvent.AlreadyAnswered -> onAlreadyAnswered(event.hangoutId)

            InvitePreviewEvent.InviteWithdrawn -> {
                snackbarHostState.showFlashMessage(
                    message = getString(Res.string.invite_preview_withdrawn),
                    type = LynkFlashType.Info
                )
                onDismissRequest()
            }

            InvitePreviewEvent.Dismissed -> onDismissRequest()
        }
    }

    LynkAdaptiveSheet(
        onDismissRequest = onDismissRequest,
        skipBottomSheetPartiallyExpanded = false
    ) {
        InvitePreviewSheetContent(
            state = state,
            onAction = onAction,
            modifier = modifier
        )
    }
}

@Composable
private fun InvitePreviewSheetContent(
    state: InvitePreviewState,
    onAction: (InvitePreviewAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val hangoutPreview = state.hangoutPreview

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (state.isLoading || hangoutPreview == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                LynkProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LynkText(
                    text = stringResource(Res.string.invite_preview_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = hangoutPreview.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    StatusChip(status = hangoutPreview.status)
                }
            }

            hangoutPreview.description?.let { description ->
                LynkText(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(
                    icon = hangoutPreview.vibe.getIcon(),
                    text = hangoutPreview.vibe.getTitle()
                )

                DetailRow(
                    icon = Lucide.CalendarClock,
                    text = hangoutPreview.scheduledAt.toHangoutDisplayDate()
                )

                val chosenSpot = hangoutPreview.chosenSpot
                DetailRow(
                    icon = Lucide.MapPin,
                    text = chosenSpot?.name ?: stringResource(Res.string.invite_preview_spot_pending),
                    supportingText = chosenSpot?.let { spot ->
                        listOfNotNull(
                            spot.category.getTitle(),
                            spot.priceLevel?.let { getPriceLevelSymbol(it.tier) },
                            spot.shortAddress
                        ).joinToString(" • ")
                    }
                )

                val maxAttendees = hangoutPreview.maxAttendees
                DetailRow(
                    icon = Lucide.Users,
                    text = if (maxAttendees != null) {
                        stringResource(
                            Res.string.invite_preview_going_capped,
                            hangoutPreview.participantCount,
                            maxAttendees
                        )
                    } else {
                        stringResource(Res.string.invite_preview_going, hangoutPreview.participantCount)
                    }
                )
            }

            if (hangoutPreview.attendees.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LynkText(
                        text = stringResource(Res.string.invite_preview_attendees),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ParticipantStack(users = hangoutPreview.attendees)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isResponding = state.respondingTo != null

                LynkButton(
                    text = stringResource(Res.string.invite_preview_decline),
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onAction(InvitePreviewAction.OnDeclineClick)
                    },
                    style = LynkButtonStyle.SECONDARY,
                    enabled = !isResponding,
                    isLoading = state.respondingTo == RsvpStatus.DECLINED,
                    loadingText = stringResource(Res.string.invite_preview_declining),
                    modifier = Modifier.weight(1f)
                )

                LynkButton(
                    text = stringResource(Res.string.invite_preview_accept),
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onAction(InvitePreviewAction.OnAcceptClick)
                    },
                    style = LynkButtonStyle.PRIMARY,
                    enabled = !isResponding,
                    isLoading = state.respondingTo == RsvpStatus.ATTENDING,
                    loadingText = stringResource(Res.string.invite_preview_accepting),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    text: String,
    supportingText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LynkText(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            supportingText?.takeIf { it.isNotBlank() }?.let {
                LynkText(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun previewHangout(
    status: HangoutStatus,
    chosenSpot: SpotUi?
) = HangoutPreviewUi(
    id = "h1",
    hostId = "u1",
    name = "Sunday Jollof Run",
    description = "Meeting up for the good stuff before the match starts.",
    vibe = HangoutVibe.FOOD,
    status = status,
    scheduledAt = Instant.fromEpochSeconds(1_790_000_000L),
    maxAttendees = 8,
    participantCount = 5,
    chosenSpot = chosenSpot,
    attendees = List(4) { index ->
        HangoutUserUi(
            userId = "$index",
            username = "user$index",
            displayName = "User $index",
            initials = "U$index",
            profilePictureUrl = null
        )
    },
    createdAt = Instant.fromEpochSeconds(1_789_000_000L)
)

private val previewSpot = SpotUi(
    id = "s1",
    name = "Nok by Alara",
    description = null,
    photoUrls = emptyList(),
    category = SpotCategory.RESTAURANT,
    tags = emptyList(),
    priceLevel = PriceLevel.MODERATE,
    rating = 4.6,
    reviewCount = 210,
    isOpenNow = true,
    shortAddress = "Victoria Island, Lagos",
    latitude = 6.43,
    longitude = 3.42,
    websiteUrl = null,
    googleMapsUrl = null,
    isSaved = false
)

@Composable
private fun InvitePreviewSheetPreview(state: InvitePreviewState) {
    LynkTheme {
        InvitePreviewSheetContent(
            state = state,
            onAction = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(vertical = 20.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun InvitePreviewSheetScheduledPreview() = InvitePreviewSheetPreview(
    InvitePreviewState(
        hangoutPreview = previewHangout(HangoutStatus.SCHEDULED, previewSpot)
    )
)

@PreviewLightDark
@Composable
private fun InvitePreviewSheetVotingPreview() = InvitePreviewSheetPreview(
    InvitePreviewState(
        hangoutPreview = previewHangout(HangoutStatus.VOTING, chosenSpot = null)
    )
)

@PreviewLightDark
@Composable
private fun InvitePreviewSheetRespondingPreview() = InvitePreviewSheetPreview(
    InvitePreviewState(
        hangoutPreview = previewHangout(HangoutStatus.SCHEDULED, previewSpot),
        respondingTo = RsvpStatus.ATTENDING
    )
)

@PreviewLightDark
@Composable
private fun InvitePreviewSheetLoadingPreview() = InvitePreviewSheetPreview(
    InvitePreviewState(isLoading = true)
)
