package com.eeseka.lynk.hangouts.presentation.hangout_detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CalendarX2
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SquarePen
import com.composables.icons.lucide.UserRoundPlus
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.ChosenSpotSection
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.ConnectionBanner
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.DetailEmptyState
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.DetailErrorState
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.DetailSection
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.HangoutHero
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.InviteParticipantSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.ParticipantsSection
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.ParticipantsSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.ProposeSpotSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.VotingSection
import com.eeseka.lynk.hangouts.presentation.util.toHangoutDisplayDate
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosBarButtonItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosDropDownMenuItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_ATTENDEES
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.coroutines.flow.Flow
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_about
import lynk.feature.hangouts.generated.resources.detail_cancel
import lynk.feature.hangouts.generated.resources.detail_cancel_confirm_action
import lynk.feature.hangouts.generated.resources.detail_cancel_confirm_message
import lynk.feature.hangouts.generated.resources.detail_cancel_confirm_title
import lynk.feature.hangouts.generated.resources.detail_complete
import lynk.feature.hangouts.generated.resources.detail_complete_confirm_action
import lynk.feature.hangouts.generated.resources.detail_complete_confirm_message
import lynk.feature.hangouts.generated.resources.detail_complete_confirm_title
import lynk.feature.hangouts.generated.resources.detail_completing
import lynk.feature.hangouts.generated.resources.detail_dialog_dismiss
import lynk.feature.hangouts.generated.resources.detail_invite
import lynk.feature.hangouts.generated.resources.detail_leave
import lynk.feature.hangouts.generated.resources.detail_leave_confirm_action
import lynk.feature.hangouts.generated.resources.detail_leave_confirm_message
import lynk.feature.hangouts.generated.resources.detail_leave_confirm_title
import lynk.feature.hangouts.generated.resources.detail_more_actions
import lynk.feature.hangouts.generated.resources.detail_update
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

