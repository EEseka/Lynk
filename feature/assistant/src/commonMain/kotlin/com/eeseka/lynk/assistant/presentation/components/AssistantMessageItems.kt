package com.eeseka.lynk.assistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Hourglass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.TriangleAlert
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.rememberMarkdownState
import com.mikepenz.markdown.model.rememberStreamingMarkdownState
import kotlinx.coroutines.delay
import lynk.feature.assistant.generated.resources.Res
import lynk.feature.assistant.generated.resources.answer_pending
import lynk.feature.assistant.generated.resources.check_again
import lynk.feature.assistant.generated.resources.copy_answer
import lynk.feature.assistant.generated.resources.retry_answer
import lynk.feature.assistant.generated.resources.share_answer
import lynk.feature.assistant.generated.resources.try_again
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

private val COPIED_TICK_DURATION = 2.seconds

private val UserBubbleShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 20.dp,
    bottomEnd = 4.dp
)

@Composable
fun UserMessageBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .padding(start = 48.dp)
                .clip(UserBubbleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SelectionContainer {
                LynkText(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AssistantAnswerRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}

@Composable
fun AssistantAnswerText(
    text: String,
    isStreamed: Boolean,
    modifier: Modifier = Modifier
) {
    SelectionContainer(modifier = modifier.fillMaxWidth()) {
        if (isStreamed) {
            StreamedAnswerMarkdown(text = text)
        } else {
            Markdown(
                markdownState = rememberMarkdownState(text, retainState = true, immediate = true),
                colors = markdownColor(text = MaterialTheme.colorScheme.onSurface),
                typography = answerMarkdownTypography(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StreamedAnswerMarkdown(text: String) {
    val currentText by rememberUpdatedState(text)
    val streamingState = rememberStreamingMarkdownState()

    LaunchedEffect(Unit) {
        snapshotFlow { currentText }.collect { shown ->
            val streamedLength = streamingState.content.length
            if (shown.length > streamedLength && shown.startsWith(streamingState.content)) {
                streamingState.append(shown.substring(streamedLength))
            }
        }
    }

    if (text.startsWith(streamingState.content)) {
        Markdown(
            streamingMarkdownState = streamingState,
            colors = markdownColor(text = MaterialTheme.colorScheme.onSurface),
            typography = answerMarkdownTypography(),
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Markdown(
            markdownState = rememberMarkdownState(text, retainState = true, immediate = true),
            colors = markdownColor(text = MaterialTheme.colorScheme.onSurface),
            typography = answerMarkdownTypography(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun answerMarkdownTypography(): MarkdownTypography {
    val typography = MaterialTheme.typography
    val text = typography.bodyLarge.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.sp)
    return markdownTypography(
        h1 = typography.titleLarge,
        h2 = typography.titleMedium,
        h3 = typography.titleSmall,
        h4 = typography.titleSmall,
        h5 = typography.titleSmall,
        h6 = typography.titleSmall,
        text = text,
        paragraph = text,
        ordered = text,
        bullet = text,
        list = text
    )
}

@Composable
fun AssistantAnswerActions(
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onRetryClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    // Copy turns into a tick for a moment, so the confirmation shows where it was tapped
    var isCopied by remember { mutableStateOf(false) }
    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(COPIED_TICK_DURATION)
            isCopied = false
        }
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 28.dp) {
        Row(modifier = modifier.offset(x = (-8).dp)) {
            AssistantAnswerAction(
                icon = if (isCopied) Lucide.Check else Lucide.Copy,
                contentDescription = stringResource(Res.string.copy_answer),
                onClick = {
                    onCopyClick()
                    isCopied = true
                }
            )
            AssistantAnswerAction(
                icon = Lucide.Share2,
                contentDescription = stringResource(Res.string.share_answer),
                onClick = onShareClick
            )
            onRetryClick?.let { onClick ->
                AssistantAnswerAction(
                    icon = Lucide.RefreshCw,
                    contentDescription = stringResource(Res.string.retry_answer),
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun AssistantAnswerAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .size(20.dp)
    )
}

@Composable
fun AssistantFailedAnswer(
    message: String,
    onRetryClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    AssistantNoticeCard(
        icon = Lucide.TriangleAlert,
        iconTint = MaterialTheme.colorScheme.error,
        message = message,
        actionText = stringResource(Res.string.try_again),
        actionIcon = Lucide.RefreshCw,
        onActionClick = onRetryClick,
        modifier = modifier
    )
}

@Composable
fun AssistantPendingAnswer(
    onCheckAgainClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistantNoticeCard(
        icon = Lucide.Hourglass,
        iconTint = MaterialTheme.colorScheme.extended.violet,
        message = stringResource(Res.string.answer_pending),
        actionText = stringResource(Res.string.check_again),
        actionIcon = Lucide.RefreshCw,
        onActionClick = onCheckAgainClick,
        modifier = modifier
    )
}

@Composable
private fun AssistantNoticeCard(
    icon: ImageVector,
    iconTint: Color,
    message: String,
    actionText: String,
    actionIcon: ImageVector,
    onActionClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    LynkCard(style = LynkCardStyle.OUTLINED, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                LynkText(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            onActionClick?.let {
                LynkButton(
                    text = actionText,
                    onClick = it,
                    style = LynkButtonStyle.SECONDARY,
                    leadingIcon = {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AssistantMessageItemsPreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            UserMessageBubble(text = "Somewhere chill for drinks tonight, not too pricey")
            AssistantAnswerRow {
                AssistantAnswerText(
                    text = "### Two relaxed spots near Lekki\n- **The Lounge** for quiet drinks\n- **Rooftop Bar** for the view\n\n| Spot | Price | Vibe |\n| --- | --- | --- |\n| The Lounge | ₦₦ | Quiet |\n| Rooftop Bar | ₦₦₦ | Lively |\n\nBoth are *a short drive* from most of the group.",
                    isStreamed = false
                )
                AssistantAnswerActions(onCopyClick = {}, onShareClick = {}, onRetryClick = {})
            }
            AssistantAnswerRow {
                AssistantStatusLine(title = "Looking for spots…")
            }
            AssistantAnswerRow {
                AssistantFailedAnswer(
                    message = "Lynk took too long to answer. This one didn't count toward today's messages.",
                    onRetryClick = {}
                )
            }
            AssistantAnswerRow {
                AssistantPendingAnswer(onCheckAgainClick = {})
            }
        }
    }
}
