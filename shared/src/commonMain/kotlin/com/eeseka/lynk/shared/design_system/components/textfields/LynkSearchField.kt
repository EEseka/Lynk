package com.eeseka.lynk.shared.design_system.components.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
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
    showClearButton: Boolean = true,
    onSearch: (() -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scheme = MaterialTheme.colorScheme

    val containerColor = if (enabled) {
        scheme.extended.lavenderGrey
    } else {
        scheme.extended.lavenderGrey.copy(alpha = 0.5f)
    }
    val contentColor = if (enabled) {
        scheme.extended.onLavenderGrey
    } else {
        scheme.extended.onLavenderGrey.copy(alpha = 0.5f)
    }

    BasicTextField(
        state = state,
        enabled = enabled,
        lineLimits = TextFieldLineLimits.SingleLine,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        onKeyboardAction = KeyboardActionHandler {
            onSearch?.invoke()
            keyboardController?.hide()
            focusManager.clearFocus()
        },
        cursorBrush = SolidColor(scheme.primary),
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(containerColor)
            .padding(start = 16.dp, end = 4.dp),
        decorator = { innerBox ->
            val showClear = showClearButton && state.text.isNotEmpty()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.Search,
                    contentDescription = stringResource(Res.string.search),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (showClear) 0.dp else 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (state.text.isEmpty()) {
                        LynkText(
                            text = placeholder,
                            color = contentColor.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerBox()
                }

                if (showClear) {
                    Icon(
                        imageVector = Lucide.CircleX,
                        contentDescription = stringResource(Res.string.clear_search),
                        tint = contentColor,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .clickable { state.clearText() }
                            .padding(12.dp)
                            .size(24.dp)
                    )
                }
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun LynkSearchFieldPreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            LynkSearchField(state = TextFieldState("Kotlin"))
            LynkSearchField(state = TextFieldState())
            LynkSearchField(state = TextFieldState(), enabled = false)
        }
    }
}
