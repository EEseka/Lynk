package com.eeseka.lynk.shared.design_system.components.textfields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoSearchTextField
import com.slapps.cupertino.CupertinoSearchTextFieldDefaults
import com.slapps.cupertino.ExperimentalCupertinoApi
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.clear_search
import lynk.shared.generated.resources.search
import lynk.shared.generated.resources.search_placeholder
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.search_placeholder),
    enabled: Boolean = true,
    onSearch: (() -> Unit)? = null
) {
    val typedTextStyle = LynkTheme.Typography.bodyLarge.copy(color = LynkTheme.colors.onSurface)

    if (isIOS()) {
        CupertinoSearchTextField(
            value = query,
            onValueChange = onQueryChange,
            enabled = enabled,
            modifier = modifier.fillMaxWidth(),
            textStyle = typedTextStyle,
            placeholder = {
                LynkText(
                    text = placeholder,
                    style = LynkTheme.Typography.bodyLarge,
                    color = LynkTheme.colors.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
            colors = CupertinoSearchTextFieldDefaults.colors(
                cursorColor = LynkTheme.colors.primary,
            ),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Lucide.CircleX,
                            contentDescription = stringResource(Res.string.clear_search),
                            tint = LynkTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )
    } else {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            enabled = enabled,
            modifier = modifier.fillMaxWidth(),
            textStyle = typedTextStyle,
            placeholder = {
                LynkText(
                    text = placeholder,
                    style = LynkTheme.Typography.bodyLarge,
                    color = LynkTheme.colors.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Lucide.Search,
                    contentDescription = stringResource(Res.string.search),
                    tint = LynkTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Lucide.CircleX,
                            contentDescription = stringResource(Res.string.clear_search),
                            tint = LynkTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LynkTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = LynkTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                focusedBorderColor = LynkTheme.colors.primary,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                cursorColor = LynkTheme.colors.primary,
                focusedTextColor = LynkTheme.colors.onSurface,
                unfocusedTextColor = LynkTheme.colors.onSurface
            )
        )
    }
}