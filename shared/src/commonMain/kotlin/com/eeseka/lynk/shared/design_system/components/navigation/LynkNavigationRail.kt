package com.eeseka.lynk.shared.design_system.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.eeseka.lynk.shared.navigation.LynkNavigationItem
import com.slapps.cupertino.LocalContentColor

@Composable
fun LynkNavigationRail(
    selectedItem: LynkNavigationItem,
    onItemSelected: (LynkNavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isIOS()) {
        LynkNavigationRailCupertino(selectedItem, onItemSelected, modifier)
    } else {
        LynkNavigationRailMaterial3(selectedItem, onItemSelected, modifier)
    }
}

@Composable
private fun LynkNavigationRailCupertino(
    selectedItem: LynkNavigationItem,
    onItemSelected: (LynkNavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(LynkTheme.colors.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
                    .width(80.dp)
                    .selectableGroup()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LynkNavigationItem.entries.forEach { item ->
                    val isSelected = selectedItem == item
                    val contentColor = if (isSelected) LynkTheme.colors.primary
                        else LynkTheme.colors.onSurfaceVariant
                    val currentIcon = if (isSelected) item.selectedIcon else item.unselectedIcon

                    CupertinoRailItem(
                        selected = isSelected,
                        onClick = { onItemSelected(item) },
                        contentColor = contentColor,
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
                                style = LynkTheme.Typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                color = contentColor
                            )
                        }
                    )
                }
            }
        }

        VerticalDivider(
            thickness = 1.dp,
            color = LynkTheme.colors.outlineVariant.copy(alpha = 0.2f),
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)
            )
        )
    }
}

@Composable
private fun CupertinoRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    contentColor: Color,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val effectiveAlpha = if (pressed && !selected) 0.5f else 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val pressedColor = contentColor.copy(alpha = contentColor.alpha * effectiveAlpha)
        CompositionLocalProvider(
            LocalContentColor provides pressedColor
        ) {
            icon()
            label()
        }
    }
}

@Composable
private fun LynkNavigationRailMaterial3(
    selectedItem: LynkNavigationItem,
    onItemSelected: (LynkNavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            containerColor = LynkTheme.colors.surface
        ) {
            LynkNavigationItem.entries.forEach { item ->
                val isSelected = selectedItem == item
                val currentIcon = if (isSelected) item.selectedIcon else item.unselectedIcon

                NavigationRailItem(
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
                            style = LynkTheme.Typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        indicatorColor = LynkTheme.colors.primaryContainer,
                        selectedIconColor = LynkTheme.colors.onPrimaryContainer,
                        selectedTextColor = LynkTheme.colors.onSurface,
                        unselectedIconColor = LynkTheme.colors.onSurfaceVariant,
                        unselectedTextColor = LynkTheme.colors.onSurfaceVariant
                    )
                )
            }
        }

        VerticalDivider(
            thickness = 1.dp,
            color = LynkTheme.colors.outlineVariant.copy(alpha = 0.2f),
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)
            )
        )
    }
}