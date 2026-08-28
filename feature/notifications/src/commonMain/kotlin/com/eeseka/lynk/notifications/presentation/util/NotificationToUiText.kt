package com.eeseka.lynk.notifications.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Ban
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.CircleDollarSign
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Landmark
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.PartyPopper
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.UserMinus
import com.composables.icons.lucide.UserPlus
import com.composables.icons.lucide.Undo2
import com.composables.icons.lucide.Vote
import com.composables.icons.lucide.Wallet
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import com.eeseka.lynk.shared.presentation.notification.model.NotificationUi
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toNairaString
import lynk.feature.notifications.generated.resources.Res
import lynk.feature.notifications.generated.resources.notification_details_edited
import lynk.feature.notifications.generated.resources.notification_hangout_cancelled
import lynk.feature.notifications.generated.resources.notification_hangout_completed
import lynk.feature.notifications.generated.resources.notification_hangout_started
import lynk.feature.notifications.generated.resources.notification_invite_cancelled
import lynk.feature.notifications.generated.resources.notification_participant_invited
import lynk.feature.notifications.generated.resources.notification_participant_left
import lynk.feature.notifications.generated.resources.notification_payment_deadline_changed
import lynk.feature.notifications.generated.resources.notification_payment_deadline_needs_decision
import lynk.feature.notifications.generated.resources.notification_payment_deadline_resolved
import lynk.feature.notifications.generated.resources.notification_payment_received
import lynk.feature.notifications.generated.resources.notification_payment_received_no_amount
import lynk.feature.notifications.generated.resources.notification_payments_enabled
import lynk.feature.notifications.generated.resources.notification_payout_failed
import lynk.feature.notifications.generated.resources.notification_payout_nothing_to_send
import lynk.feature.notifications.generated.resources.notification_payout_succeeded
import lynk.feature.notifications.generated.resources.notification_refund_issued
import lynk.feature.notifications.generated.resources.notification_removed_for_non_payment
import lynk.feature.notifications.generated.resources.notification_schedule_changed
import lynk.feature.notifications.generated.resources.notification_someone
import lynk.feature.notifications.generated.resources.notification_spot_chosen
import lynk.feature.notifications.generated.resources.notification_voting_reopened
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotificationUi.toUiText(): UiText {
    val someone = stringResource(Res.string.notification_someone)
    val actor = actorDisplayName ?: someone
    val amount = amountKobo?.toNairaString()

    return when (type) {
        NotificationType.PARTICIPANT_INVITED -> UiText.Resource(
            Res.string.notification_participant_invited,
            arrayOf(actor, hangoutName)
        )

        NotificationType.INVITE_CANCELLED -> UiText.Resource(
            Res.string.notification_invite_cancelled,
            arrayOf(actor, hangoutName)
        )

        NotificationType.REMOVED_FOR_NON_PAYMENT -> UiText.Resource(
            Res.string.notification_removed_for_non_payment,
            arrayOf(hangoutName)
        )

        NotificationType.PARTICIPANT_LEFT -> UiText.Resource(
            Res.string.notification_participant_left,
            arrayOf(actor, hangoutName)
        )

        NotificationType.DETAILS_EDITED -> UiText.Resource(
            Res.string.notification_details_edited,
            arrayOf(actor, hangoutName)
        )

        NotificationType.SCHEDULE_CHANGED -> UiText.Resource(
            Res.string.notification_schedule_changed,
            arrayOf(actor, hangoutName)
        )

        NotificationType.SPOT_CHOSEN -> UiText.Resource(
            Res.string.notification_spot_chosen,
            arrayOf(actor, hangoutName)
        )

        NotificationType.VOTING_REOPENED -> UiText.Resource(
            Res.string.notification_voting_reopened,
            arrayOf(actor, hangoutName)
        )

        NotificationType.PAYMENTS_ENABLED -> UiText.Resource(
            Res.string.notification_payments_enabled,
            arrayOf(actor, hangoutName)
        )

        NotificationType.HANGOUT_STARTED -> UiText.Resource(
            Res.string.notification_hangout_started,
            arrayOf(hangoutName)
        )

        NotificationType.HANGOUT_COMPLETED -> UiText.Resource(
            Res.string.notification_hangout_completed,
            arrayOf(hangoutName)
        )

        NotificationType.HANGOUT_CANCELLED -> UiText.Resource(
            Res.string.notification_hangout_cancelled,
            arrayOf(actor, hangoutName)
        )

        NotificationType.PAYMENT_DEADLINE_RESOLVED -> UiText.Resource(
            Res.string.notification_payment_deadline_resolved,
            arrayOf(hangoutName)
        )

        NotificationType.PAYMENT_DEADLINE_NEEDS_DECISION -> UiText.Resource(
            Res.string.notification_payment_deadline_needs_decision,
            arrayOf(hangoutName)
        )

        NotificationType.PAYMENT_DEADLINE_CHANGED -> UiText.Resource(
            Res.string.notification_payment_deadline_changed,
            arrayOf(actor, hangoutName)
        )

        NotificationType.PAYOUT_SUCCEEDED -> UiText.Resource(
            Res.string.notification_payout_succeeded,
            arrayOf(amount.orEmpty(), hangoutName)
        )

        NotificationType.PAYOUT_FAILED -> UiText.Resource(
            Res.string.notification_payout_failed,
            arrayOf(amount.orEmpty(), hangoutName)
        )

        NotificationType.PAYOUT_NOTHING_TO_SEND -> UiText.Resource(
            Res.string.notification_payout_nothing_to_send,
            arrayOf(hangoutName)
        )

        NotificationType.PAYMENT_RECEIVED -> if (amount != null) {
            UiText.Resource(
                Res.string.notification_payment_received,
                arrayOf(actor, amount, hangoutName)
            )
        } else {
            UiText.Resource(
                Res.string.notification_payment_received_no_amount,
                arrayOf(actor, hangoutName)
            )
        }

        NotificationType.REFUND_ISSUED -> UiText.Resource(
            Res.string.notification_refund_issued,
            arrayOf(amount.orEmpty(), hangoutName)
        )
    }
}

