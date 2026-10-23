package com.eeseka.lynk.assistant.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowDown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import com.eeseka.lynk.assistant.presentation.components.AssistantAnswerRow
import com.eeseka.lynk.assistant.presentation.components.AssistantClosedNotice
import com.eeseka.lynk.assistant.presentation.components.AssistantAnswer
import com.eeseka.lynk.assistant.presentation.components.AssistantEmptyState
import com.eeseka.lynk.assistant.presentation.components.AssistantInputBar
import com.eeseka.lynk.assistant.presentation.components.AssistantStatusLine
import com.eeseka.lynk.assistant.presentation.components.UserMessageBubble
import com.eeseka.lynk.assistant.presentation.mappers.getTitle
import com.eeseka.lynk.assistant.presentation.preview.previewAssistantMessages
import com.eeseka.lynk.assistant.presentation.model.SpotSuggestState
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.layouts.safeArea
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosBarButtonItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageRole
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageStatus
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_VOTING_CANDIDATES
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.components.LynkErrorState
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.media.rememberTextSharer
import com.eeseka.lynk.shared.presentation.preview.previewSpots
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.toUpcomingTimeLabel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import lynk.feature.assistant.generated.resources.Res
import lynk.feature.assistant.generated.resources.jump_to_latest
import lynk.feature.assistant.generated.resources.assistant_title
import lynk.feature.assistant.generated.resources.chat_cleared
import lynk.feature.assistant.generated.resources.chat_expired
import lynk.feature.assistant.generated.resources.clear_chat
import lynk.feature.assistant.generated.resources.clear_chat_confirm_message
import lynk.feature.assistant.generated.resources.clear_chat_confirm_title
import lynk.feature.assistant.generated.resources.close
import lynk.feature.assistant.generated.resources.dialog_dismiss
import lynk.feature.assistant.generated.resources.empty_message_discover
import lynk.feature.assistant.generated.resources.empty_message_discover_area
import lynk.feature.assistant.generated.resources.empty_message_hangout
import lynk.feature.assistant.generated.resources.empty_message_hangout_read_only
import lynk.feature.assistant.generated.resources.empty_title_discover
import lynk.feature.assistant.generated.resources.empty_title_hangout
import lynk.feature.assistant.generated.resources.history_load_error_title
import lynk.feature.assistant.generated.resources.messages_left
import lynk.feature.assistant.generated.resources.out_of_messages_message
import lynk.feature.assistant.generated.resources.out_of_messages_message_tomorrow
import lynk.feature.assistant.generated.resources.out_of_messages_title
import lynk.feature.assistant.generated.resources.read_only_ended
import lynk.feature.assistant.generated.resources.read_only_not_attending
import lynk.feature.assistant.generated.resources.read_only_voting_closed
import lynk.feature.assistant.generated.resources.spot_suggested
import lynk.feature.assistant.generated.resources.starter_discover_birthday
import lynk.feature.assistant.generated.resources.starter_discover_cheap
import lynk.feature.assistant.generated.resources.starter_discover_drinks
import lynk.feature.assistant.generated.resources.starter_discover_music
import lynk.feature.assistant.generated.resources.starter_hangout_budget
import lynk.feature.assistant.generated.resources.starter_hangout_late
import lynk.feature.assistant.generated.resources.starter_hangout_reach
import lynk.feature.assistant.generated.resources.starter_hangout_vibe
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val MESSAGES_LEFT_VISIBLE_AT = 5

private sealed interface AssistantBodyState {
    data object Loading : AssistantBodyState
    data class Error(val message: UiText) : AssistantBodyState
    data object Empty : AssistantBodyState
    data object Chat : AssistantBodyState
}

private enum class AssistantBottomBar {
    NOT_ATTENDING,
    ENDED,
    VOTING_CLOSED,
    OUT_OF_MESSAGES,
    INPUT
}

