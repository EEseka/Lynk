package com.eeseka.lynk.shared.design_system.components.textfields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.clear_search
import lynk.shared.generated.resources.search
import lynk.shared.generated.resources.search_placeholder
import org.jetbrains.compose.resources.stringResource
@Composable
fun LynkSearchField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.search_placeholder),
    enabled: Boolean = true,
    onSearch: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme

    LynkTextFieldLayout(
        title = null,
        isError = false,
        errorMessage = null,
        helperText = null,
        enabled = enabled,
        shape = CircleShape,
        onFocusChanged = {},
        modifier = modifier
    ) { styleModifier, interactionSource ->

        BasicTextField(
            state = state,
            enabled = enabled,
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (enabled) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            onKeyboardAction = onSearch?.let { action ->
                KeyboardActionHandler { action() }
            },
            cursorBrush = SolidColor(scheme.primary),
            interactionSource = interactionSource,
            modifier = styleModifier,
            decorator = { innerBox ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Lucide.Search,
                        contentDescription = stringResource(Res.string.search),
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.text.isEmpty()) {
                            LynkText(
                                text = placeholder,
                                color = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerBox()
                    }

                    if (state.text.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { state.clearText() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Lucide.CircleX,
                                contentDescription = stringResource(Res.string.clear_search),
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LynkSearchFieldPreview() {
    LynkTheme {
        LynkSearchField(state = TextFieldState("Kotlin"))
    }
}

@Preview
@Composable
private fun LynkSearchFieldPreviewDark() {
    LynkTheme(true) {
        LynkSearchField(state = TextFieldState(""))
    }
}

@Preview(showBackground = true)
@Composable
private fun LynkDisabledSearchFieldPreview() {
    LynkTheme {
        LynkSearchField(
            state = TextFieldState("Kotlin"),
            enabled = false
        )
    }
}

@Preview
@Composable
private fun LynkDisabledSearchFieldPreviewDark() {
    LynkTheme(true) {
        LynkSearchField(
            state = TextFieldState(""),
            enabled = false
        )
    }
}