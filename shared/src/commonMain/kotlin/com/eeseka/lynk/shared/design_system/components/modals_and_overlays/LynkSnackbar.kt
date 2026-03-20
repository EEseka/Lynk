package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import kotlinx.coroutines.delay

enum class LynkFlashType {
    Success,
    Error,
    Warning,
    Info
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
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    currentSnackbarData?.dismiss()
    showSnackbar(
        LynkFlashVisuals(
            message = message,
            type = type,
            duration = duration
        )
    )
}

@Composable
fun LynkFlashMessageHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val currentData = hostState.currentSnackbarData
    var displayedData by remember { mutableStateOf(currentData) }

    LaunchedEffect(currentData) {
        if (currentData != null) {
            displayedData = currentData
            val timeout = when (currentData.visuals.duration) {
                SnackbarDuration.Short -> 3000L
                SnackbarDuration.Long -> 5000L
                SnackbarDuration.Indefinite -> Long.MAX_VALUE
            }
            delay(timeout)
            currentData.dismiss()
        }
    }

    AnimatedVisibility(
        visible = currentData != null,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        ) + fadeOut(),
        modifier = modifier
    ) {
        displayedData?.let { data ->
            val visuals = data.visuals as? LynkFlashVisuals
            val type = visuals?.type ?: LynkFlashType.Info

            LynkFlashPill(
                message = data.visuals.message,
                type = type,
                modifier = Modifier.pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -5f) currentData?.dismiss()
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
    modifier: Modifier = Modifier
) {
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
        LynkFlashType.Success -> LynkTheme.colors.primary
        LynkFlashType.Error -> LynkTheme.colors.error
        LynkFlashType.Warning -> LynkTheme.colors.secondary
        LynkFlashType.Info -> LynkTheme.colors.surfaceVariant
    }

    val contentColor = when (type) {
        LynkFlashType.Info -> LynkTheme.colors.onSurface
        LynkFlashType.Success -> LynkTheme.colors.onPrimary
        LynkFlashType.Error -> LynkTheme.colors.onError
        LynkFlashType.Warning -> LynkTheme.colors.onSecondary
    }

    val icon = when (type) {
        LynkFlashType.Success -> Lucide.CircleCheck
        LynkFlashType.Error -> Lucide.CircleX
        LynkFlashType.Warning -> Lucide.CircleAlert
        LynkFlashType.Info -> Lucide.Info
    }

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .widthIn(max = 480.dp)
            .shadow(elevation = 8.dp, shape = LynkTheme.shapes.pill)
            .clip(LynkTheme.shapes.pill)
            .background(containerColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            style = LynkTheme.Typography.bodyMedium
        )
    }
}