private sealed interface DetailBodyState {
    data object Loading : DetailBodyState
    data class Content(val hangout: HangoutUi) : DetailBodyState
    data class Error(val message: UiText) : DetailBodyState
    data object Empty : DetailBodyState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangoutDetailScreen(
    state: HangoutDetailState,
    events: Flow<HangoutDetailEvent>,
    onAction: (HangoutDetailAction) -> Unit,
    isDetailPaneFullScreen: Boolean,
    onBackClick: () -> Unit,
    onLeave: () -> Unit,
    onEditClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showParticipantsSheet by remember { mutableStateOf(false) }
    var showChosenSpotSheet by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    ObserveAsEvents(events) { event ->
        when (event) {
            is HangoutDetailEvent.ShowMessage -> {
                snackbarHostState.showFlashMessage(
                    message = event.message.asStringAsync(),
                    type = event.type
                )
            }

            HangoutDetailEvent.NavigateBack -> onLeave()
        }
    }

    val hangout = state.hangout
    val isHost = hangout != null && hangout.hostId == state.currentUserId
    val isUpcoming = hangout != null &&
            (hangout.status == HangoutStatus.VOTING || hangout.status == HangoutStatus.SCHEDULED)
    val showInvite = isHost && isUpcoming
    val inviteEnabled = run {
        if (hangout == null) return@run true
        val max = hangout.maxAttendees ?: MAX_ATTENDEES
        val activeCount = hangout.participants.count {
            it.rsvpStatus == RsvpStatus.ATTENDING || it.rsvpStatus == RsvpStatus.PENDING
        }
        activeCount < max
    }
    val canCancel = hangout != null && isHost &&
            hangout.status != HangoutStatus.COMPLETED &&
            hangout.status != HangoutStatus.CANCELLED
    val canLeave = hangout != null && !isHost && isUpcoming
    val canEdit = isHost && isUpcoming
    val canComplete = hangout != null && isHost && hangout.status == HangoutStatus.ONGOING

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            val inviteLabel = stringResource(Res.string.detail_invite)
            val moreLabel = stringResource(Res.string.detail_more_actions)
            val cancelLabel = stringResource(Res.string.detail_cancel)
            val leaveLabel = stringResource(Res.string.detail_leave)
            val editLabel = stringResource(Res.string.detail_update)

            LynkTopAppBar(
                navigationIcon = {
                    if (isDetailPaneFullScreen) {
                        LynkIconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Lucide.ChevronLeft,
                                contentDescription = null
                            )
                        }
                    }
                },
                iosLeadingItems = if (isDetailPaneFullScreen) {
                    listOf(LynkIosBarButtonItem(sfSymbol = "chevron.left", onClick = onBackClick))
                } else emptyList(),
                actions = {
                    if (showInvite) {
                        LynkIconButton(
                            onClick = { onAction(HangoutDetailAction.OnInviteClick) },
                            enabled = inviteEnabled
                        ) {
                            Icon(
                                imageVector = Lucide.UserRoundPlus,
                                contentDescription = inviteLabel
                            )
                        }
                    }
                    val overflowItems = buildList {
                        if (canEdit) {
                            add(
                                LynkDropDownItem(
                                    title = editLabel,
                                    icon = Lucide.SquarePen,
                                    onClick = onEditClick
                                )
                            )
                        }
                        if (canCancel) {
                            add(
                                LynkDropDownItem(
                                    title = cancelLabel,
                                    icon = Lucide.CalendarX2,
                                    isDestructive = true,
                                    isDisabled = state.isCancelling,
                                    onClick = { showCancelDialog = true }
                                )
                            )
                        }
                        if (canLeave) {
                            add(
                                LynkDropDownItem(
                                    title = leaveLabel,
                                    icon = Lucide.LogOut,
                                    isDestructive = true,
                                    isDisabled = state.isLeaving,
                                    onClick = { showLeaveDialog = true }
                                )
                            )
                        }
                    }
                    if (overflowItems.isNotEmpty()) {
                        LynkDropDownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            items = overflowItems,
                            anchor = {
                                LynkIconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(
                                        imageVector = Lucide.EllipsisVertical,
                                        contentDescription = moreLabel
                                    )
                                }
                            }
                        )
                    }
                },
                iosTrailingItems = buildList {
                    if (showInvite) {
                        add(
                            LynkIosBarButtonItem(
                                sfSymbol = "person.badge.plus",
                                enabled = inviteEnabled,
                                onClick = { onAction(HangoutDetailAction.OnInviteClick) }
                            )
                        )
                    }
                    val iosOverflowItems = buildList {
                        if (canEdit) {
                            add(
                                LynkIosDropDownMenuItem(
                                    title = editLabel,
                                    sfSymbol = "square.and.pencil",
                                    onClick = onEditClick
                                )
                            )
                        }
                        if (canCancel) {
                            add(
                                LynkIosDropDownMenuItem(
                                    title = cancelLabel,
                                    sfSymbol = "calendar.badge.minus",
                                    isDestructive = true,
                                    isDisabled = state.isCancelling,
                                    onClick = { showCancelDialog = true }
                                )
                            )
                        }
                        if (canLeave) {
                            add(
                                LynkIosDropDownMenuItem(
                                    title = leaveLabel,
                                    sfSymbol = "rectangle.portrait.and.arrow.right",
                                    isDestructive = true,
                                    isDisabled = state.isLeaving,
                                    onClick = { showLeaveDialog = true }
                                )
                            )
                        }
                    }
                    if (iosOverflowItems.isNotEmpty()) {
                        add(
                            LynkIosBarButtonItem(
                                sfSymbol = "ellipsis.circle",
                                menuItems = iosOverflowItems
                            )
                        )
                    }
                }.reversed()
            )
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
                                isHost = target.hangout.hostId == state.currentUserId,
                                currentUserId = state.currentUserId,
                                presentUserIds = state.presentUserIds,
                                candidates = state.candidates,
                                votes = state.votes,
                                removingSpotIds = state.removingSpotIds,
                                tiedSpotIds = state.tiedSpotIds,
                                isClosingVoting = state.isClosingVoting,
                                isCompleting = state.isCompleting,
                                onSeeAllParticipantsClick = { showParticipantsSheet = true },
                                onChosenSpotClick = { showChosenSpotSheet = true },
                                onCompleteClick = { showCompleteDialog = true },
                                onCastVote = { onAction(HangoutDetailAction.OnCastVote(it)) },
                                onRemoveSpot = { onAction(HangoutDetailAction.OnRemoveSpot(it)) },
                                onProposeClick = { onAction(HangoutDetailAction.OnProposeSpotClick) },
                                onCloseVoting = { onAction(HangoutDetailAction.OnCloseVotingClick) },
                                onBreakTie = { onAction(HangoutDetailAction.OnBreakTie(it)) },
                                contentPadding = scaffoldPadding
                            )
                        }

                        is DetailBodyState.Error -> {
                            DetailErrorState(
                                message = target.message.asString(),
                                onRetry = { onAction(HangoutDetailAction.OnRetryClick) }
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
            isHost = state.hangout.hostId == state.currentUserId,
            onDismiss = { showParticipantsSheet = false },
            onWithdraw = { onAction(HangoutDetailAction.OnWithdrawParticipantInvite(it)) },
            withdrawingUserIds = state.withdrawingUserIds,
            presentUserIds = state.presentUserIds
        )
    }

    val chosenSpot = state.hangout?.chosenSpot
    if (showChosenSpotSheet && chosenSpot != null) {
        SpotDetailSheet(
            spot = chosenSpot,
            userLat = state.center?.latitude ?: state.myLocation?.latitude,
            userLng = state.center?.longitude ?: state.myLocation?.longitude,
            onDismissRequest = { showChosenSpotSheet = false },
            onToggleSave = { spotId, isCurrentlySaved ->
                onAction(HangoutDetailAction.OnToggleSaveSpot(spotId, isCurrentlySaved))
            }
        )
    }

    if (state.isInviteSheetOpen) {
        val resultId = state.inviteResult?.userId
        val alreadyInvited = resultId != null && state.hangout?.participants?.any {
            it.userId == resultId && it.rsvpStatus != RsvpStatus.DECLINED
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

    if (state.isProposeSpotSheetOpen) {
        ProposeSpotSheet(
            state = state,
            onTabSelected = { onAction(HangoutDetailAction.OnTabSelected(it)) },
            onPropose = { onAction(HangoutDetailAction.OnProposeSpot(it)) },
            onLoadNextSpotPage = { onAction(HangoutDetailAction.LoadNextSpotPage) },
            onLoadNextFavoritePage = { onAction(HangoutDetailAction.LoadNextFavoriteSpotPage) },
            onDismiss = { onAction(HangoutDetailAction.OnDismissProposeSpotSheet) }
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
        onShareLocation = { lat, lng -> onAction(HangoutDetailAction.OnShareLocation(lat, lng)) }
    )
}

@Composable
private fun LocationShareEffect(
    enabled: Boolean,
    isConnected: Boolean,
    onShareLocation: (Double, Double) -> Unit
) {
    val permissionController = rememberPermissionController()
    val locationController = rememberLocationController()
    var permissionState by remember { mutableStateOf(PermissionState.NOT_DETERMINED) }

    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect

        permissionState = permissionController.getPermissionState(Permission.LOCATION)
        if (permissionState == PermissionState.NOT_DETERMINED || permissionState == PermissionState.DENIED) {
            permissionState = permissionController.requestPermission(Permission.LOCATION)
        }
    }

    LaunchedEffect(permissionState, enabled, isConnected) {
        if (!enabled || !isConnected) return@LaunchedEffect
        if (permissionState != PermissionState.GRANTED) return@LaunchedEffect

        val coordinate = locationController.getCurrentLocation() ?: return@LaunchedEffect
        onShareLocation(coordinate.latitude, coordinate.longitude)
    }
}

@Composable
private fun HangoutDetailContent(
    hangout: HangoutUi,
    isHost: Boolean,
    currentUserId: String?,
    presentUserIds: Set<String>,
    candidates: List<SpotUi>,
    votes: Map<String, String>,
    removingSpotIds: Set<String>,
    tiedSpotIds: List<String>,
    isClosingVoting: Boolean,
    isCompleting: Boolean,
    onSeeAllParticipantsClick: () -> Unit,
    onChosenSpotClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onCastVote: (String) -> Unit,
    onRemoveSpot: (String) -> Unit,
    onProposeClick: () -> Unit,
    onCloseVoting: () -> Unit,
    onBreakTie: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp
            )
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HangoutHero(
            name = hangout.name,
            vibe = hangout.vibe,
            status = hangout.status,
            scheduledDate = hangout.scheduledAt.toHangoutDisplayDate(),
            isHost = isHost
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
                onSpotClick = onChosenSpotClick
            )
        }

        if (isHost && hangout.status == HangoutStatus.ONGOING) {
            LynkButton(
                text = stringResource(Res.string.detail_complete),
                onClick = onCompleteClick,
                style = LynkButtonStyle.PRIMARY,
                isLoading = isCompleting,
                loadingText = stringResource(Res.string.detail_completing),
                modifier = Modifier.height(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private val previewSpot = SpotUi(
    id = "s1",
    name = "The Rooftop Lounge",
    description = "Skyline views",
    photoUrls = emptyList(),
    category = SpotCategory.RESTAURANT,
    tags = emptyList(),
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

private val previewCandidates = listOf(
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

private fun previewHangout(
    status: HangoutStatus = HangoutStatus.SCHEDULED,
    withSpot: Boolean = true
) = HangoutUi(
    id = "1",
    hostId = "host-1",
    name = "Rooftop Party in Lekki",
    description = "Bring your best vibes. We'll sort drinks and music, you just show up.",
    vibe = HangoutVibe.PARTY,
    status = status,
    scheduledAt = Instant.fromEpochSeconds(1_800_000_000L),
    maxAttendees = 10,
    participantCount = 6,
    chosenSpot = if (withSpot) previewSpot else null,
    totalCost = null,
    costPerPerson = null,
    participants = List(6) { index ->
        HangoutParticipantUi(
            userId = "$index",
            username = "user$index",
            displayName = "Guest $index",
            initials = "G$index",
            profilePictureUrl = null,
            rsvpStatus = RsvpStatus.ATTENDING,
            hasPaid = false
        )
    },
    createdAt = Instant.fromEpochSeconds(1_790_000_000L)
)

@Composable
private fun HangoutDetailContentPreview(
    hangout: HangoutUi = previewHangout(),
    isHost: Boolean = true,
    candidates: List<SpotUi> = emptyList(),
    votes: Map<String, String> = emptyMap(),
    tiedSpotIds: List<String> = emptyList(),
    isClosingVoting: Boolean = false,
    isCompleting: Boolean = false
) {
    LynkTheme {
        HangoutDetailContent(
            hangout = hangout,
            isHost = isHost,
            currentUserId = if (isHost) hangout.hostId else "someone-else",
            presentUserIds = setOf("0", "2"),
            candidates = candidates,
            votes = votes,
            removingSpotIds = emptySet(),
            tiedSpotIds = tiedSpotIds,
            isClosingVoting = isClosingVoting,
            isCompleting = isCompleting,
            onSeeAllParticipantsClick = {},
            onChosenSpotClick = {},
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
    votes = mapOf("0" to "s1", "1" to "s1", "2" to "s2", "host-1" to "s3")
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
    votes = mapOf("0" to "s1", "1" to "s2"),
    tiedSpotIds = listOf("s1", "s2")
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