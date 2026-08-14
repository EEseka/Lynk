package com.eeseka.lynk.shared.design_system.components.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

/**
 * A flat filled search bar, the shape both platforms have settled on: a grey pill, no outline, no
 * focus ring. It deliberately does not use [LynkTextFieldLayout] — that one exists for labelled
 * form fields, and a search bar wants none of its title, helper text, error text or border.
 */
@Composable
fun LynkSearchField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.search_placeholder),
    enabled: Boolean = true,
    onSearch: (() -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scheme = MaterialTheme.colorScheme

    val containerColor = if (enabled) {
        scheme.extended.searchBar
    } else {
        scheme.extended.searchBar.copy(alpha = 0.5f)
    }
    val contentColor = if (enabled) {
        scheme.extended.onSearchBar
    } else {
        scheme.extended.onSearchBar.copy(alpha = 0.5f)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        decorator = { innerBox ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.Search,
                    contentDescription = stringResource(Res.string.search),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier.weight(1f),
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

                if (state.text.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { state.clearText() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.CircleX,
                            contentDescription = stringResource(Res.string.clear_search),
                            tint = contentColor
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun LynkSearchFieldPreview(
    text: String = "Kotlin",
    enabled: Boolean = true
) {
    LynkTheme {
        LynkSearchField(
            state = TextFieldState(text),
            enabled = enabled,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkSearchFieldFilledPreview() = LynkSearchFieldPreview()

@PreviewLightDark
@Composable
private fun LynkSearchFieldPlaceholderPreview() = LynkSearchFieldPreview(text = "")

@PreviewLightDark
@Composable
private fun LynkSearchFieldDisabledPreview() =
    LynkSearchFieldPreview(text = "", enabled = false)
