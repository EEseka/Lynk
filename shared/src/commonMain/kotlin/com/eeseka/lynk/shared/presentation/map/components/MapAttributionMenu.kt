package com.eeseka.lynk.shared.presentation.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import kotlinx.collections.immutable.persistentListOf
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.map_data_label
import lynk.shared.generated.resources.maptiler_attribution
import lynk.shared.generated.resources.osm_attribution
import lynk.shared.generated.resources.show_map_attribution
import org.jetbrains.compose.resources.stringResource

private const val OSM_COPYRIGHT_LINK = "https://www.openstreetmap.org/copyright"
private const val MAPTILER_COPYRIGHT_LINK = "https://www.maptiler.com/copyright"

@Composable
fun MapAttributionMenu(modifier: Modifier = Modifier) {
    val hapticFeedback = rememberAppHaptic()
    val uriHandler = LocalUriHandler.current

    var showAttributionMenu by remember { mutableStateOf(false) }

    LynkDropDownMenu(
        expanded = showAttributionMenu,
        onDismissRequest = { showAttributionMenu = false },
        modifier = modifier,
        items = persistentListOf(
            LynkDropDownItem(
                title = stringResource(Res.string.osm_attribution),
                onClick = {
                    hapticFeedback(AppHaptic.ImpactLight)
                    uriHandler.openUri(OSM_COPYRIGHT_LINK)
                }
            ),
            LynkDropDownItem(
                title = stringResource(Res.string.maptiler_attribution),
                onClick = {
                    hapticFeedback(AppHaptic.ImpactLight)
                    uriHandler.openUri(MAPTILER_COPYRIGHT_LINK)
                }
            )
        ),
        anchor = {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .clickable(
                        onClickLabel = stringResource(Res.string.show_map_attribution),
                        role = Role.Button
                    ) {
                        hapticFeedback(AppHaptic.ImpactLight)
                        showAttributionMenu = true
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Lucide.Info,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                LynkText(
                    text = stringResource(Res.string.map_data_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    )
}
