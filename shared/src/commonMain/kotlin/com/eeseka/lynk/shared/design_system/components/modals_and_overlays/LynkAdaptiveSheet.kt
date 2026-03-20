package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration

/**
 * Adaptive overlay — bottom sheet on phones, centered dialog on tablets/desktops.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LynkAdaptiveSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val configuration = currentDeviceConfiguration()

    if (configuration.isMobile) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        LaunchedEffect(sheetState.isVisible) {
            if (sheetState.isVisible) {
                sheetState.expand()
            }
        }

        LynkBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            modifier = modifier
        ) {
            content()
        }
    } else {
        Dialog(onDismissRequest = onDismissRequest) {
            Box(
                modifier = modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .heightIn(max = 540.dp)
                    .clip(LynkTheme.shapes.large)
                    .background(LynkTheme.colors.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
    }
}