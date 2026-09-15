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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.CheckCheck
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.notifications.presentation.invite_preview.InvitePreviewRoot
import com.eeseka.lynk.notifications.presentation.mappers.toUiText
import com.eeseka.lynk.notifications.presentation.notifications.components.NotificationListItem
import com.eeseka.lynk.notifications.presentation.notifications.components.NotificationsEmptyState
import com.eeseka.lynk.notifications.presentation.util.toNotificationTimeLabel
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
import com.eeseka.lynk.shared.presentation.components.LynkErrorState
import com.eeseka.lynk.shared.presentation.preview.previewNotifications
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.collections.immutable.persistentListOf
import lynk.feature.notifications.generated.resources.Res
import lynk.feature.notifications.generated.resources.back
import lynk.feature.notifications.generated.resources.mark_all_read
import lynk.feature.notifications.generated.resources.notifications
import lynk.feature.notifications.generated.resources.notifications_load_error_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationsRoot(
    previewHangoutId: String?,
    navigateBack: () -> Unit,
    navigateToHangout: (String) -> Unit,
    viewModel: NotificationsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // A notification tap can deep link straight into the invite preview.
    LaunchedEffect(previewHangoutId) {
        previewHangoutId?.let { viewModel.onAction(NotificationsAction.OnOpenInvitePreview(it)) }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NotificationsEvent.Error -> {
                snackbarHostState.showFlashMessage(
                    message = event.message.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }

            is NotificationsEvent.NavigateToHangout -> navigateToHangout(event.hangoutId)
        }
    }

    NotificationsScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        navigateBack = navigateBack
    )

    InvitePreviewRoot(
        visible = state.previewHangoutId != null,
        hangoutId = state.previewHangoutId,
        snackbarHostState = snackbarHostState,
        onDismiss = { viewModel.onAction(NotificationsAction.OnDismissInvitePreview) },
        onAccepted = { hangoutId ->
            viewModel.onAction(NotificationsAction.OnDismissInvitePreview)
            navigateToHangout(hangoutId)
        },
        onAlreadyAnswered = { hangoutId ->
            viewModel.onAction(NotificationsAction.OnDismissInvitePreview)
            navigateToHangout(hangoutId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsState,
    onAction: (NotificationsAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigateBack: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    val listState = rememberLazyListState()

    PaginationScrollListener(
        lazyListState = listState,
        itemCount = state.notifications.size,
        isPaginationLoading = state.isLoading,
        isEndReached = state.isEndReached,
        onNearBottom = { onAction(NotificationsAction.LoadNextPage) }
    )

    val hasUnread = remember(state.notifications) {
        state.notifications.any { !it.isRead }
    }

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            val backLabel = stringResource(Res.string.back)
            val markAllReadLabel = stringResource(Res.string.mark_all_read)

            LynkTopAppBar(
                title = stringResource(Res.string.notifications),
                navigationIcon = {
                    LynkIconButton(
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            navigateBack()
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
                iosLeadingItems = persistentListOf(
                    LynkIosBarButtonItem(
                        sfSymbol = "chevron.left",
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            navigateBack()
                        }
                    )
                ),
                iosTrailingItems = if (hasUnread) {
                    persistentListOf(
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
                    persistentListOf()
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
            val showLoadError = state.notifications.isEmpty() && state.loadError != null && !state.isLoading

            if (showLoadError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LynkErrorState(
                        title = stringResource(Res.string.notifications_load_error_title),
                        message = state.loadError.asString(),
                        onRetry = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(NotificationsAction.OnRetryClick)
                        }
                    )
                }
            } else {
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
                                },
                                modifier = Modifier.animateItem()
                            )
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
    }
}

@PreviewLightDark
@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun NotificationsScreenListPreview() = NotificationsScreenPreview(
    NotificationsState(
        notifications = previewNotifications,
        isEndReached = true
    )
)

@PreviewLightDark
@Composable
private fun NotificationsScreenEmptyPreview() = NotificationsScreenPreview(
    NotificationsState(isEndReached = true)
)

@Composable
private fun NotificationsScreenPreview(state: NotificationsState) {
    LynkTheme {
        NotificationsScreen(
            state = state,
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            navigateBack = {}
        )
    }
}
