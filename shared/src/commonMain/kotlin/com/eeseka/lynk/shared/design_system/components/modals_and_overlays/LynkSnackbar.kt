package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.flash_dismiss
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

enum class LynkFlashType {
    Success, Error, Warning, Info
}

class LynkFlashVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val type: LynkFlashType = LynkFlashType.Info
) : SnackbarVisuals

suspend fun SnackbarHostState.showFlashMessage(
    message: String,
    type: LynkFlashType = LynkFlashType.Info,
    duration: SnackbarDuration = SnackbarDuration.Short,
    actionLabel: String? = null,
    withDismissAction: Boolean = duration == SnackbarDuration.Indefinite
): SnackbarResult {
    currentSnackbarData?.dismiss()
    yield()

    return showSnackbar(
        LynkFlashVisuals(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction,
            duration = duration,
            type = type
        )
    )
}

@Composable
fun LynkFlashMessageHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val currentData = hostState.currentSnackbarData

    LaunchedEffect(currentData) {
        val data = currentData ?: return@LaunchedEffect

        val timeoutMillis = when (data.visuals.duration) {
            SnackbarDuration.Short -> 3000L
            SnackbarDuration.Long -> 5000L
            SnackbarDuration.Indefinite -> null
        } ?: return@LaunchedEffect

        delay(timeoutMillis.milliseconds)
        data.dismiss()
    }

    AnimatedContent(
        targetState = currentData,
        transitionSpec = {
            (expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()) togetherWith (slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) + fadeOut())
        },
        modifier = modifier,
        label = "FlashMessageAnimation"
    ) { data ->
        if (data != null) {
            val visuals = data.visuals as? LynkFlashVisuals
            val type = visuals?.type ?: LynkFlashType.Info

            val scope = rememberCoroutineScope()
            val dragOffset = remember { Animatable(0f) }

            LynkFlashPill(
                message = data.visuals.message,
                type = type,
                actionLabel = data.visuals.actionLabel,
                onAction = { data.performAction() },
                showDismissAction = data.visuals.withDismissAction,
                onDismiss = { data.dismiss() },
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = dragOffset.value.roundToInt()) }
                    .pointerInput(data) {
                        val dismissThreshold = 32.dp.toPx()
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffset.value <= -dismissThreshold) {
                                    data.dismiss()
                                } else {
                                    scope.launch {
                                        dragOffset.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy
                                            )
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch { dragOffset.animateTo(0f) }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                dragOffset.snapTo(
                                    (dragOffset.value + dragAmount).coerceAtMost(0f)
                                )
                            }
                        }
                    }
            )
        }
    }
}

@Composable
private fun LynkFlashPill(
    message: String,
    type: LynkFlashType,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    showDismissAction: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val triggerHaptic = rememberAppHaptic()

    LaunchedEffect(type) {
        when (type) {
            LynkFlashType.Success -> triggerHaptic(AppHaptic.Success)
            LynkFlashType.Error -> triggerHaptic(AppHaptic.Error)
            LynkFlashType.Warning -> triggerHaptic(AppHaptic.Warning)
            LynkFlashType.Info -> triggerHaptic(AppHaptic.ImpactLight)
        }
    }

    val containerColor = when (type) {
        LynkFlashType.Success -> scheme.extended.successContainer
        LynkFlashType.Error -> scheme.errorContainer
        LynkFlashType.Warning -> scheme.extended.warningContainer
        LynkFlashType.Info -> scheme.inverseSurface
    }

    val contentColor = when (type) {
        LynkFlashType.Success -> scheme.extended.onSuccessContainer
        LynkFlashType.Error -> scheme.onErrorContainer
        LynkFlashType.Warning -> scheme.extended.onWarningContainer
        LynkFlashType.Info -> scheme.inverseOnSurface
    }

    val icon = when (type) {
        LynkFlashType.Success -> Lucide.CircleCheck
        LynkFlashType.Error -> Lucide.CircleAlert
        LynkFlashType.Warning -> Lucide.TriangleAlert
        LynkFlashType.Info -> Lucide.Info
    }

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .widthIn(max = 480.dp)
            .heightIn(min = 48.dp)
            // Added semantic label so screen readers announce it instantly
            .semantics { liveRegion = LiveRegionMode.Polite }
            .shadow(elevation = 8.dp, shape = CircleShape)
            .border(
                BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.2f)),
                CircleShape
            )
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        LynkText(
            text = message,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )

        if (actionLabel != null) {
            Spacer(modifier = Modifier.width(8.dp))
            LynkText(
                text = actionLabel,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable {
                        triggerHaptic(AppHaptic.ImpactLight)
                        onAction()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (showDismissAction) {
            Spacer(modifier = Modifier.width(4.dp))
            LynkText(
                text = stringResource(Res.string.flash_dismiss),
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable {
                        triggerHaptic(AppHaptic.ImpactLight)
                        onDismiss()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun LynkSuccessSnackbarPreview() {
    LynkTheme {
        LynkFlashPill(
            message = "Something occurred successfully!",
            type = LynkFlashType.Success
        )
    }
}

@Preview
@Composable
private fun LynkErrorSnackbarPreview() {
    LynkTheme {
        LynkFlashPill(
            message = "Something went wrong!",
            type = LynkFlashType.Error
        )
    }
}

@Preview
@Composable
private fun LynkWarningSnackbarPreview() {
    LynkTheme {
        LynkFlashPill(
            message = "Something might go wrong!",
            type = LynkFlashType.Warning
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkInfoSnackbarPreview() {
    LynkTheme {
        LynkFlashPill(
            message = "Here is some information.",
            type = LynkFlashType.Info
        )
    }
}

@Preview
@Composable
private fun LynkSnackbarWithActionPreview() {
    LynkTheme {
        LynkFlashPill(
            message = "Could not send your RSVP.",
            type = LynkFlashType.Error,
            actionLabel = "Retry"
        )
    }
}

@Preview
@Composable
private fun LynkSnackbarDismissiblePreview() {
    LynkTheme {
        LynkFlashPill(
            message = "Uploading your photo in the background.",
            type = LynkFlashType.Info,
            showDismissAction = true
        )
    }
}

@Preview
@Composable
private fun LynkSnackbarActionAndDismissPreview() {
    LynkTheme {
        LynkFlashPill(
            message = "Your changes could not be saved to the server.",
            type = LynkFlashType.Warning,
            actionLabel = "Retry",
            showDismissAction = true
        )
    }
}