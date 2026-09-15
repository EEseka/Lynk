package com.eeseka.lynk.shared.design_system.components.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Trash2
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownSection
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class LynkIosBarButtonItem(
    val title: String? = null,
    val sfSymbol: String? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {},
    val menuItems: ImmutableList<LynkDropDownItem> = persistentListOf(),
    val menuSections: ImmutableList<LynkIosDropDownMenuSection> = persistentListOf()
)

@Immutable
data class LynkIosDropDownMenuSection(
    val title: String? = null,
    val items: ImmutableList<LynkDropDownItem> = persistentListOf()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
fun LynkTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    iosLeadingItems: ImmutableList<LynkIosBarButtonItem> = persistentListOf(),
    iosTrailingItems: ImmutableList<LynkIosBarButtonItem> = persistentListOf(),
) {
    val mappedLeading = remember(iosLeadingItems) { iosLeadingItems.toUIKitItems() }
    val mappedTrailing = remember(iosTrailingItems) { iosTrailingItems.toUIKitItems() }

    // Workaround for Calf (0.14.0): on iOS, AdaptiveTopBar measures the native bar's height once,
    // stops after a few steady frames, and never measures again. After a rotation the content keeps
    // the old orientation's top padding — a big gap in landscape, or content under the bar in portrait.
    // Keying on the window size rebuilds the bar on rotation so it measures again.
    // Remove this key (and this comment) once Calf re-measures on its own: https://github.com/MohamedRejeb/Calf/issues/548
    key(LocalWindowInfo.current.containerSize) {
        AdaptiveTopBar(
            title = {
                title?.let { LynkText(text = it) }
            },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor
            ),
            iosTitle = title.orEmpty(),
            iosLeadingItems = mappedLeading,
            iosTrailingItems = mappedTrailing,
        )
    }
}

private fun List<LynkIosBarButtonItem>.toUIKitItems(): List<UIKitUIBarButtonItem> =
    map { it.toUIKitItem() }

private fun LynkIosBarButtonItem.toUIKitItem(): UIKitUIBarButtonItem {
    val image = sfSymbol?.let { UIKitImage.SystemName(it) }

    return when {
        menuItems.isNotEmpty() || menuSections.isNotEmpty() -> UIKitUIBarButtonItem.withMenu(
            image = image ?: UIKitImage.SystemName("ellipsis.circle"),
            menuItems = menuItems.toAdaptiveItems(),
            menuSections = menuSections.toAdaptiveSections(),
        )

        image != null -> UIKitUIBarButtonItem.image(
            image = image,
            enabled = enabled,
            onClick = onClick
        )

        title != null -> UIKitUIBarButtonItem.title(
            title = title,
            enabled = enabled,
            onClick = onClick
        )

        else -> UIKitUIBarButtonItem.flexibleSpace()
    }
}

private fun List<LynkDropDownItem>.toAdaptiveItems(): List<AdaptiveDropDownItem> =
    map { item ->
        AdaptiveDropDownItem(
            title = item.title,
            iosIcon = item.sfSymbol?.let { UIKitImage.SystemName(it) },
            isDestructive = item.isDestructive,
            isDisabled = item.isDisabled,
            onClick = item.onClick
        )
    }

private fun List<LynkIosDropDownMenuSection>.toAdaptiveSections(): List<AdaptiveDropDownSection> =
    map { section ->
        AdaptiveDropDownSection(
            title = section.title ?: "",
            items = section.items.toAdaptiveItems()
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun LynkTopAppBarPreview() {
    LynkTheme {
        LynkTopAppBar(
            title = "Home",
            navigationIcon = {
                LynkIconButton(onClick = {}) {
                    Icon(
                        imageVector = Lucide.ChevronLeft,
                        contentDescription = null
                    )
                }
            },
            actions = {
                LynkIconButton(onClick = {}) {
                    Icon(
                        imageVector = Lucide.EllipsisVertical,
                        contentDescription = null
                    )
                }
            },
            iosLeadingItems = persistentListOf(
                LynkIosBarButtonItem(sfSymbol = "chevron.left", onClick = {})
            ),
            iosTrailingItems = persistentListOf(
                LynkIosBarButtonItem(
                    sfSymbol = "ellipsis.circle",
                    menuItems = persistentListOf(
                        LynkDropDownItem(
                            title = "Share",
                            icon = Lucide.Share2,
                            sfSymbol = "square.and.arrow.up",
                            onClick = {}
                        ),
                        LynkDropDownItem(
                            title = "Delete",
                            icon = Lucide.Trash2,
                            sfSymbol = "trash",
                            isDestructive = true,
                            onClick = {}
                        )
                    )
                )
            )
        )
    }
}