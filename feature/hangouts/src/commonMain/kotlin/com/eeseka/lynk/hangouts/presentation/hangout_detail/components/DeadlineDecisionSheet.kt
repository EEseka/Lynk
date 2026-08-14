package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRoundMinus
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheetItem
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_decision_cancel
import lynk.feature.hangouts.generated.resources.payment_decision_extend
import lynk.feature.hangouts.generated.resources.payment_decision_message
import lynk.feature.hangouts.generated.resources.payment_decision_proceed
import lynk.feature.hangouts.generated.resources.payment_decision_remove
import lynk.feature.hangouts.generated.resources.payment_decision_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeadlineDecisionSheet(
    unpaidCount: Int,
    onDecision: (DeadlineDecision) -> Unit,
    onDismiss: () -> Unit
) {
    LynkActionSheet(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.payment_decision_title),
        message = stringResource(Res.string.payment_decision_message, unpaidCount),
        items = listOf(
            LynkActionSheetItem(
                text = stringResource(Res.string.payment_decision_extend),
                icon = Lucide.CalendarClock,
                onClick = { onDecision(DeadlineDecision.EXTEND) }
            ),
            LynkActionSheetItem(
                text = stringResource(Res.string.payment_decision_remove),
                icon = Lucide.UserRoundMinus,
                onClick = { onDecision(DeadlineDecision.REMOVE_NON_PAYERS) }
            ),
            LynkActionSheetItem(
                text = stringResource(Res.string.payment_decision_proceed),
                icon = Lucide.Check,
                onClick = { onDecision(DeadlineDecision.PROCEED_ANYWAY) }
            ),
            LynkActionSheetItem(
                text = stringResource(Res.string.payment_decision_cancel),
                icon = Lucide.CircleX,
                isDestructive = true,
                onClick = { onDecision(DeadlineDecision.CANCEL) }
            )
        )
    )
}

@Composable
private fun DeadlineDecisionSheetPreview(unpaidCount: Int = 3) {
    LynkTheme {
        DeadlineDecisionSheet(
            unpaidCount = unpaidCount,
            onDecision = {},
            onDismiss = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun DeadlineDecisionSheetDefaultPreview() = DeadlineDecisionSheetPreview()

@PreviewLightDark
@Composable
private fun DeadlineDecisionSheetOneUnpaidPreview() = DeadlineDecisionSheetPreview(unpaidCount = 1)