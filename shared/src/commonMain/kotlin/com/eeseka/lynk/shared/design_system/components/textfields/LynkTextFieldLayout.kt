package com.eeseka.lynk.shared.design_system.components.textfields

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun LynkTextFieldLayout(
    title: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    textField: @Composable (Modifier, MutableInteractionSource) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        onFocusChanged(isFocused)
    }

    val textFieldStyleModifier = Modifier
        .fillMaxWidth()
        .background(
            color = when {
                isFocused -> scheme.primary.copy(alpha = 0.05f)
                enabled -> scheme.surface
                else -> scheme.surfaceVariant.copy(alpha = 0.5f)
            },
            shape = shape
        )
        .border(
            width = if (isFocused) 2.dp else 1.dp,
            color = when {
                isError -> scheme.error
                isFocused -> scheme.primary
                else -> scheme.outlineVariant
            },
            shape = shape
        )
        .padding(horizontal = 16.dp, vertical = 12.dp)

    val customTextSelectionColors = TextSelectionColors(
        handleColor = if (isError) scheme.error else scheme.primary,
        backgroundColor = if (isError) scheme.error.copy(alpha = 0.4f) else scheme.primary.copy(
            alpha = 0.4f
        )
    )

    Column(modifier = modifier) {
        if (title != null) {
            LynkText(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) scheme.error else scheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        CompositionLocalProvider(
            LocalTextSelectionColors provides customTextSelectionColors
        ) {
            textField(textFieldStyleModifier, interactionSource)
        }

        val showError = isError && errorMessage != null
        val showHelper = !isError && helperText != null

        AnimatedVisibility(
            visible = showError || showHelper,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                LynkText(
                    text = if (isError) errorMessage ?: "" else helperText ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isError) scheme.error else scheme.onSurfaceVariant
                )
            }
        }
    }
}