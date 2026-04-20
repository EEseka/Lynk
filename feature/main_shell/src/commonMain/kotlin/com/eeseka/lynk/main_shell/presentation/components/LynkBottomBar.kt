package com.eeseka.lynk.main_shell.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.eeseka.lynk.main_shell.domain.LynkNavigationItem
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
    modifier: Modifier = Modifier
) {
    val entries = LynkNavigationItem.entries
    val selectedIndex = entries.indexOf(selectedItem)

    val scheme = MaterialTheme.colorScheme

    AdaptiveNavigationBar(
        modifier = modifier,
        iosItems = entries.map { item ->
            val uiKitImageSystemName = when (item) {
                LynkNavigationItem.DISCOVER -> "map.fill"
                LynkNavigationItem.HANGOUTS -> "calendar"
                LynkNavigationItem.PROFILE -> "person.crop.circle.fill"
            }
            UIKitUITabBarItem(
                title = item.title.asString(),
                image = UIKitImage.SystemName(uiKitImageSystemName)
            )
        },
        iosSelectedIndex = selectedIndex,
        iosOnItemSelected = { index -> onItemSelected(entries[index]) },
        iosConfiguration = UIKitTabBarConfiguration(selectedItemColor = scheme.primary),
        content = {
            entries.forEach { item ->
                val isSelected = selectedItem == item

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onItemSelected(item) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title.asString()
                        )
                    },
                    label = {
                        LynkText(
                            text = item.title.asString(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
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

@Preview
@Composable
private fun LynkBottomBarPreview() {
    LynkTheme {
        LynkBottomBar(
            selectedItem = LynkNavigationItem.DISCOVER,
            onItemSelected = {}
        )
    }
}

@Preview
@Composable
private fun LynkBottomBarPreviewDark() {
    LynkTheme(true) {
        LynkBottomBar(
            selectedItem = LynkNavigationItem.DISCOVER,
            onItemSelected = {}
        )
    }
}