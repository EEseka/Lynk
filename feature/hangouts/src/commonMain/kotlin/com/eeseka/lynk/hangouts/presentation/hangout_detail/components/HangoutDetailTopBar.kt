package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRoundPlus
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosBarButtonItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_invite
import lynk.feature.hangouts.generated.resources.detail_more_actions
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangoutDetailTopBar(
    showInvite: Boolean,
    inviteEnabled: Boolean,
    canEdit: Boolean,
    canCancel: Boolean,
    canLeave: Boolean,
    isCancelling: Boolean,
    isLeaving: Boolean,
    isOverflowExpanded: Boolean,
    onOverflowExpandedChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onInviteClick: () -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()

    val inviteLabel = stringResource(Res.string.detail_invite)
    val moreLabel = stringResource(Res.string.detail_more_actions)

    val overflowItems = detailOverflowItems(
        canEdit = canEdit,
        canCancel = canCancel,
        canLeave = canLeave,
        isCancelling = isCancelling,
        isLeaving = isLeaving,
        onEditClick = onEditClick,
        onCancelClick = onCancelClick,
        onLeaveClick = onLeaveClick
    )
    LynkTopAppBar(
        navigationIcon = {
            LynkIconButton(
                onClick = {
                    hapticFeedback(AppHaptic.ImpactLight)
                    onBackClick()
                }
            ) {
                Icon(
                    imageVector = Lucide.ChevronLeft,
                    contentDescription = null
                )
            }
        },
        iosLeadingItems = persistentListOf(
            LynkIosBarButtonItem(
                sfSymbol = "chevron.left",
                onClick = {
                    hapticFeedback(AppHaptic.ImpactLight)
                    onBackClick()
                }
            )
        ),
        actions = {
            if (showInvite) {
                LynkIconButton(
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onInviteClick()
                    },
                    enabled = inviteEnabled
                ) {
                    Icon(
                        imageVector = Lucide.UserRoundPlus,
                        contentDescription = inviteLabel
                    )
                }
            }

            if (overflowItems.isNotEmpty()) {
                LynkDropDownMenu(
                    expanded = isOverflowExpanded,
                    onDismissRequest = { onOverflowExpandedChange(false) },
                    items = overflowItems,
                    anchor = {
                        LynkIconButton(
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onOverflowExpandedChange(true)
                            }
                        ) {
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
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onInviteClick()
                        }
                    )
                )
            }
            if (overflowItems.isNotEmpty()) {
                add(
                    LynkIosBarButtonItem(
                        sfSymbol = "ellipsis.circle",
                        menuItems = overflowItems
                    )
                )
            }
        }.reversed().toImmutableList()
    )
}

@Composable
private fun HangoutDetailTopBarPreview(
    showInvite: Boolean = true,
    inviteEnabled: Boolean = true,
    canEdit: Boolean = true,
    canCancel: Boolean = true,
    canLeave: Boolean = false
) {
    LynkTheme {
        HangoutDetailTopBar(
            showInvite = showInvite,
            inviteEnabled = inviteEnabled,
            canEdit = canEdit,
            canCancel = canCancel,
            canLeave = canLeave,
            isCancelling = false,
            isLeaving = false,
            isOverflowExpanded = false,
            onOverflowExpandedChange = {},
            onBackClick = {},
            onInviteClick = {},
            onEditClick = {},
            onCancelClick = {},
            onLeaveClick = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun HangoutDetailTopBarHostPreview() = HangoutDetailTopBarPreview()

@PreviewLightDark
@Composable
private fun HangoutDetailTopBarInviteFullPreview() = HangoutDetailTopBarPreview(inviteEnabled = false)

@PreviewLightDark
@Composable
private fun HangoutDetailTopBarAttendeePreview() = HangoutDetailTopBarPreview(
    showInvite = false,
    canEdit = false,
    canCancel = false,
    canLeave = true
)
