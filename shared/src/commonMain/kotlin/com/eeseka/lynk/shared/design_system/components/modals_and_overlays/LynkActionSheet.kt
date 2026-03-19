package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.AlertActionStyle
import com.slapps.cupertino.CupertinoActionSheetNative
import com.slapps.cupertino.ExperimentalCupertinoApi

data class LynkActionSheetItem(
    val text: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalCupertinoApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LynkActionSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    items: List<LynkActionSheetItem>,
    title: String? = null,
    message: String? = null,
    cancelText: String = "Cancel"
) {
    if (isIOS()) {
        CupertinoActionSheetNative(
            visible = visible,
            onDismissRequest = onDismissRequest,
            title = title,
            message = message
        ) {
            items.forEach { item ->
                action(
                    onClick = {
                        item.onClick()
                        onDismissRequest()
                    },
                    style = if (item.isDestructive) AlertActionStyle.Destructive else AlertActionStyle.Default,
                    title = item.text
                )
            }

            action(
                onClick = onDismissRequest,
                style = AlertActionStyle.Cancel,
                title = cancelText
            )
        }
    } else {
        if (visible) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            LynkBottomSheet(
                onDismissRequest = onDismissRequest,
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (title != null) {
                        LynkText(
                            text = title,
                            style = LynkTheme.LynkTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = LynkTheme.colors.textMuted,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items.forEach { item ->
                        val contentColor =
                            if (item.isDestructive) LynkTheme.colors.error else LynkTheme.colors.textMain
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(LynkTheme.shapes.medium)
                                .clickable {
                                    item.onClick()
                                    onDismissRequest()
                                }
                                .padding(vertical = 16.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.icon != null) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.padding(end = 16.dp).size(20.dp)
                                )
                            }
                            LynkText(
                                text = item.text,
                                style = LynkTheme.LynkTypography.titleMedium,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}