package com.eeseka.lynk.notifications.presentation.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CheckCheck
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.notifications.presentation.invite_preview.InvitePreviewRoot
import com.eeseka.lynk.notifications.presentation.notifications.components.NotificationListItem
import com.eeseka.lynk.notifications.presentation.notifications.components.NotificationsEmptyState
import com.eeseka.lynk.notifications.presentation.util.toNotificationTimeLabel
import com.eeseka.lynk.notifications.presentation.util.toUiText
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosBarButtonItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import com.eeseka.lynk.shared.presentation.notification.model.NotificationUi
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import lynk.feature.notifications.generated.resources.Res
import lynk.feature.notifications.generated.resources.back
import lynk.feature.notifications.generated.resources.mark_all_read
import lynk.feature.notifications.generated.resources.notifications
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsState,
    events: Flow<NotificationsEvent>,
    onAction: (NotificationsAction) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHangout: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = rememberAppHaptic()
    val listState = rememberLazyListState()

    ObserveAsEvents(events) { event ->
        when (event) {
            is NotificationsEvent.Error -> {
                hapticFeedback(AppHaptic.Error)
                snackbarHostState.showFlashMessage(
                    message = event.message.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }

            is NotificationsEvent.NavigateToHangout -> onNavigateToHangout(event.hangoutId)
        }
    }

    PaginationScrollListener(
        lazyListState = listState,
        itemCount = state.notifications.size,
        isPaginationLoading = state.isLoading,
        isEndReached = state.isEndReached,
        onNearBottom = { onAction(NotificationsAction.LoadNextPage) }
    )

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            val backLabel = stringResource(Res.string.back)
            val markAllReadLabel = stringResource(Res.string.mark_all_read)
            val hasUnread = state.notifications.any { !it.isRead }

            LynkTopAppBar(
                title = stringResource(Res.string.notifications),
                navigationIcon = {
                    LynkIconButton(
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.ChevronLeft,
                            contentDescription = backLabel
                        )
                    }
                },
                actions = {
                    if (hasUnread) {
                        LynkIconButton(
                            enabled = !state.isMarkingAllRead,
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onAction(NotificationsAction.OnMarkAllReadClick)
                            }
                        ) {
                            Icon(
                                imageVector = Lucide.CheckCheck,
                                contentDescription = markAllReadLabel
                            )
                        }
                    }
                },
                iosLeadingItems = listOf(
                    LynkIosBarButtonItem(
                        sfSymbol = "chevron.left",
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onNavigateBack()
                        }
                    )
                ),
                iosTrailingItems = if (hasUnread) {
                    listOf(
                        LynkIosBarButtonItem(
                            sfSymbol = "checkmark.circle",
                            enabled = !state.isMarkingAllRead,
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onAction(NotificationsAction.OnMarkAllReadClick)
                            }
                        )
                    )
                } else {
                    emptyList()
                }
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val configuration = currentDeviceConfiguration()
            val listMaxWidth = if (configuration.isMobile) Dp.Unspecified else 640.dp

            val showEmptyList = state.notifications.isEmpty() && !state.isLoading && state.isEndReached

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = scaffoldPadding.calculateTopPadding() + 16.dp,
                    bottom = scaffoldPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.widthIn(max = listMaxWidth).fillMaxSize()
            ) {
                if (showEmptyList) {
                    item {
                        NotificationsEmptyState()
                    }
                } else {
                    items(state.notifications, key = { it.id }) { notification ->
                        Box(modifier = Modifier.animateItem()) {
                            NotificationListItem(
                                type = notification.type,
                                message = notification.toUiText().asString(),
                                timeLabel = notification.createdAt.toNotificationTimeLabel(),
                                isRead = notification.isRead,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    onAction(
                                        NotificationsAction.OnNotificationClick(
                                            notificationId = notification.id,
                                            hangoutId = notification.hangoutId,
                                            type = notification.type
                                        )
                                    )
                                }
                            )
                        }
                    }

                    if (state.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LynkProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    InvitePreviewRoot(
        visible = state.previewHangoutId != null,
        hangoutId = state.previewHangoutId,
        snackbarHostState = snackbarHostState,
        onDismiss = { onAction(NotificationsAction.OnDismissInvitePreview) },
        onAccepted = { hangoutId ->
            onAction(NotificationsAction.OnDismissInvitePreview)
            onNavigateToHangout(hangoutId)
        },
        onAlreadyAnswered = { hangoutId ->
            onAction(NotificationsAction.OnDismissInvitePreview)
            onNavigateToHangout(hangoutId)
        }
    )
}

@PreviewLightDark
@Composable
private fun NotificationsScreenPreview() {
    LynkTheme {
        NotificationsScreen(
            state = NotificationsState(
                notifications = listOf(
                    NotificationUi(
                        id = "1",
                        type = NotificationType.PARTICIPANT_INVITED,
                        hangoutId = "h1",
                        hangoutName = "Sunday Jollof Run",
                        actorDisplayName = "Tolu",
                        amountKobo = null,
                        isRead = false,
                        createdAt = Clock.System.now() - 3.hours
                    ),
                    NotificationUi(
                        id = "2",
                        type = NotificationType.PAYOUT_SUCCEEDED,
                        hangoutId = "h2",
                        hangoutName = "Game Night",
                        actorDisplayName = null,
                        amountKobo = 2_400_000L,
                        isRead = true,
                        createdAt = Clock.System.now() - 50.hours
                    )
                ),
                isEndReached = true
            ),
            events = flowOf(),
            onAction = {},
            onNavigateBack = {},
            onNavigateToHangout = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun NotificationsScreenEmptyPreview() {
    LynkTheme {
        NotificationsScreen(
            state = NotificationsState(isEndReached = true),
            events = flowOf(),
            onAction = {},
            onNavigateBack = {},
            onNavigateToHangout = {}
        )
    }
}