fun NotificationType.getIcon(): ImageVector {
    return when (this) {
        NotificationType.PARTICIPANT_INVITED -> Lucide.UserPlus
        NotificationType.INVITE_CANCELLED -> Lucide.Ban
        NotificationType.REMOVED_FOR_NON_PAYMENT -> Lucide.UserMinus
        NotificationType.PARTICIPANT_LEFT -> Lucide.UserMinus

        NotificationType.DETAILS_EDITED -> Lucide.Pencil
        NotificationType.SCHEDULE_CHANGED -> Lucide.CalendarClock
        NotificationType.SPOT_CHOSEN -> Lucide.MapPin
        NotificationType.VOTING_REOPENED -> Lucide.Vote
        NotificationType.PAYMENTS_ENABLED -> Lucide.Wallet

        NotificationType.HANGOUT_STARTED -> Lucide.Play
        NotificationType.HANGOUT_COMPLETED -> Lucide.PartyPopper
        NotificationType.HANGOUT_CANCELLED -> Lucide.Ban

        NotificationType.PAYMENT_DEADLINE_RESOLVED -> Lucide.BadgeCheck
        NotificationType.PAYMENT_DEADLINE_NEEDS_DECISION -> Lucide.TriangleAlert
        NotificationType.PAYMENT_DEADLINE_CHANGED -> Lucide.Clock
        NotificationType.PAYOUT_SUCCEEDED -> Lucide.Landmark
        NotificationType.PAYOUT_FAILED -> Lucide.TriangleAlert
        NotificationType.PAYOUT_NOTHING_TO_SEND -> Lucide.Landmark

        NotificationType.PAYMENT_RECEIVED -> Lucide.CircleDollarSign
        NotificationType.REFUND_ISSUED -> Lucide.Undo2
    }
}