@Composable
fun AssistantRoot(
    hangoutId: String?,
    latitude: Double?,
    longitude: Double?,
    areaName: String?,
    navigateBack: () -> Unit,
    viewModel: AssistantViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = rememberAppHaptic()

    LaunchedEffect(hangoutId, latitude, longitude) {
        if (hangoutId != null) {
            viewModel.onAction(AssistantAction.OnOpenHangoutChat(hangoutId))
        } else if (latitude != null && longitude != null) {
            viewModel.onAction(AssistantAction.OnOpenDiscoverChat(latitude, longitude, areaName))
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is AssistantEvent.Error -> snackbarHostState.showFlashMessage(
                message = event.message.asStringAsync(),
                type = LynkFlashType.Error
            )

            AssistantEvent.SpotSuggested -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.spot_suggested),
                type = LynkFlashType.Success
            )

            AssistantEvent.ChatCleared -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.chat_cleared),
                type = LynkFlashType.Info
            )

            AssistantEvent.ChatExpired -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.chat_expired),
                type = LynkFlashType.Info
            )

            AssistantEvent.AnswerFailed -> hapticFeedback(AppHaptic.Error)
        }
    }

    AssistantScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        navigateBack = navigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    state: AssistantState,
    onAction: (AssistantAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigateBack: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val permissionController = rememberPermissionController()
    val locationController = rememberLocationController()

    var showClearDialog by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LocationCoordinates?>(null) }

    val isSpotSheetShowing = state.selectedSpot != null
    LaunchedEffect(isSpotSheetShowing) {
        if (!isSpotSheetShowing) return@LaunchedEffect
        if (permissionController.getPermissionState(Permission.LOCATION) != PermissionState.GRANTED) return@LaunchedEffect

        locationController.observeCurrentLocation().collect { locationResult ->
            locationResult.onSuccess { coordinate -> userLocation = coordinate }
        }
    }

    val isHangoutChat = state.hangoutId != null
    val newestMessage = state.messages.firstOrNull()

    val isAnswerInProgress = state.isAnswering || newestMessage?.status == AssistantMessageStatus.PENDING
    val isVoting = state.hangoutStatus == HangoutStatus.VOTING

    val isChatOpen = !isHangoutChat || (state.isAttending == true && isVoting) // !isHangoutChat is essentially discover chat
    val isOutOfMessages = state.messagesLeft == 0
    val canAsk = isChatOpen && !isOutOfMessages && !isAnswerInProgress
    val isWaitingForHistory = isHangoutChat && state.messages.isEmpty() && (state.isLoading || state.loadError != null)

    val canSend = canAsk && !isWaitingForHistory && state.messageTextState.text.isNotBlank()
    val canSuggest = isHangoutChat && state.isAttending == true && isVoting

    val showClearChat = isHangoutChat && state.messages.isNotEmpty()
    val isClearChatEnabled = !isAnswerInProgress && !state.isClearingChat

    val onCopyClick = { content: String ->
        hapticFeedback(AppHaptic.ImpactLight)
        clipboardManager.setText(AnnotatedString(content))
    }

    val textSharer = rememberTextSharer()
    val onShareClick = { content: String ->
        hapticFeedback(AppHaptic.ImpactLight)
        textSharer.share(content)
    }

    PaginationScrollListener(
        lazyListState = listState,
        itemCount = state.messages.size,
        isPaginationLoading = state.isLoading,
        isEndReached = state.isEndReached,
        onNearBottom = { onAction(AssistantAction.LoadNextPage) },
        resetKey = state.messagesResetEpoch
    )

    val newestMessageId = newestMessage?.id
    remember(newestMessageId) {
        if (newestMessageId != null) listState.requestScrollToItem(0)
    }

    val configuration = currentDeviceConfiguration()
    val contentMaxWidth = if (configuration.isMobile) Dp.Unspecified else 640.dp

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            val closeLabel = stringResource(Res.string.close)
            val clearChatLabel = stringResource(Res.string.clear_chat)

            LynkTopAppBar(
                title = stringResource(Res.string.assistant_title),
                navigationIcon = {
                    LynkIconButton(
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            navigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.X,
                            contentDescription = closeLabel
                        )
                    }
                },
                actions = {
                    if (showClearChat) {
                        LynkIconButton(
                            enabled = isClearChatEnabled,
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                showClearDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Lucide.Trash2,
                                contentDescription = clearChatLabel
                            )
                        }
                    }
                },
                iosLeadingItems = persistentListOf(
                    LynkIosBarButtonItem(
                        sfSymbol = "xmark",
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            navigateBack()
                        }
                    )
                ),
                iosTrailingItems = if (showClearChat) {
                    persistentListOf(
                        LynkIosBarButtonItem(
                            sfSymbol = "trash",
                            enabled = isClearChatEnabled,
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                showClearDialog = true
                            }
                        )
                    )
                } else {
                    persistentListOf()
                }
            )
        },
        bottomBar = {
            val bottomBar = when {
                isHangoutChat && state.isAttending == false -> AssistantBottomBar.NOT_ATTENDING
                state.hangoutStatus == HangoutStatus.COMPLETED || state.hangoutStatus == HangoutStatus.CANCELLED -> AssistantBottomBar.ENDED
                isHangoutChat && state.hangoutStatus != null && !isVoting -> AssistantBottomBar.VOTING_CLOSED
                isOutOfMessages -> AssistantBottomBar.OUT_OF_MESSAGES
                else -> AssistantBottomBar.INPUT
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeArea.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = bottomBar,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "assistant_bottom_bar",
                    modifier = Modifier
                        .widthIn(max = contentMaxWidth)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                ) { target ->
                    when (target) {
                        AssistantBottomBar.NOT_ATTENDING -> {
                            AssistantClosedNotice(
                                title = null,
                                message = stringResource(Res.string.read_only_not_attending),
                                isOutOfMessages = false
                            )
                        }

                        AssistantBottomBar.ENDED -> {
                            AssistantClosedNotice(
                                title = null,
                                message = stringResource(Res.string.read_only_ended),
                                isOutOfMessages = false
                            )
                        }

                        AssistantBottomBar.VOTING_CLOSED -> {
                            AssistantClosedNotice(
                                title = null,
                                message = stringResource(Res.string.read_only_voting_closed),
                                isOutOfMessages = false
                            )
                        }

                        AssistantBottomBar.OUT_OF_MESSAGES -> {
                            AssistantClosedNotice(
                                title = stringResource(Res.string.out_of_messages_title),
                                message = state.usageResetsAt?.let { resetsAt ->
                                    stringResource(
                                        Res.string.out_of_messages_message,
                                        resetsAt.toUpcomingTimeLabel(TimeZone.currentSystemDefault())
                                    )
                                } ?: stringResource(Res.string.out_of_messages_message_tomorrow),
                                isOutOfMessages = true
                            )
                        }

                        AssistantBottomBar.INPUT -> {
                            val messagesLeft = state.messagesLeft
                            val messageLimit = state.messageLimit

                            AssistantInputBar(
                                textState = state.messageTextState,
                                canSend = canSend,
                                messagesLeftLabel = if (messagesLeft != null && messageLimit != null && messagesLeft <= MESSAGES_LEFT_VISIBLE_AT) {
                                    stringResource(Res.string.messages_left, messagesLeft, messageLimit)
                                } else null,
                                onSendClick = {
                                    hapticFeedback(AppHaptic.ImpactMedium)
                                    onAction(AssistantAction.OnSendClick)
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier.fillMaxSize().clearFocusOnTap(),
            contentAlignment = Alignment.TopCenter
        ) {
            val loadError = state.loadError
            val bodyState = when {
                loadError != null && state.messages.isEmpty() -> AssistantBodyState.Error(loadError)
                isWaitingForHistory -> AssistantBodyState.Loading
                state.messages.isEmpty() -> AssistantBodyState.Empty
                else -> AssistantBodyState.Chat
            }

            AnimatedContent(
                targetState = bodyState,
                contentKey = { it::class },
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "assistant_body",
                modifier = Modifier.widthIn(max = contentMaxWidth).fillMaxSize()
            ) { target ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (target) {
                        is AssistantBodyState.Error -> {
                            LynkErrorState(
                                title = stringResource(Res.string.history_load_error_title),
                                message = target.message.asString(),
                                onRetry = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    onAction(AssistantAction.OnRetryClick)
                                }
                            )
                        }

                        AssistantBodyState.Loading -> {
                            LynkProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        AssistantBodyState.Empty -> {
                            val starterPrompts = if (isHangoutChat) {
                                persistentListOf(
                                    stringResource(Res.string.starter_hangout_vibe),
                                    stringResource(Res.string.starter_hangout_reach),
                                    stringResource(Res.string.starter_hangout_budget),
                                    stringResource(Res.string.starter_hangout_late)
                                )
                            } else {
                                persistentListOf(
                                    stringResource(Res.string.starter_discover_drinks),
                                    stringResource(Res.string.starter_discover_birthday),
                                    stringResource(Res.string.starter_discover_cheap),
                                    stringResource(Res.string.starter_discover_music)
                                )
                            }
                            val areaName = state.areaName
                            val hangoutName = state.hangoutName

                            AssistantEmptyState(
                                title = when {
                                    !isHangoutChat -> stringResource(Res.string.empty_title_discover)
                                    hangoutName != null -> stringResource(Res.string.empty_title_hangout, hangoutName)
                                    else -> stringResource(Res.string.assistant_title)
                                },
                                message = when {
                                    isHangoutChat && !isChatOpen -> stringResource(Res.string.empty_message_hangout_read_only)
                                    isHangoutChat -> stringResource(Res.string.empty_message_hangout)
                                    areaName != null -> stringResource(Res.string.empty_message_discover_area, areaName)
                                    else -> stringResource(Res.string.empty_message_discover)
                                },
                                starterPrompts = if (canAsk) starterPrompts else persistentListOf(),
                                onStarterPromptClick = { prompt ->
                                    hapticFeedback(AppHaptic.ImpactMedium)
                                    onAction(AssistantAction.OnStarterPromptClick(prompt))
                                },
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(
                                        top = scaffoldPadding.calculateTopPadding() + 24.dp,
                                        bottom = scaffoldPadding.calculateBottomPadding() + 24.dp
                                    )
                            )
                        }

                        AssistantBodyState.Chat -> {
                            LazyColumn(
                                state = listState,
                                reverseLayout = true,
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = scaffoldPadding.calculateTopPadding() + 16.dp,
                                    bottom = scaffoldPadding.calculateBottomPadding() + 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (state.isAnswering && newestMessage?.role == AssistantMessageRole.USER) {
                                    item(key = "answer_status") {
                                        AssistantAnswerRow {
                                            AssistantStatusLine(title = state.answerStatus.getTitle())
                                        }
                                    }
                                }

                                items(state.messages, key = { it.id }) { message ->
                                    when (message.role) {
                                        AssistantMessageRole.USER -> {
                                            UserMessageBubble(
                                                text = message.content,
                                                modifier = Modifier.animateItem(placementSpec = null)
                                            )
                                        }

                                        AssistantMessageRole.ASSISTANT -> {
                                            AssistantAnswer(
                                                message = message,
                                                isStreaming = state.isAnswering && message.id == newestMessage?.id,
                                                answerStatus = state.answerStatus,
                                                canRetry = canAsk && !isWaitingForHistory && message.id == newestMessage?.id,
                                                loadingSpotId = state.loadingSpotId,
                                                suggestStateOf = { spotId ->
                                                    if (!canSuggest) null
                                                    else when {
                                                        spotId in state.suggestingSpotIds -> SpotSuggestState.SUGGESTING
                                                        spotId in state.ballotSpotIds -> SpotSuggestState.ON_BALLOT
                                                        state.ballotSpotIds.size >= MAX_VOTING_CANDIDATES -> SpotSuggestState.BALLOT_FULL
                                                        else -> SpotSuggestState.SUGGEST
                                                    }
                                                },
                                                onAction = onAction,
                                                onCopyClick = { onCopyClick(message.content) },
                                                onShareClick = { onShareClick(message.content) }
                                            )
                                        }
                                    }
                                }

                                if (state.isLoading) {
                                    item(key = "history_loading") {
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

                            val isAwayFromLatest by remember { derivedStateOf { listState.canScrollBackward } }
                            JumpToLatestButton(
                                visible = isAwayFromLatest,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    coroutineScope.launch { listState.animateScrollToItem(0) }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = scaffoldPadding.calculateBottomPadding() + 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    state.selectedSpot?.let { spot ->
        SpotDetailSheet(
            spot = spot,
            userLat = userLocation?.latitude,
            userLng = userLocation?.longitude,
            onToggleSave = { spotId, isCurrentlySaved ->
                onAction(AssistantAction.OnToggleSaveSpot(spotId, isCurrentlySaved))
            },
            onDismissRequest = { onAction(AssistantAction.OnDismissSpotSheet) }
        )
    }

    if (showClearDialog) {
        LynkDialog(
            onDismissRequest = { showClearDialog = false },
            title = stringResource(Res.string.clear_chat_confirm_title),
            message = stringResource(Res.string.clear_chat_confirm_message),
            confirmText = stringResource(Res.string.clear_chat),
            dismissText = stringResource(Res.string.dialog_dismiss),
            isDestructive = true,
            onConfirm = { onAction(AssistantAction.OnClearChatConfirmed) }
        )
    }
}

@Composable
private fun JumpToLatestButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        LynkTonalIconButton(onClick = onClick) {
            Icon(
                imageVector = Lucide.ArrowDown,
                contentDescription = stringResource(Res.string.jump_to_latest)
            )
        }
    }
}

@PreviewLightDark
@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun AssistantScreenChatPreview() = AssistantScreenPreview(
    AssistantState(
        hangoutId = "h1",
        hangoutName = "Friday Night Out",
        hangoutStatus = HangoutStatus.VOTING,
        isAttending = true,
        ballotSpotIds = persistentSetOf(previewSpots[1].id),
        messages = previewAssistantMessages,
        isEndReached = true,
        messagesLeft = 3,
        messageLimit = 20
    )
)

@PreviewLightDark
@Composable
private fun AssistantScreenEmptyPreview() = AssistantScreenPreview(
    AssistantState(
        searchCenter = LocationCoordinates(6.44, 3.42),
        areaName = "Lekki",
        isEndReached = true,
        messagesLeft = 20,
        messageLimit = 20
    )
)

@PreviewLightDark
@Composable
private fun AssistantScreenReadOnlyPreview() = AssistantScreenPreview(
    AssistantState(
        hangoutId = "h1",
        hangoutName = "Friday Night Out",
        hangoutStatus = HangoutStatus.COMPLETED,
        isAttending = true,
        messages = previewAssistantMessages,
        isEndReached = true
    )
)

@Composable
private fun AssistantScreenPreview(state: AssistantState) {
    LynkTheme {
        AssistantScreen(
            state = state,
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            navigateBack = {}
        )
    }
}
