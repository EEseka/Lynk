package com.eeseka.lynk.shared.design_system.components.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoTopAppBar
import com.slapps.cupertino.CupertinoTopAppBarDefaults
import com.slapps.cupertino.ExperimentalCupertinoApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCupertinoApi::class)
@Composable
fun LynkTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = LynkTheme.colors.surface,
    contentColor: Color = LynkTheme.colors.textMain
) {
    if (isIOS()) {
        CupertinoTopAppBar(
            title = {
                LynkText(
                    text = title,
                    style = LynkTheme.LynkTypography.titleMedium,
                    color = contentColor
                )
            },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = CupertinoTopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = contentColor,
                actionIconContentColor = LynkTheme.colors.primary,
                navigationIconContentColor = LynkTheme.colors.primary
            )
        )
    } else {
        TopAppBar(
            title = {
                LynkText(text = title, style = LynkTheme.LynkTypography.titleLarge)
            },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor
            )
        )
    }
}