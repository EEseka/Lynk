package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRoundSearch
import com.composables.icons.lucide.UserX
import com.eeseka.lynk.shared.presentation.hangout.components.ParticipantAvatar
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import kotlinx.coroutines.delay
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.invite_action
import lynk.feature.hangouts.generated.resources.invite_host
import lynk.feature.hangouts.generated.resources.invite_idle_message
import lynk.feature.hangouts.generated.resources.invite_idle_title
import lynk.feature.hangouts.generated.resources.invite_invited
import lynk.feature.hangouts.generated.resources.invite_not_found_message
import lynk.feature.hangouts.generated.resources.invite_not_found_title
import lynk.feature.hangouts.generated.resources.invite_search_hint
import lynk.feature.hangouts.generated.resources.invite_sheet_title
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

private sealed interface InviteBodyState {
    data object Searching : InviteBodyState
    data class Match(
        val user: HangoutUserUi,
        val isInviting: Boolean,
        val alreadyInvited: Boolean,
        val isHost: Boolean
    ) : InviteBodyState

    data object NotFound : InviteBodyState
    data object Idle : InviteBodyState
}

private enum class InviteActionState { Host, Inviting, Invited, Invite }

@Composable
fun InviteParticipantSheet(
    queryState: TextFieldState,
    result: HangoutUserUi?,
    isSearching: Boolean,
    notFound: Boolean,
    isInviting: Boolean,
    alreadyInvited: Boolean,
    isResultHost: Boolean,
    onInvite: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        skipBottomSheetPartiallyExpanded = false
    ) {
        InviteParticipantSheetContent(
            queryState = queryState,
            result = result,
            isSearching = isSearching,
            notFound = notFound,
            isInviting = isInviting,
            alreadyInvited = alreadyInvited,
            isResultHost = isResultHost,
            onInvite = onInvite,
            modifier = modifier
        )
    }
}

@Composable
private fun InviteParticipantSheetContent(
    queryState: TextFieldState,
    result: HangoutUserUi?,
    isSearching: Boolean,
    notFound: Boolean,
    isInviting: Boolean,
    alreadyInvited: Boolean,
    isResultHost: Boolean,
    onInvite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(100.milliseconds)
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearFocusOnTap()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LynkText(
            text = stringResource(Res.string.invite_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        LynkSearchField(
            state = queryState,
            placeholder = stringResource(Res.string.invite_search_hint),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
        )

        val bodyState = when {
            isSearching -> InviteBodyState.Searching
            result != null -> InviteBodyState.Match(
                user = result,
                isInviting = isInviting,
                alreadyInvited = alreadyInvited,
                isHost = isResultHost
            )

            notFound -> InviteBodyState.NotFound
            else -> InviteBodyState.Idle
        }

        AnimatedContent(
            targetState = bodyState,
            contentKey = { it::class },
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "invite_body"
        ) { target ->
            when (target) {
                InviteBodyState.Searching -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 136.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LynkProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                is InviteBodyState.Match -> {
                    InviteResultRow(
                        user = target.user,
                        isInviting = target.isInviting,
                        alreadyInvited = target.alreadyInvited,
                        isHost = target.isHost,
                        onInvite = { onInvite(target.user.userId) }
                    )
                }

                InviteBodyState.NotFound -> {
                    InviteMessage(
                        icon = Lucide.UserX,
                        title = stringResource(Res.string.invite_not_found_title),
                        message = stringResource(Res.string.invite_not_found_message)
                    )
                }

                InviteBodyState.Idle -> {
                    InviteMessage(
                        icon = Lucide.UserRoundSearch,
                        title = stringResource(Res.string.invite_idle_title),
                        message = stringResource(Res.string.invite_idle_message)
                    )
                }
            }
        }
    }
}

@Composable
private fun InviteResultRow(
    user: HangoutUserUi,
    isInviting: Boolean,
    alreadyInvited: Boolean,
    isHost: Boolean,
    onInvite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ParticipantAvatar(
            displayName = user.displayName,
            initials = user.initials,
            profilePictureUrl = user.profilePictureUrl,
            size = 44.dp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LynkText(
                text = user.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LynkText(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val action = when {
            isHost -> InviteActionState.Host
            isInviting -> InviteActionState.Inviting
            alreadyInvited -> InviteActionState.Invited
            else -> InviteActionState.Invite
        }

        AnimatedContent(
            targetState = action,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "invite_action"
        ) { target ->
            when (target) {
                InviteActionState.Host -> {
                    HostChip()
                }

                InviteActionState.Inviting -> {
                    LynkProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                InviteActionState.Invited -> {
                    LynkText(
                        text = stringResource(Res.string.invite_invited),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                InviteActionState.Invite -> {
                    LynkText(
                        text = stringResource(Res.string.invite_action),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .clickable {
                                hapticFeedback(AppHaptic.ImpactMedium)
                                onInvite()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InviteMessage(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(36.dp)
        )
        LynkText(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        LynkText(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HostChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.extended.gold)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.Crown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.extended.onGold,
            modifier = Modifier.size(12.dp)
        )
        LynkText(
            text = stringResource(Res.string.invite_host),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.extended.onGold
        )
    }
}

private val previewInviteUser = HangoutUserUi(
    userId = "u1",
    username = "e.eseka",
    displayName = "Eseka Emmanuel",
    initials = "EE",
    profilePictureUrl = null
)

@Composable
private fun InvitePreview(
    query: String,
    result: HangoutUserUi?,
    isSearching: Boolean,
    notFound: Boolean,
    isInviting: Boolean,
    alreadyInvited: Boolean,
    isResultHost: Boolean = false
) {
    LynkTheme {
        InviteParticipantSheetContent(
            queryState = TextFieldState(query),
            result = result,
            isSearching = isSearching,
            notFound = notFound,
            isInviting = isInviting,
            alreadyInvited = alreadyInvited,
            isResultHost = isResultHost,
            onInvite = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 20.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun InviteSheetIdlePreview() = InvitePreview(
    query = "",
    result = null,
    isSearching = false,
    notFound = false,
    isInviting = false,
    alreadyInvited = false
)

@PreviewLightDark
@Composable
private fun InviteSheetSearchingPreview() = InvitePreview(
    query = "e.eseka",
    result = null,
    isSearching = true,
    notFound = false,
    isInviting = false,
    alreadyInvited = false
)

@PreviewLightDark
@Composable
private fun InviteSheetResultPreview() = InvitePreview(
    query = "e.eseka",
    result = previewInviteUser,
    isSearching = false,
    notFound = false,
    isInviting = false,
    alreadyInvited = false
)

@PreviewLightDark
@Composable
private fun InviteSheetHostPreview() = InvitePreview(
    query = "e.eseka",
    result = previewInviteUser,
    isSearching = false,
    notFound = false,
    isInviting = false,
    alreadyInvited = false,
    isResultHost = true
)

@PreviewLightDark
@Composable
private fun InviteSheetInvitingPreview() = InvitePreview(
    query = "e.eseka",
    result = previewInviteUser,
    isSearching = false,
    notFound = false,
    isInviting = true,
    alreadyInvited = false
)

@PreviewLightDark
@Composable
private fun InviteSheetAlreadyInvitedPreview() = InvitePreview(
    query = "e.eseka",
    result = previewInviteUser,
    isSearching = false,
    notFound = false,
    isInviting = false,
    alreadyInvited = true
)

@PreviewLightDark
@Composable
private fun InviteSheetNotFoundPreview() = InvitePreview(
    query = "ghost_user",
    result = null,
    isSearching = false,
    notFound = true,
    isInviting = false,
    alreadyInvited = false
)