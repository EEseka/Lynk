package com.eeseka.lynk.shared.design_system.components.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.eeseka.lynk.shared.navigation.LynkNavigationItem
import com.slapps.cupertino.CupertinoNavigationBar
import com.slapps.cupertino.CupertinoNavigationBarDefaults
import com.slapps.cupertino.CupertinoNavigationBarItem
import com.slapps.cupertino.ExperimentalCupertinoApi

@Composable
fun LynkBottomBar(
    selectedItem: LynkNavigationItem,
    onItemSelected: (LynkNavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isIOS()) {
        LynkBottomBarCupertino(selectedItem, onItemSelected, modifier)
    } else {
        LynkBottomBarMaterial3(selectedItem, onItemSelected, modifier)
    }
}

@OptIn(ExperimentalCupertinoApi::class)
@Composable
private fun LynkBottomBarCupertino(
    selectedItem: LynkNavigationItem,
    onItemSelected: (LynkNavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    CupertinoNavigationBar(
        modifier = modifier,
        containerColor = LynkTheme.colors.surface
    ) {
        LynkNavigationItem.entries.forEach { item ->
            val isSelected = selectedItem == item
            val contentColor =
                if (isSelected) LynkTheme.colors.primary else LynkTheme.colors.textMain
            val currentIcon = if (isSelected) item.selectedIcon else item.unselectedIcon

            CupertinoNavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item) },
                pressIndicationEnabled = true,
                icon = {
                    Icon(
                        imageVector = currentIcon,
                        contentDescription = item.title.asString(),
                        tint = contentColor
                    )
                },
                label = {
                    LynkText(
                        text = item.title.asString(),
                        style = LynkTheme.LynkTypography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        color = contentColor
                    )
                },
                colors = CupertinoNavigationBarDefaults.itemColors(
                    selectedIconColor = LynkTheme.colors.primary,
                    unselectedIconColor = LynkTheme.colors.textMain,
                    selectedTextColor = LynkTheme.colors.primary,
                    unselectedTextColor = LynkTheme.colors.textMain
                )
            )
        }
    }
}

@Composable
private fun LynkBottomBarMaterial3(
    selectedItem: LynkNavigationItem,
    onItemSelected: (LynkNavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = LynkTheme.colors.surface
    ) {
        LynkNavigationItem.entries.forEach { item ->
            val isSelected = selectedItem == item
            val currentIcon = if (isSelected) item.selectedIcon else item.unselectedIcon

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = currentIcon,
                        contentDescription = item.title.asString()
                    )
                },
                label = {
                    LynkText(
                        text = item.title.asString(),
                        style = LynkTheme.LynkTypography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = LynkTheme.colors.primaryContainer,
                    selectedIconColor = Color.White,
                    selectedTextColor = LynkTheme.colors.textMain,
                    unselectedIconColor = LynkTheme.colors.textMuted,
                    unselectedTextColor = LynkTheme.colors.textMuted
                )
            )
        }
    }
}