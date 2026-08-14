package com.eeseka.lynk.create_hangout.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
fun LynkDateTimeTile(
    title: String,
    value: String?,
    placeholder: String,
    icon: ImageVector,
    errorMessage: String?,
    isExpanded: Boolean,
    onClick: () -> Unit,
    pickerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = errorMessage != null
    val scheme = MaterialTheme.colorScheme
    val containerColor =
        if (isError) scheme.errorContainer.copy(alpha = 0.1f) else scheme.surfaceContainerHigh
    val contentColor = scheme.onSurface
    val valueTextColor =
        if (value != null) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.6f)

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(containerColor)
                .border(
                    1.dp,
                    if (isError) scheme.error else Color.Transparent,
                    MaterialTheme.shapes.medium
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    LynkText(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LynkText(
                        text = value ?: placeholder,
                        style = MaterialTheme.typography.titleMedium,
                        color = valueTextColor
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                pickerContent()
            }
        }

        AnimatedVisibility(
            visible = isError,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            LynkText(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LynkDateTimeTilePreview() {
    LynkTheme {
        LynkDateTimeTile(
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
            title = "Time",
            value = "20:00",
            placeholder = "Select Time",
            icon = Lucide.Clock,
            errorMessage = null,
            isExpanded = false,
            onClick = {},
            pickerContent = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkDateTimeTileErrorPreview() {
    LynkTheme {
        LynkDateTimeTile(
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
            title = "Time",
            value = "20:00",
            placeholder = "Select Time",
            icon = Lucide.Clock,
            errorMessage = "Please Select a Time",
            isExpanded = false,
            onClick = {},
            pickerContent = {}
        )
    }
}
