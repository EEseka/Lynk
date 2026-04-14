package com.eeseka.lynk.shared.design_system.components.textfields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.User
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
fun LynkTextField(
    state: TextFieldState,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    errorMessage: String? = null,
    helperText: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    LynkTextFieldLayout(
        title = label,
        isError = isError,
        errorMessage = errorMessage,
        helperText = helperText,
        enabled = enabled,
        onFocusChanged = {},
        modifier = modifier
    ) { styleModifier, interactionSource ->

        BasicTextField(
            state = state,
            enabled = enabled,
            lineLimits = if (singleLine) TextFieldLineLimits.SingleLine else TextFieldLineLimits.Default,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (enabled) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(if (isError) scheme.error else scheme.primary),
            interactionSource = interactionSource,
            modifier = styleModifier,
            decorator = { innerBox ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.text.isEmpty() && placeholder != null) {
                            LynkText(
                                text = placeholder,
                                color = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerBox()
                    }

                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        trailingIcon()
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LynkTextFieldPreview() {
    LynkTheme {
        LynkTextField(
            state = TextFieldState("Emmanuel"),
            leadingIcon = {
                Icon(
                    imageVector = Lucide.User,
                    contentDescription = null
                )
            }
        )
    }
}

@Preview
@Composable
private fun LynkTextFieldPreviewDark() {
    LynkTheme(true) {
        LynkTextField(
            state = TextFieldState("Emmanuel"),
            leadingIcon = {
                Icon(
                    imageVector = Lucide.User,
                    contentDescription = null
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LynkErrorTextFieldPreview() {
    LynkTheme {
        LynkTextField(
            state = TextFieldState("Emmanuel"),
            leadingIcon = {
                Icon(
                    imageVector = Lucide.User,
                    contentDescription = null
                )
            },
            isError = true,
            errorMessage = "Invalid name"
        )
    }
}

@Preview
@Composable
private fun LynkErrorTextFieldPreviewDark() {
    LynkTheme(true) {
        LynkTextField(
            state = TextFieldState("Emmanuel"),
            leadingIcon = {
                Icon(
                    imageVector = Lucide.User,
                    contentDescription = null
                )
            },
            isError = true,
            errorMessage = "Invalid name"
        )
    }
}