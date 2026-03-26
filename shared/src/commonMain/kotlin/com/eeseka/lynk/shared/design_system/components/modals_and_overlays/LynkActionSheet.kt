package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Trash2
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dialog.AdaptiveBasicAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosAction
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.rememberAlertDialogIosProperties
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource

@Immutable
data class LynkActionSheetItem(
    val text: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
fun LynkActionSheet(
    onDismissRequest: () -> Unit,
    items: List<LynkActionSheetItem>,
    title: String? = null,
    message: String? = null,
    cancelText: String = stringResource(Res.string.cancel)
) {
    val scheme = MaterialTheme.colorScheme

    val iosProperties = rememberAlertDialogIosProperties(
        title = title ?: "",
        text = message ?: "",
        style = AlertDialogIosStyle.ActionSheet,
        actions = items.map { item ->
            AlertDialogIosAction(
                title = item.text,
                style = if (item.isDestructive) AlertDialogIosActionStyle.Destructive
                else AlertDialogIosActionStyle.Default,
                onClick = {
                    item.onClick()
                    onDismissRequest()
                }
            )
        } + AlertDialogIosAction(
            title = cancelText,
            style = AlertDialogIosActionStyle.Cancel,
            onClick = onDismissRequest
        )
    )

    AdaptiveBasicAlertDialog(
        onDismissRequest = onDismissRequest,
        iosProperties = iosProperties,
        materialContent = {
            val sheetState = rememberAdaptiveSheetState(skipPartiallyExpanded = true)

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
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items.forEach { item ->
                        val contentColor = if (item.isDestructive) scheme.error
                        else scheme.onSurface

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        item.onClick()
                                        onDismissRequest()
                                    }
                                )
                                .padding(vertical = 16.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.icon != null) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(20.dp)
                                )
                            }
                            LynkText(
                                text = item.text,
                                style = MaterialTheme.typography.titleMedium,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
private fun LynkActionSheetPreview() {
    LynkTheme {
        LynkActionSheet(
            onDismissRequest = {},
            title = "What would you like to do?",
            items = previewItems
        )
    }
}

@Preview
@Composable
private fun LynkActionSheetPreviewDark() {
    LynkTheme(true) {
        LynkActionSheet(
            onDismissRequest = {},
            title = "What would you like to do?",
            items = previewItems
        )
    }
}

private val previewItems = listOf(
    LynkActionSheetItem(
        text = "Edit",
        icon = Lucide.Pencil,
        onClick = {}
    ),
    LynkActionSheetItem(
        text = "Share",
        icon = Lucide.Share2,
        onClick = {}
    ),
    LynkActionSheetItem(
        text = "Delete",
        icon = Lucide.Trash2,
        isDestructive = true,
        onClick = {}
    )
)