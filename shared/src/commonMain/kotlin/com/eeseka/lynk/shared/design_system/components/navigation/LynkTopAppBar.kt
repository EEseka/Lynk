package com.eeseka.lynk.shared.design_system.components.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownSection
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

@Immutable
data class LynkIosBarButtonItem(
    val title: String? = null,
    val sfSymbol: String? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {},
    val menuItems: List<LynkIosDropDownMenuItem> = emptyList(),
    val menuSections: List<LynkIosDropDownMenuSection> = emptyList(),
)

@Immutable
data class LynkIosDropDownMenuItem(
    val title: String,
    val sfSymbol: String? = null,
    val isDestructive: Boolean = false,
    val isDisabled: Boolean = false,
    val onClick: () -> Unit = {},
)

@Immutable
data class LynkIosDropDownMenuSection(
    val title: String? = null,
    val items: List<LynkIosDropDownMenuItem> = emptyList(),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
fun LynkTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    iosLeadingItems: List<LynkIosBarButtonItem> = emptyList(),
    iosTrailingItems: List<LynkIosBarButtonItem> = emptyList(),
) {
    val mappedLeading = remember(iosLeadingItems) { iosLeadingItems.toUIKitItems() }
    val mappedTrailing = remember(iosTrailingItems) { iosTrailingItems.toUIKitItems() }

    AdaptiveTopBar(
        title = {
            LynkText(text = title)
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
        iosTitle = title,
        iosLeadingItems = mappedLeading,
        iosTrailingItems = mappedTrailing,
    )
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

private fun List<LynkIosDropDownMenuItem>.toAdaptiveItems(): List<AdaptiveDropDownItem> =
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
@Preview
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
            iosLeadingItems = listOf(
                LynkIosBarButtonItem(sfSymbol = "chevron.left", onClick = {})
            ),
            iosTrailingItems = listOf(
                LynkIosBarButtonItem(
                    sfSymbol = "ellipsis.circle",
                    menuItems = listOf(
                        LynkIosDropDownMenuItem(
                            title = "Share",
                            sfSymbol = "square.and.arrow.up",
                            onClick = {}
                        ),
                        LynkIosDropDownMenuItem(
                            title = "Delete",
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun LynkTopAppBarPreviewDark() {
    LynkTheme(true) {
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
            iosLeadingItems = listOf(
                LynkIosBarButtonItem(sfSymbol = "chevron.left", onClick = {})
            ),
            iosTrailingItems = listOf(
                LynkIosBarButtonItem(
                    sfSymbol = "ellipsis.circle",
                    menuItems = listOf(
                        LynkIosDropDownMenuItem(
                            title = "Share",
                            sfSymbol = "square.and.arrow.up",
                            onClick = {}
                        ),
                        LynkIosDropDownMenuItem(
                            title = "Delete",
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