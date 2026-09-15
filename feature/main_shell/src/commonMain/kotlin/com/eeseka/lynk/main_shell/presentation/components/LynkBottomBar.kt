package com.eeseka.lynk.main_shell.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.eeseka.lynk.main_shell.presentation.mappers.getIcon
import com.eeseka.lynk.main_shell.presentation.mappers.toSfSymbolName
import com.eeseka.lynk.main_shell.presentation.mappers.toTitle
import com.eeseka.lynk.main_shell.presentation.model.LynkNavigationItem
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun LynkBottomBar(
    selectedItem: LynkNavigationItem,
    onItemSelected: (LynkNavigationItem) -> Unit,
    hasUnseenNotifications: Boolean,
    modifier: Modifier = Modifier
) {
    val entries = LynkNavigationItem.entries

    val scheme = MaterialTheme.colorScheme

    AdaptiveNavigationBar(
        modifier = modifier,
        iosItems = entries.map { item ->
            UIKitUITabBarItem(
                title = item.toTitle().asString(),
                image = UIKitImage.SystemName(item.toSfSymbolName(hasUnseenNotifications))
            )
        },
        iosSelectedIndex = selectedItem.ordinal,
        iosOnItemSelected = { index -> onItemSelected(entries[index]) },
        iosConfiguration = UIKitTabBarConfiguration(selectedItemColor = scheme.primary),
        content = {
            entries.forEach { item ->
                val isSelected = selectedItem == item

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onItemSelected(item) },
                    icon = {
                        NavigationItemIcon(
                            icon = item.getIcon(),
                            hasUnread = hasUnseenNotifications && item == LynkNavigationItem.HANGOUTS
                        )
                    },
                    label = {
                        LynkText(
                            text = item.toTitle().asString(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = scheme.onPrimaryContainer,
                        selectedTextColor = scheme.primary,
                        indicatorColor = scheme.primaryContainer
                    )
                )
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun LynkBottomBarPreview() {
    LynkTheme {
        LynkBottomBar(
            selectedItem = LynkNavigationItem.DISCOVER,
            onItemSelected = {},
            hasUnseenNotifications = false
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkBottomBarUnreadPreview() {
    LynkTheme {
        LynkBottomBar(
            selectedItem = LynkNavigationItem.DISCOVER,
            onItemSelected = {},
            hasUnseenNotifications = true
        )
    }
}
