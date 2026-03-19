package com.eeseka.lynk.shared.design_system.components.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoButton
import com.slapps.cupertino.CupertinoButtonDefaults
import com.slapps.cupertino.ExperimentalCupertinoApi

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (isIOS()) {
        CupertinoButton(
            onClick = onClick,
            modifier = modifier.size(48.dp),
            enabled = enabled,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = CupertinoButtonDefaults.plainButtonColors(
                contentColor = LynkTheme.colors.textMain,
                disabledContentColor = LynkTheme.colors.textMain.copy(alpha = 0.38f)
            )
        ) {
            content()
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(48.dp),
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = LynkTheme.colors.textMain,
                disabledContentColor = LynkTheme.colors.textMain.copy(alpha = 0.38f)
            )
        ) {
            content()
        }
    }
}