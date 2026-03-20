package com.eeseka.lynk.shared.design_system.components.textfields

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoTextField
import com.slapps.cupertino.CupertinoTextFieldDefaults
import com.slapps.cupertino.ExperimentalCupertinoApi

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val shape = LynkTheme.shapes.medium
    val typedTextStyle = LynkTheme.Typography.bodyLarge.copy(color = LynkTheme.colors.onSurface)

    if (isIOS()) {
        Column(modifier = modifier.fillMaxWidth()) {
            if (label != null) {
                LynkText(
                    text = label,
                    style = LynkTheme.Typography.labelLarge,
                    color = if (isError) LynkTheme.colors.error else LynkTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            val borderColor =
                if (isError) LynkTheme.colors.error else LynkTheme.colors.outlineVariant

            CupertinoTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LynkTheme.colors.background, shape)
                    .border(1.dp, borderColor, shape)
                    .clip(shape)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                textStyle = typedTextStyle,
                placeholder = placeholder?.let {
                    {
                        LynkText(
                            text = it,
                            style = LynkTheme.Typography.bodyLarge,
                            color = LynkTheme.colors.onSurfaceVariant
                        )
                    }
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
                colors = CupertinoTextFieldDefaults.colors(
                    cursorColor = if (isError) LynkTheme.colors.error else LynkTheme.colors.primary,
                    focusedTextColor = LynkTheme.colors.onSurface,
                    unfocusedTextColor = LynkTheme.colors.onSurface
                )
            )

            val showSupportingText =
                (isError && errorMessage != null) || (!isError && helperText != null)

            AnimatedVisibility(
                visible = showSupportingText,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    LynkText(
                        text = if (isError) errorMessage ?: "" else helperText ?: "",
                        style = LynkTheme.Typography.labelSmall,
                        color = if (isError) LynkTheme.colors.error else LynkTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            textStyle = typedTextStyle,
            label = label?.let {
                { LynkText(text = it, style = LynkTheme.Typography.labelLarge) }
            },
            placeholder = placeholder?.let {
                {
                    LynkText(
                        text = it,
                        style = LynkTheme.Typography.bodyLarge,
                        color = LynkTheme.colors.onSurfaceVariant
                    )
                }
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            supportingText = {
                if (isError && errorMessage != null) {
                    LynkText(
                        text = errorMessage,
                        style = LynkTheme.Typography.labelSmall,
                        color = LynkTheme.colors.error
                    )
                } else if (helperText != null) {
                    LynkText(
                        text = helperText,
                        style = LynkTheme.Typography.labelSmall,
                        color = LynkTheme.colors.onSurfaceVariant
                    )
                }
            },
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LynkTheme.colors.primary,
                unfocusedBorderColor = LynkTheme.colors.outline,
                errorBorderColor = LynkTheme.colors.error,
                focusedLabelColor = LynkTheme.colors.primary,
                unfocusedLabelColor = LynkTheme.colors.onSurfaceVariant,
                errorLabelColor = LynkTheme.colors.error,
                cursorColor = LynkTheme.colors.primary,
                errorCursorColor = LynkTheme.colors.error,
                errorSupportingTextColor = LynkTheme.colors.error,
                focusedTextColor = LynkTheme.colors.onSurface,
                unfocusedTextColor = LynkTheme.colors.onSurface
            )
        )
    }
}