package com.eeseka.lynk.assistant.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowUp
import com.composables.icons.lucide.Hourglass
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.assistant.AssistantConstants.MAX_MESSAGE_LENGTH
import lynk.feature.assistant.generated.resources.Res
import lynk.feature.assistant.generated.resources.input_counter
import lynk.feature.assistant.generated.resources.input_placeholder
import lynk.feature.assistant.generated.resources.send
import org.jetbrains.compose.resources.stringResource

private const val COUNTER_VISIBLE_WITHIN = 200

private val ChatFieldShape = RoundedCornerShape(28.dp)

@Composable
fun AssistantInputBar(
    textState: TextFieldState,
    canSend: Boolean,
    messagesLeftLabel: String?,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = messagesLeftLabel,
            contentKey = { it != null },
            transitionSpec = {
                (fadeIn() + expandVertically()) togetherWith (fadeOut() + shrinkVertically())
            },
            label = "messages_left_label"
        ) { label ->
            label?.let {
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Hourglass,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.extended.violet,
                        modifier = Modifier.size(12.dp)
                    )
                    LynkText(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.extended.violet
                    )
                }
            }
        }

        ChatField(
            textState = textState,
            canSend = canSend,
            onSendClick = onSendClick
        )

        AnimatedVisibility(
            visible = textState.text.length >= MAX_MESSAGE_LENGTH - COUNTER_VISIBLE_WITHIN,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            LynkText(
                text = stringResource(Res.string.input_counter, textState.text.length, MAX_MESSAGE_LENGTH),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
            )
        }
    }
}

@Composable
private fun ChatField(
    textState: TextFieldState,
    canSend: Boolean,
    onSendClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    BasicTextField(
        state = textState,
        lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.extended.onLavenderGrey),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        inputTransformation = InputTransformation.maxLength(MAX_MESSAGE_LENGTH),
        cursorBrush = SolidColor(scheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .clip(ChatFieldShape)
            .background(scheme.extended.lavenderGrey)
            .padding(start = 16.dp),
        decorator = { innerBox ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (textState.text.isEmpty()) {
                        LynkText(
                            text = stringResource(Res.string.input_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.extended.onLavenderGrey.copy(alpha = 0.6f)
                        )
                    }
                    innerBox()
                }

                SendButton(
                    canSend = canSend,
                    onClick = onSendClick
                )
            }
        }
    )
}

@Composable
private fun SendButton(
    canSend: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = if (canSend) scheme.extended.violetContainer else scheme.onSurface.copy(alpha = 0.1f),
        label = "send_button_container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (canSend) scheme.extended.onVioletContainer else scheme.onSurfaceVariant.copy(alpha = 0.38f),
        label = "send_button_content"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .padding(8.dp)
            .size(40.dp)
            .clip(CircleShape)
            .drawBehind { drawRect(containerColor) }
            .clickable(enabled = canSend, onClick = onClick)
    ) {
        Icon(
            imageVector = Lucide.ArrowUp,
            contentDescription = stringResource(Res.string.send),
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun AssistantInputBarPreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            AssistantInputBar(
                textState = TextFieldState("Somewhere with a rooftop"),
                canSend = true,
                messagesLeftLabel = null,
                onSendClick = {}
            )
            AssistantInputBar(
                textState = TextFieldState(),
                canSend = false,
                messagesLeftLabel = "3 of 20 messages left today",
                onSendClick = {}
            )
        }
    }
}
