package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoDropdownMenu
import com.slapps.cupertino.ExperimentalCupertinoApi

data class LynkDropDownItem(
    val title: String,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkDropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<LynkDropDownItem>,
    modifier: Modifier = Modifier
) {
    if (isIOS()) {
        CupertinoDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            containerColor = LynkTheme.colors.surface,
            modifier = modifier
        ) {
            items.forEachIndexed { index, item ->
                val contentColor = if (item.isDestructive) LynkTheme.colors.error
                    else LynkTheme.colors.onSurface

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                item.onClick()
                                onDismissRequest()
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = item.title,
                        style = LynkTheme.Typography.bodyLarge,
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

                if (index != items.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = LynkTheme.colors.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    } else {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            containerColor = LynkTheme.colors.surface,
            border = BorderStroke(
                width = 1.dp,
                color = LynkTheme.colors.outlineVariant.copy(alpha = 0.5f)
            ),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            items.forEachIndexed { index, item ->
                val contentColor = if (item.isDestructive) LynkTheme.colors.error
                    else LynkTheme.colors.onSurface

                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LynkText(
                                text = item.title,
                                style = LynkTheme.Typography.bodyLarge,
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
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = MenuDefaults.itemColors(
                        textColor = contentColor,
                        leadingIconColor = contentColor,
                        trailingIconColor = contentColor
                    )
                )

                if (index != items.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = LynkTheme.colors.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}