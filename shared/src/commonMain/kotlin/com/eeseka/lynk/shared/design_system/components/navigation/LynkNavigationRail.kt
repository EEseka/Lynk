package com.eeseka.lynk.shared.design_system.components.navigation

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.eeseka.lynk.shared.navigation.LynkNavigationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun LynkNavigationRail(
    selectedItem: LynkNavigationItem,
    onItemSelected: (LynkNavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = if (isIOS()) {
        NavigationRailItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
        )
    } else {
        NavigationRailItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface
        )
    }
    Row(modifier = modifier) {
        NavigationRail(modifier = Modifier.fillMaxHeight()) {
            LynkNavigationItem.entries.forEach { item ->
                val isSelected = selectedItem == item

                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onItemSelected(item) },
                    icon = {
                        Icon(
                            item.icon,
                            contentDescription = item.title.asString()
                        )
                    },
                    label = {
                        Text(
                            item.title.asString(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    },
                    colors = colors,
                    interactionSource = if (isIOS()) remember { NoRippleInteractionSource() } else null

                )
            }
        }
        VerticalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    }
}

/**
 * No-ripple interaction source for iOS to provide native feel
 */
private class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}

@Preview
@Composable
private fun LynkNavigationRailPreview() {
    LynkTheme {
        LynkNavigationRail(
            selectedItem = LynkNavigationItem.PROFILE,
            onItemSelected = {}
        )
    }
}

@Preview
@Composable
private fun LynkNavigationRailPreviewDark() {
    LynkTheme(true) {
        LynkNavigationRail(
            selectedItem = LynkNavigationItem.PROFILE,
            onItemSelected = {}
        )
    }
}