package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDown
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

@Immutable
data class LynkDropDownItem(
    val title: String,
    val icon: ImageVector? = null,
    val sfSymbol: String? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun LynkDropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<LynkDropDownItem>,
    modifier: Modifier = Modifier,
    anchor: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        anchor()

        AdaptiveDropDown(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            iosItems = items.map { item ->
                AdaptiveDropDownItem(
                    title = item.title,
                    iosIcon = item.sfSymbol?.let { UIKitImage.SystemName(it) },
                    isDestructive = item.isDestructive,
                    onClick = {
                        item.onClick()
                        onDismissRequest()
                    }
                )
            },
            materialContent = {
                items.forEachIndexed { index, item ->
                    val contentColor = if (item.isDestructive) scheme.error else scheme.onSurface

                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LynkText(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = contentColor
                                )
                                if (item.icon != null) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = contentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            item.onClick()
                            onDismissRequest()
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = contentColor,
                            leadingIconColor = contentColor,
                            trailingIconColor = contentColor
                        )
                    )

                    if (index != items.lastIndex) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = scheme.outlineVariant.copy(alpha = 0.3f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        )
    }
}