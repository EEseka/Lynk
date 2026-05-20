package com.eeseka.lynk.create_hangout.presentation.mappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.create_hangout.domain.SearchTab
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.all_spots
import lynk.feature.create_hangout.generated.resources.favorites
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTab.getTitle(): String {
    return stringResource(
        when (this) {
            SearchTab.ALL_SPOTS -> Res.string.all_spots
            SearchTab.FAVORITES -> Res.string.favorites
        }
    )
}

fun SearchTab.getIcon(): ImageVector {
    return when (this) {
        SearchTab.ALL_SPOTS -> Lucide.Globe
        SearchTab.FAVORITES -> Lucide.Bookmark
    }
}