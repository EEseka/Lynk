package com.eeseka.lynk.shared.design_system.components.textfields

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.User
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

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
    val scheme = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let {
            { LynkText(text = it) }
        },
        placeholder = placeholder?.let {
            { LynkText(text = it) }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = {
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
        },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = MaterialTheme.shapes.medium
    )
}

@Preview(showBackground = true)
@Composable
private fun LynkTextFieldPreview() {
    LynkTheme {
        LynkTextField(
            value = "Emmanuel",
            onValueChange = {},
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
            value = "Emmanuel",
            onValueChange = {},
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
            value = "Emmanuel",
            onValueChange = {},
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
            value = "Emmanuel",
            onValueChange = {},
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