package com.eeseka.lynk.hangouts.presentation.mappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.model.SearchTab
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.all_spots
import lynk.feature.hangouts.generated.resources.favorites
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTab.getTitle(): String = stringResource(
    when (this) {
        SearchTab.ALL_SPOTS -> Res.string.all_spots
        SearchTab.FAVORITES -> Res.string.favorites
    }
)

fun SearchTab.getIcon(): ImageVector = when (this) {
    SearchTab.ALL_SPOTS -> Lucide.Globe
    SearchTab.FAVORITES -> Lucide.Bookmark
}
