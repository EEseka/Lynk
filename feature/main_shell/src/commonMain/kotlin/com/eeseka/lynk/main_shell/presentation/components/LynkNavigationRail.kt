package com.eeseka.lynk.main_shell.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.main_shell.presentation.mappers.getIcon
import com.eeseka.lynk.main_shell.presentation.mappers.toTitle
import com.eeseka.lynk.main_shell.presentation.model.LynkNavigationItem
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
fun LynkNavigationRail(
    selectedItem: LynkNavigationItem,
    onItemSelected: (LynkNavigationItem) -> Unit,
    hasUnseenNotifications: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val colors = NavigationRailItemDefaults.colors(
        indicatorColor = scheme.primaryContainer,
        selectedIconColor = scheme.onPrimaryContainer,
        selectedTextColor = scheme.primary
    )
    Row(modifier = modifier) {
        NavigationRail(modifier = Modifier.fillMaxHeight()) {
            LynkNavigationItem.entries.forEach { item ->
                val isSelected = selectedItem == item

                NavigationRailItem(
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
                            item.toTitle().asString(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = colors
                )
            }
        }
        VerticalDivider(
            thickness = 1.dp,
            color = scheme.outlineVariant.copy(alpha = 0.2f)
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkNavigationRailPreview() {
    LynkTheme {
        LynkNavigationRail(
            selectedItem = LynkNavigationItem.PROFILE,
            onItemSelected = {},
            hasUnseenNotifications = false
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkNavigationRailUnreadPreview() {
    LynkTheme {
        LynkNavigationRail(
            selectedItem = LynkNavigationItem.PROFILE,
            onItemSelected = {},
            hasUnseenNotifications = true
        )
    }
}
