package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CalendarX2
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SquarePen
import com.composables.icons.lucide.UserRoundPlus
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_cancel
import lynk.feature.hangouts.generated.resources.detail_invite
import lynk.feature.hangouts.generated.resources.detail_leave
import lynk.feature.hangouts.generated.resources.detail_more_actions
import lynk.feature.hangouts.generated.resources.detail_update
import org.jetbrains.compose.resources.stringResource

@Composable
fun detailOverflowItems(
    canEdit: Boolean,
    canCancel: Boolean,
    canLeave: Boolean,
    isCancelling: Boolean,
    isLeaving: Boolean,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onLeaveClick: () -> Unit
): ImmutableList<LynkDropDownItem> {
    val hapticFeedback = rememberAppHaptic()

    val cancelLabel = stringResource(Res.string.detail_cancel)
    val leaveLabel = stringResource(Res.string.detail_leave)
    val editLabel = stringResource(Res.string.detail_update)

    return buildList {
        if (canEdit) {
            add(
                LynkDropDownItem(
                    title = editLabel,
                    icon = Lucide.SquarePen,
                    sfSymbol = "square.and.pencil",
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onEditClick()
                    }
                )
            )
        }
        if (canCancel) {
            add(
                LynkDropDownItem(
                    title = cancelLabel,
                    icon = Lucide.CalendarX2,
                    sfSymbol = "calendar.badge.minus",
                    isDestructive = true,
                    isDisabled = isCancelling,
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onCancelClick()
                    }
                )
            )
        }
        if (canLeave) {
            add(
                LynkDropDownItem(
                    title = leaveLabel,
                    icon = Lucide.LogOut,
                    sfSymbol = "rectangle.portrait.and.arrow.right",
                    isDestructive = true,
                    isDisabled = isLeaving,
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onLeaveClick()
                    }
                )
            )
        }
    }.toImmutableList()
}

@Composable
fun HangoutHeroActions(
    showInvite: Boolean,
    inviteEnabled: Boolean,
    overflowItems: ImmutableList<LynkDropDownItem>,
    isOverflowExpanded: Boolean,
    onOverflowExpandedChange: (Boolean) -> Unit,
    onInviteClick: () -> Unit
) {
    if (!showInvite && overflowItems.isEmpty()) return

    val hapticFeedback = rememberAppHaptic()
    val scheme = MaterialTheme.colorScheme

    val inviteLabel = stringResource(Res.string.detail_invite)
    val moreLabel = stringResource(Res.string.detail_more_actions)

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showInvite) {
            Icon(
                imageVector = Lucide.UserRoundPlus,
                contentDescription = inviteLabel,
                tint = if (inviteEnabled) scheme.onSurfaceVariant
                else scheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(enabled = inviteEnabled) {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onInviteClick()
                    }
                    .padding(8.dp)
                    .size(20.dp)
            )
        }

        if (overflowItems.isNotEmpty()) {
            LynkDropDownMenu(
                expanded = isOverflowExpanded,
                onDismissRequest = { onOverflowExpandedChange(false) },
                items = overflowItems,
                anchor = {
                    Icon(
                        imageVector = Lucide.EllipsisVertical,
                        contentDescription = moreLabel,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .clickable {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onOverflowExpandedChange(true)
                            }
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun HangoutHeroActionsPreview(
    showInvite: Boolean = true,
    inviteEnabled: Boolean = true,
    canEdit: Boolean = true,
    canCancel: Boolean = true,
    canLeave: Boolean = false
) {
    LynkTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            HangoutHeroActions(
                showInvite = showInvite,
                inviteEnabled = inviteEnabled,
                overflowItems = detailOverflowItems(
                    canEdit = canEdit,
                    canCancel = canCancel,
                    canLeave = canLeave,
                    isCancelling = false,
                    isLeaving = false,
                    onEditClick = {},
                    onCancelClick = {},
                    onLeaveClick = {}
                ),
                isOverflowExpanded = false,
                onOverflowExpandedChange = {},
                onInviteClick = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HangoutHeroActionsHostPreview() = HangoutHeroActionsPreview()

@PreviewLightDark
@Composable
private fun HangoutHeroActionsInviteFullPreview() = HangoutHeroActionsPreview(inviteEnabled = false)

@PreviewLightDark
@Composable
private fun HangoutHeroActionsAttendeePreview() = HangoutHeroActionsPreview(
    showInvite = false,
    canEdit = false,
    canCancel = false,
    canLeave = true
)
