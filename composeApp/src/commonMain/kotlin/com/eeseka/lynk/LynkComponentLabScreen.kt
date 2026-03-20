package com.eeseka.lynk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Trash2
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.buttons.LynkFloatingActionButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.date_and_time.LynkDatePicker
import com.eeseka.lynk.shared.design_system.components.date_and_time.LynkTimePicker
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheetItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkBottomSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkBottomBar
import com.eeseka.lynk.shared.design_system.components.navigation.LynkNavigationRail
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedControl
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedItem
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedStyle
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSlider
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSwitch
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.navigation.LynkNavigationItem
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Lab tab model — 5 tabs, maps 1:1 to app's 5 LynkNavigationItems by ordinal
// ─────────────────────────────────────────────────────────────────────────────
private enum class LabTab(val label: String) {
    BUTTONS("Buttons"),
    TYPE_COLOR("Type & Color"),
    FORMS("Forms"),
    CARDS("Cards"),
    OVERLAYS("Overlays")
}

// ─────────────────────────────────────────────────────────────────────────────
// Root — portrait = BottomBar, landscape = NavigationRail
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LynkComponentLabScreen() {
    val configuration = currentDeviceConfiguration()
    val isLandscape = configuration == DeviceConfiguration.MOBILE_LANDSCAPE

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = rememberAppHaptic()

    var selectedTab by remember { mutableStateOf(LabTab.FORMS) }
    var selectedNavItem by remember { mutableStateOf(LynkNavigationItem.DISCOVER) }
    var showDropDown by remember { mutableStateOf(false) }

    fun onNavItemSelected(item: LynkNavigationItem) {
        selectedNavItem = item
        haptic(AppHaptic.Selection)
        selectedTab = LabTab.entries.getOrElse(
            LynkNavigationItem.entries.indexOf(item)
        ) { LabTab.BUTTONS }
    }

    val topBar: @Composable () -> Unit = {
        LynkTopAppBar(
            title = selectedTab.label,
            navigationIcon = {
                LynkIconButton(onClick = { haptic(AppHaptic.ImpactLight) }) {
                    Icon(
                        imageVector = Lucide.ChevronLeft,
                        contentDescription = null,
                        tint = LynkTheme.colors.primary   // TINT: explicit, slot doesn't inherit
                    )
                }
            },
            actions = {
                Box {
                    LynkIconButton(onClick = {
                        haptic(AppHaptic.ImpactLight); showDropDown = true
                    }) {
                        Icon(
                            imageVector = Lucide.EllipsisVertical,
                            contentDescription = null,
                            tint = LynkTheme.colors.onSurface   // TINT: explicit
                        )
                    }
                    LynkDropDownMenu(
                        expanded = showDropDown,
                        onDismissRequest = { showDropDown = false },
                        items = listOf(
                            LynkDropDownItem("Share", icon = Lucide.Share2, onClick = {
                                haptic(AppHaptic.Selection)
                                scope.launch {
                                    snackbarHostState.showFlashMessage(
                                        "Shared!",
                                        LynkFlashType.Info
                                    )
                                }
                            }),
                            LynkDropDownItem(
                                "Delete", icon = Lucide.Trash2, isDestructive = true,
                                onClick = { haptic(AppHaptic.Error) })
                        )
                    )
                }
            }
        )
    }

    if (isLandscape) {
        LynkScaffold(
            snackbarHostState = snackbarHostState,
            topBar = topBar,
            contentWindowInsets = WindowInsets(0)
        ) { padding ->
            Row(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.width(padding.calculateLeftPadding(LayoutDirection.Ltr)))
                LynkNavigationRail(
                    selectedItem = selectedNavItem,
                    onItemSelected = ::onNavItemSelected
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    LabContent(selectedTab, snackbarHostState, haptic, scope)
                }
                Spacer(Modifier.width(padding.calculateRightPadding(LayoutDirection.Ltr)))
            }
        }
    } else {
        LynkScaffold(
            snackbarHostState = snackbarHostState,
            topBar = topBar,
            bottomBar = {
                LynkBottomBar(
                    selectedItem = selectedNavItem,
                    onItemSelected = ::onNavItemSelected
                )
            },
            floatingActionButton = {
                LynkFloatingActionButton(
                    onClick = {
                        haptic(AppHaptic.ImpactHeavy)
                        scope.launch {
                            snackbarHostState.showFlashMessage(
                                "LynkFloatingActionButton + LynkFlashMessageHost ✓",
                                type = LynkFlashType.Success
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Lucide.Plus,
                        contentDescription = null,
                        tint = LynkTheme.colors.onPrimary   // TINT: explicit, slot doesn't inherit
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(padding.calculateTopPadding()))
                LabContent(selectedTab, snackbarHostState, haptic, scope)
                Spacer(Modifier.height(padding.calculateBottomPadding()))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab router
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LabContent(
    tab: LabTab,
    snackbarHostState: SnackbarHostState,
    haptic: (AppHaptic) -> Unit,
    scope: CoroutineScope
) {
    when (tab) {
        LabTab.BUTTONS -> ButtonsTab(haptic, snackbarHostState, scope)
        LabTab.TYPE_COLOR -> TypeColorTab()
        LabTab.FORMS -> FormsTab(haptic)
        LabTab.CARDS -> CardsTab(haptic)
        LabTab.OVERLAYS -> OverlaysTab(haptic, snackbarHostState, scope)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LabScrollColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        content()
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LabSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LynkText(
            text = title.uppercase(),
            style = LynkTheme.Typography.labelSmall,
            color = LynkTheme.colors.primary
        )
        content()
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            thickness = 0.5.dp,
            color = LynkTheme.colors.outlineVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 1 — Buttons & Controls
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ButtonsTab(
    haptic: (AppHaptic) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    var isLoading by remember { mutableStateOf(false) }
    var segmentFixed by remember { mutableIntStateOf(0) }
    var segmentChip by remember { mutableIntStateOf(0) }

    LabScrollColumn {
        LabSection("LynkButton — All 5 styles + states") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LynkButton("Primary", onClick = { haptic(AppHaptic.ImpactMedium) })
                LynkButton(
                    "Secondary Outlined", style = LynkButtonStyle.SECONDARY,
                    onClick = { haptic(AppHaptic.ImpactLight) })
                LynkButton(
                    "Destructive Primary", style = LynkButtonStyle.DESTRUCTIVE_PRIMARY,
                    onClick = { haptic(AppHaptic.Error) })
                LynkButton(
                    "Destructive Secondary", style = LynkButtonStyle.DESTRUCTIVE_SECONDARY,
                    onClick = { haptic(AppHaptic.Warning) })
                LynkButton(
                    "Text Button", style = LynkButtonStyle.TEXT,
                    onClick = { haptic(AppHaptic.Selection) })
                LynkButton("Disabled", enabled = false, onClick = {})
                LynkButton(
                    text = "Loading (tap me)",
                    isLoading = isLoading,
                    loadingText = "Processing…",
                    onClick = {
                        haptic(AppHaptic.ImpactMedium)
                        isLoading = true
                        scope.launch {
                            kotlinx.coroutines.delay(2000)
                            isLoading = false
                            snackbarHostState.showFlashMessage("Done!", LynkFlashType.Success)
                        }
                    }
                )
                LynkButton(
                    text = "With Icon",
                    icon = {
                        Icon(
                            imageVector = Lucide.Heart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = LynkTheme.colors.onPrimary   // TINT: explicit, slot doesn't inherit
                        )
                    },
                    onClick = { haptic(AppHaptic.ImpactLight) }
                )
            }
        }

        LabSection("LynkIconButton — enabled + disabled") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LynkIconButton(onClick = { haptic(AppHaptic.ImpactLight) }) {
                    Icon(
                        Lucide.Search, "Search",
                        tint = LynkTheme.colors.onSurface
                    )          // TINT: explicit
                }
                LynkIconButton(onClick = { haptic(AppHaptic.ImpactLight) }) {
                    Icon(
                        Lucide.Bell, "Bell",
                        tint = LynkTheme.colors.onSurface
                    )
                }
                LynkIconButton(onClick = { haptic(AppHaptic.ImpactLight) }) {
                    Icon(
                        Lucide.Share2, "Share",
                        tint = LynkTheme.colors.onSurface
                    )
                }
                LynkIconButton(enabled = false, onClick = {}) {
                    Icon(
                        Lucide.Trash2, "Trash (disabled)",
                        tint = LynkTheme.colors.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }

        LabSection("LynkSegmentedControl — Fixed Bar") {
            LynkSegmentedControl(
                items = listOf(
                    LynkSegmentedItem("Day"), LynkSegmentedItem("Week"),
                    LynkSegmentedItem("Month"), LynkSegmentedItem("Year")
                ),
                selectedIndex = segmentFixed,
                onItemSelected = { segmentFixed = it; haptic(AppHaptic.Selection) },
                style = LynkSegmentedStyle.FIXED_BAR
            )
        }

        LabSection("LynkSegmentedControl — Scrollable Chips") {
            LynkSegmentedControl(
                items = listOf(
                    LynkSegmentedItem("All"), LynkSegmentedItem("Following"),
                    LynkSegmentedItem("Trending"), LynkSegmentedItem("Nearby"),
                    LynkSegmentedItem("Saved"), LynkSegmentedItem("Featured")
                ),
                selectedIndex = segmentChip,
                onItemSelected = { segmentChip = it; haptic(AppHaptic.Selection) },
                style = LynkSegmentedStyle.SCROLLABLE_CHIPS,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp)
            )
        }

        LabSection("LynkProgressIndicator — 3 colors") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LynkProgressIndicator(modifier = Modifier.size(24.dp))
                LynkProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = LynkTheme.colors.secondary
                )
                LynkProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = LynkTheme.colors.tertiary
                )
                LynkText(
                    "primary / secondary / tertiary",
                    style = LynkTheme.Typography.bodyMedium,
                    color = LynkTheme.colors.onSurfaceVariant
                )
            }
        }

        LabSection("AppHaptic — All 7 types") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Success" to AppHaptic.Success, "Warning" to AppHaptic.Warning,
                        "Error" to AppHaptic.Error
                    ).forEach { (l, t) ->
                        LynkButton(
                            l, modifier = Modifier.weight(1f), style = LynkButtonStyle.SECONDARY,
                            onClick = { haptic(t) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Light" to AppHaptic.ImpactLight, "Medium" to AppHaptic.ImpactMedium,
                        "Heavy" to AppHaptic.ImpactHeavy, "Select" to AppHaptic.Selection
                    ).forEach { (l, t) ->
                        LynkButton(
                            l, modifier = Modifier.weight(1f), style = LynkButtonStyle.TEXT,
                            onClick = { haptic(t) })
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 2 — Typography & Color Palette
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TypeColorTab() {
    LabScrollColumn {
        LabSection("Typography — All 10 slots") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LynkText(
                    "Display Large — SpaceGrotesk Bold",
                    style = LynkTheme.Typography.displayLarge
                )
                LynkText(
                    "Headline Large — SpaceGrotesk Bold",
                    style = LynkTheme.Typography.headlineLarge
                )
                LynkText(
                    "Headline Medium — SpaceGrotesk Bold",
                    style = LynkTheme.Typography.headlineMedium
                )
                LynkText("Title Large — PlusJakarta Bold", style = LynkTheme.Typography.titleLarge)
                LynkText(
                    "Title Medium — PlusJakarta Bold",
                    style = LynkTheme.Typography.titleMedium
                )
                LynkText("Body Large — PlusJakarta Regular", style = LynkTheme.Typography.bodyLarge)
                LynkText(
                    "Body Medium — PlusJakarta Medium",
                    style = LynkTheme.Typography.bodyMedium
                )
                LynkText(
                    "Label Large — PlusJakarta Medium",
                    style = LynkTheme.Typography.labelLarge
                )
                LynkText("Label Medium — JetBrains Mono", style = LynkTheme.Typography.labelMedium)
                LynkText("Label Small — JetBrains Mono", style = LynkTheme.Typography.labelSmall)
            }
        }

        LabSection("Brand tokens — bg / on* text") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaletteRow("primary", LynkTheme.colors.primary, LynkTheme.colors.onPrimary)
                PaletteRow(
                    "primaryContainer",
                    LynkTheme.colors.primaryContainer,
                    LynkTheme.colors.onPrimaryContainer
                )
                PaletteRow("secondary", LynkTheme.colors.secondary, LynkTheme.colors.onSecondary)
                PaletteRow(
                    "secondaryContainer",
                    LynkTheme.colors.secondaryContainer,
                    LynkTheme.colors.onSecondaryContainer
                )
                PaletteRow("tertiary", LynkTheme.colors.tertiary, LynkTheme.colors.onTertiary)
                PaletteRow(
                    "tertiaryContainer",
                    LynkTheme.colors.tertiaryContainer,
                    LynkTheme.colors.onTertiaryContainer
                )
            }
        }

        LabSection("Surface tokens") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaletteRow("background", LynkTheme.colors.background, LynkTheme.colors.onBackground)
                PaletteRow("surface", LynkTheme.colors.surface, LynkTheme.colors.onSurface)
                PaletteRow(
                    "surfaceVariant",
                    LynkTheme.colors.surfaceVariant,
                    LynkTheme.colors.onSurfaceVariant
                )
                PaletteRow(
                    "surfaceContainerLow",
                    LynkTheme.colors.surfaceContainerLow,
                    LynkTheme.colors.onSurface
                )
                PaletteRow(
                    "surfaceContainerHigh",
                    LynkTheme.colors.surfaceContainerHigh,
                    LynkTheme.colors.onSurface
                )
            }
        }

        LabSection("Utility tokens") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaletteRow("error", LynkTheme.colors.error, LynkTheme.colors.onError)
                PaletteRow(
                    "errorContainer",
                    LynkTheme.colors.errorContainer,
                    LynkTheme.colors.onErrorContainer
                )
                PaletteRow("outline", LynkTheme.colors.outline, LynkTheme.colors.onSurface)
                PaletteRow(
                    "outlineVariant",
                    LynkTheme.colors.outlineVariant,
                    LynkTheme.colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun PaletteRow(name: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(LynkTheme.shapes.medium)
            .background(bg)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        LynkText(name, style = LynkTheme.Typography.labelMedium, color = fg)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 3 — Forms & Inputs
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FormsTab(haptic: (AppHaptic) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var switchOn by remember { mutableStateOf(true) }
    var switchOff by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0.4f) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pickedTime by remember { mutableStateOf("Not selected") }
    var pickedDate by remember { mutableStateOf("Not selected") }

    LabScrollColumn {
        LabSection("LynkSearchField") {
            LynkSearchField(query = query, onQueryChange = { query = it })
        }

        LabSection("LynkTextField — normal / helper / live error") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LynkTextField(
                    value = name, onValueChange = { name = it },
                    label = "Full Name", placeholder = "Jane Appleseed"
                )
                LynkTextField(
                    value = email, onValueChange = { email = it },
                    label = "Email", placeholder = "you@example.com",
                    helperText = "We'll never share your email."
                )
                LynkTextField(
                    value = password, onValueChange = { password = it },
                    label = "Password", placeholder = "Min. 8 characters",
                    isError = password.isNotEmpty() && password.length < 8,
                    errorMessage = "Password must be at least 8 characters"
                )
            }
        }

        LabSection("LynkSwitch — on / off / disabled") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    LynkText("Push Notifications", style = LynkTheme.Typography.bodyLarge)
                    LynkSwitch(checked = switchOn, onCheckedChange = {
                        switchOn = it; haptic(AppHaptic.Selection)
                    })
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    LynkText("Dark Mode Override", style = LynkTheme.Typography.bodyLarge)
                    LynkSwitch(checked = switchOff, onCheckedChange = {
                        switchOff = it; haptic(AppHaptic.Selection)
                    })
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    LynkText(
                        "Disabled", style = LynkTheme.Typography.bodyLarge,
                        color = LynkTheme.colors.onSurfaceVariant
                    )
                    LynkSwitch(checked = true, onCheckedChange = {}, enabled = false)
                }
            }
        }

        LabSection("LynkSlider") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    LynkText("Volume", style = LynkTheme.Typography.bodyLarge)
                    LynkText(
                        "${(sliderValue * 100).toInt()}%",
                        style = LynkTheme.Typography.labelMedium,
                        color = LynkTheme.colors.primary
                    )
                }
                LynkSlider(value = sliderValue, onValueChange = { sliderValue = it })
            }
        }

        LabSection("LynkDatePicker & LynkTimePicker") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.spacedBy(8.dp),
                    Alignment.CenterVertically
                ) {
                    LynkButton(
                        "Pick Date",
                        icon = {
                            Icon(
                                Lucide.Calendar, null, Modifier.size(18.dp),
                                tint = LynkTheme.colors.onBackground
                            )   // TINT: explicit
                        },
                        modifier = Modifier.weight(1f),
                        style = LynkButtonStyle.SECONDARY,
                        onClick = { showDatePicker = true }
                    )
                    LynkText(
                        pickedDate, style = LynkTheme.Typography.labelMedium,
                        color = LynkTheme.colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.spacedBy(8.dp),
                    Alignment.CenterVertically
                ) {
                    LynkButton(
                        "Pick Time",
                        icon = {
                            Icon(
                                Lucide.Clock, null, Modifier.size(18.dp),
                                tint = LynkTheme.colors.onBackground
                            )   // TINT: explicit
                        },
                        modifier = Modifier.weight(1f),
                        style = LynkButtonStyle.SECONDARY,
                        onClick = { showTimePicker = true }
                    )
                    LynkText(
                        pickedTime, style = LynkTheme.Typography.labelMedium,
                        color = LynkTheme.colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        LynkDatePicker(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { millis ->
                pickedDate = millis?.let {
                    // Proleptic Gregorian calendar — pure Kotlin stdlib, no java/kotlinx-datetime
                    var days = (it / 86_400_000L).toInt()
                    days += 719_468
                    val era = (if (days >= 0) days else days - 146_096) / 146_097
                    val doe = days - era * 146_097
                    val yoe = (doe - doe / 1460 + doe / 36_524 - doe / 146_096) / 365
                    val y = yoe + era * 400
                    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
                    val mp = (5 * doy + 2) / 153
                    val d = doy - (153 * mp + 2) / 5 + 1
                    val m = if (mp < 10) mp + 3 else mp - 9
                    val year = if (m <= 2) y + 1 else y
                    "$d/$m/$year"
                } ?: "None"
            }
        )
    }

    if (showTimePicker) {
        LynkTimePicker(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { h, m ->
                pickedTime = "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 4 — Cards & Layouts
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CardsTab(haptic: (AppHaptic) -> Unit) {
    LabScrollColumn {
        LabSection("LynkCard — FILLED (default)") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LynkCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LynkText("Filled — static", style = LynkTheme.Typography.titleMedium)
                        LynkText(
                            "surfaceVariant@50% background.",
                            style = LynkTheme.Typography.bodyMedium,
                            color = LynkTheme.colors.onSurfaceVariant
                        )
                    }
                }
                LynkCard(Modifier.fillMaxWidth(), onClick = { haptic(AppHaptic.Selection) }) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LynkText("Filled — clickable", style = LynkTheme.Typography.titleMedium)
                        LynkText(
                            "Tap me. Ripple contained inside shape.",
                            style = LynkTheme.Typography.bodyMedium,
                            color = LynkTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        LabSection("LynkCard — OUTLINED") {
            LynkCard(Modifier.fillMaxWidth(), style = LynkCardStyle.OUTLINED) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LynkText("Outlined", style = LynkTheme.Typography.titleMedium)
                    LynkText(
                        "Surface + outlineVariant border.",
                        style = LynkTheme.Typography.bodyMedium,
                        color = LynkTheme.colors.onSurfaceVariant
                    )
                }
            }
        }

        LabSection("LynkCard — ELEVATED") {
            LynkCard(Modifier.fillMaxWidth(), style = LynkCardStyle.ELEVATED) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LynkText("Elevated", style = LynkTheme.Typography.titleMedium)
                    LynkText(
                        "4dp shadow. Use sparingly.",
                        style = LynkTheme.Typography.bodyMedium,
                        color = LynkTheme.colors.onSurfaceVariant
                    )
                }
            }
        }

        LabSection("LynkCard — custom shape + nested") {
            LynkCard(
                Modifier.fillMaxWidth(), shape = LynkTheme.shapes.extraLarge,
                style = LynkCardStyle.OUTLINED
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LynkText("extraLarge shape override", style = LynkTheme.Typography.titleMedium)
                    LynkCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            LynkText("Nested filled card", style = LynkTheme.Typography.bodyLarge)
                            LynkText(
                                "Elevation hierarchy correct.",
                                style = LynkTheme.Typography.bodyMedium,
                                color = LynkTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 5 — Overlays & Modals
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverlaysTab(
    haptic: (AppHaptic) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    var showActionSheet by remember { mutableStateOf(false) }
    var showDestructiveDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showAdaptiveSheet by remember { mutableStateOf(false) }
    var dropDownExpanded by remember { mutableStateOf(false) }

    LabScrollColumn {
        LabSection("LynkFlashMessageHost — All 4 types") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("Success flash", LynkFlashType.Success, AppHaptic.Success),
                    Triple("Error flash", LynkFlashType.Error, AppHaptic.Error),
                    Triple("Warning flash", LynkFlashType.Warning, AppHaptic.Warning),
                    Triple("Info flash", LynkFlashType.Info, AppHaptic.ImpactLight)
                ).forEach { (label, type, hap) ->
                    LynkButton(label, style = LynkButtonStyle.SECONDARY, onClick = {
                        haptic(hap)
                        scope.launch {
                            snackbarHostState.showFlashMessage(
                                "$label — swipe up to dismiss", type = type
                            )
                        }
                    })
                }
            }
        }

        LabSection("LynkDialog — destructive + info") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LynkButton(
                    "Destructive Dialog", style = LynkButtonStyle.DESTRUCTIVE_SECONDARY,
                    onClick = { haptic(AppHaptic.Warning); showDestructiveDialog = true })
                LynkButton(
                    "Info Dialog", style = LynkButtonStyle.SECONDARY,
                    onClick = { haptic(AppHaptic.ImpactMedium); showInfoDialog = true })
            }
        }

        LabSection("LynkActionSheet") {
            LynkButton(
                "Open Action Sheet", style = LynkButtonStyle.SECONDARY,
                onClick = { haptic(AppHaptic.ImpactMedium); showActionSheet = true })
        }

        LabSection("LynkBottomSheet") {
            LynkButton(
                "Open Bottom Sheet", style = LynkButtonStyle.SECONDARY,
                onClick = { haptic(AppHaptic.ImpactHeavy); showBottomSheet = true })
        }

        LabSection("LynkAdaptiveSheet") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LynkButton(
                    "Open Adaptive Sheet", style = LynkButtonStyle.SECONDARY,
                    onClick = { haptic(AppHaptic.ImpactMedium); showAdaptiveSheet = true })
                LynkText(
                    "BottomSheet on phone · centered dialog on tablet",
                    style = LynkTheme.Typography.labelMedium,
                    color = LynkTheme.colors.onSurfaceVariant
                )
            }
        }

        LabSection("LynkDropDownMenu") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LynkText("Tap the icon →", style = LynkTheme.Typography.bodyLarge)
                Spacer(Modifier.width(12.dp))
                Box {
                    LynkIconButton(onClick = {
                        haptic(AppHaptic.ImpactLight); dropDownExpanded = true
                    }) {
                        Icon(
                            Lucide.EllipsisVertical, "Menu",
                            tint = LynkTheme.colors.onSurface
                        )   // TINT: explicit
                    }
                    LynkDropDownMenu(
                        expanded = dropDownExpanded,
                        onDismissRequest = { dropDownExpanded = false },
                        items = listOf(
                            LynkDropDownItem("Share", icon = Lucide.Share2, onClick = {
                                haptic(AppHaptic.Selection)
                                scope.launch {
                                    snackbarHostState.showFlashMessage(
                                        "Shared!",
                                        LynkFlashType.Info
                                    )
                                }
                            }),
                            LynkDropDownItem("Favourite", icon = Lucide.Heart, onClick = {
                                haptic(AppHaptic.Success)
                                scope.launch {
                                    snackbarHostState.showFlashMessage(
                                        "Saved",
                                        LynkFlashType.Success
                                    )
                                }
                            }),
                            LynkDropDownItem(
                                "Delete", icon = Lucide.Trash2, isDestructive = true,
                                onClick = {
                                    haptic(AppHaptic.Error)
                                    scope.launch {
                                        snackbarHostState.showFlashMessage(
                                            "Deleted",
                                            LynkFlashType.Error
                                        )
                                    }
                                })
                        )
                    )
                }
            }
        }
    }

    // ── Overlays (outside scroll column) ─────────────────────────────────────

    LynkActionSheet(
        visible = showActionSheet,
        onDismissRequest = { showActionSheet = false },
        title = "What would you like to do?",
        items = listOf(
            LynkActionSheetItem("Edit", icon = Lucide.Search, onClick = {
                haptic(AppHaptic.Selection)
                scope.launch {
                    snackbarHostState.showFlashMessage(
                        "Edit tapped",
                        LynkFlashType.Info
                    )
                }
            }),
            LynkActionSheetItem(
                "Share",
                icon = Lucide.Share2,
                onClick = { haptic(AppHaptic.Selection) }),
            LynkActionSheetItem("Delete", icon = Lucide.Trash2, isDestructive = true, onClick = {
                haptic(AppHaptic.Error)
                scope.launch { snackbarHostState.showFlashMessage("Deleted", LynkFlashType.Error) }
            })
        )
    )

    if (showDestructiveDialog) {
        LynkDialog(
            onDismissRequest = { showDestructiveDialog = false },
            title = "Delete Account?",
            message = "This action cannot be undone. All your data will be permanently removed from Lynk.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                haptic(AppHaptic.Error)
                scope.launch {
                    snackbarHostState.showFlashMessage(
                        "Account deleted (demo)",
                        LynkFlashType.Error
                    )
                }
            },
            onDismiss = { haptic(AppHaptic.Selection) }
        )
    }

    if (showInfoDialog) {
        LynkDialog(
            onDismissRequest = { showInfoDialog = false },
            title = "Enable Notifications",
            message = "Get notified when someone likes your post, follows you, or sends a message.",
            confirmText = "Enable",
            dismissText = "Not Now",
            onConfirm = {
                haptic(AppHaptic.Success)
                scope.launch {
                    snackbarHostState.showFlashMessage(
                        "Notifications enabled!",
                        LynkFlashType.Success
                    )
                }
            }
        )
    }

    if (showBottomSheet) {
        LynkBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LynkText("LynkBottomSheet", style = LynkTheme.Typography.titleLarge)
                LynkText(
                    "Drag handle = outline token (4.49:1 light, 4.43:1 dark). Scrim = black@32%.",
                    style = LynkTheme.Typography.bodyMedium,
                    color = LynkTheme.colors.onSurfaceVariant
                )
                LynkButton(
                    "Close",
                    onClick = { showBottomSheet = false },
                    style = LynkButtonStyle.TEXT
                )
            }
        }
    }

    if (showAdaptiveSheet) {
        LynkAdaptiveSheet(onDismissRequest = { showAdaptiveSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LynkText("LynkAdaptiveSheet", style = LynkTheme.Typography.titleLarge)
                LynkText(
                    "Bottom sheet on phone · 480×540dp centered dialog on tablet.",
                    style = LynkTheme.Typography.bodyMedium,
                    color = LynkTheme.colors.onSurfaceVariant
                )
                LynkButton(
                    "Close", onClick = { showAdaptiveSheet = false },
                    style = LynkButtonStyle.PRIMARY
                )
            }
        }
    }
}