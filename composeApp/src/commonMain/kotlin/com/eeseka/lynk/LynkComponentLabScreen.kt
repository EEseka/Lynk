package com.eeseka.lynk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Search
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.buttons.LynkFloatingActionButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheetItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkBottomSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkBottomBar
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSwitch
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.navigation.LynkNavigationItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LynkComponentLabScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = rememberAppHaptic()

    var showActionSheet by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedNavItem by remember { mutableStateOf(LynkNavigationItem.DISCOVER) }

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            LynkTopAppBar(
                title = "Component Lab",
                navigationIcon = {
                    LynkIconButton(onClick = { haptic(AppHaptic.ImpactLight) }) {
                        Icon(
                            imageVector = Lucide.Search,
                            contentDescription = null,
                            tint = LynkTheme.colors.textMain
                        )
                    }
                },
                actions = {
                    LynkIconButton(onClick = { haptic(AppHaptic.ImpactLight) }) {
                        Icon(
                            imageVector = Lucide.Plus,
                            contentDescription = null,
                            tint = LynkTheme.colors.textMain
                        )
                    }
                }
            )
        },
        bottomBar = {
            LynkBottomBar(
                selectedItem = selectedNavItem,
                onItemSelected = {
                    selectedNavItem = it
                    haptic(AppHaptic.Selection)
                }
            )
        },
        floatingActionButton = {
            LynkFloatingActionButton(
                onClick = {
                    haptic(AppHaptic.ImpactHeavy)
                    scope.launch {
                        snackbarHostState.showFlashMessage(
                            "FAB Clicked!",
                            type = LynkFlashType.Info
                        )
                    }
                }
            ) {
                Icon(imageVector = Lucide.Heart, contentDescription = null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))

            LynkText(
                text = "Lynk Design System Lab",
                style = LynkTheme.LynkTypography.displayLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Section: Color Palette (Secondary & Tertiary)
            ComponentSection(title = "Brand Palette") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ColorRow("Primary", LynkTheme.colors.primary, LynkTheme.colors.primaryContainer)
                    ColorRow(
                        "Secondary",
                        LynkTheme.colors.secondary,
                        LynkTheme.colors.secondaryContainer
                    )
                    ColorRow(
                        "Tertiary",
                        LynkTheme.colors.tertiary,
                        LynkTheme.colors.tertiaryContainer
                    )
                }
            }

            // Section: Typography
            ComponentSection(title = "Typography Stack") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LynkText("Display Large", style = LynkTheme.LynkTypography.displayLarge)
                    LynkText("Headline Medium", style = LynkTheme.LynkTypography.headlineMedium)
                    LynkText("Title Large", style = LynkTheme.LynkTypography.titleLarge)
                    LynkText(
                        "Body Large (Plus Jakarta Sans)",
                        style = LynkTheme.LynkTypography.bodyLarge
                    )
                    LynkText(
                        "Label Small",
                        style = LynkTheme.LynkTypography.labelSmall,
                        color = LynkTheme.colors.textMuted
                    )
                }
            }

            // Section: Buttons
            ComponentSection(title = "Buttons & Haptics") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LynkButton(
                        "Primary (Impact Medium)",
                        onClick = { haptic(AppHaptic.ImpactMedium) })
                    LynkButton(
                        "Secondary Outline",
                        style = LynkButtonStyle.SECONDARY,
                        onClick = { haptic(AppHaptic.ImpactLight) })
                    LynkButton(
                        "Destructive Primary (Error)",
                        style = LynkButtonStyle.DESTRUCTIVE_PRIMARY,
                        onClick = { haptic(AppHaptic.Error) })
                    LynkButton(
                        "Destructive Secondary",
                        style = LynkButtonStyle.DESTRUCTIVE_SECONDARY,
                        onClick = { haptic(AppHaptic.ImpactLight) })
                    LynkButton(
                        "Text Button",
                        style = LynkButtonStyle.TEXT,
                        onClick = { haptic(AppHaptic.Selection) })
                }
            }

            // Section: Forms & Toggles
            ComponentSection(title = "Forms & Controls") {
                var text by remember { mutableStateOf("") }
                var checked by remember { mutableStateOf(true) }

                LynkTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "Full Name",
                    placeholder = "Enter your name"
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText("Enable Notifications", style = LynkTheme.LynkTypography.bodyLarge)
                    LynkSwitch(
                        checked = checked,
                        onCheckedChange = {
                            checked = it
                            haptic(AppHaptic.Selection)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LynkProgressIndicator(modifier = Modifier.size(24.dp))
                    LynkText(
                        "Processing...",
                        style = LynkTheme.LynkTypography.bodyMedium,
                        color = LynkTheme.colors.textMuted
                    )
                }
            }

            // Section: Cards & Surfaces
            ComponentSection(title = "Cards & Surfaces") {
                LynkCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LynkText(
                            "Secondary Surface",
                            style = LynkTheme.LynkTypography.titleMedium,
                            color = LynkTheme.colors.secondary
                        )
                        LynkText(
                            "This is a card demonstrating the secondary brand color for accents.",
                            style = LynkTheme.LynkTypography.bodyMedium,
                            color = LynkTheme.colors.textMuted
                        )
                    }
                }
            }

            // Section: Overlays
            ComponentSection(title = "Modals & Overlays") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LynkButton(
                            "Action Sheet",
                            onClick = {
                                haptic(AppHaptic.ImpactMedium)
                                showActionSheet = true
                            },
                            modifier = Modifier.weight(1f),
                            style = LynkButtonStyle.SECONDARY
                        )
                        LynkButton(
                            "Dialog",
                            onClick = {
                                haptic(AppHaptic.ImpactMedium)
                                showDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            style = LynkButtonStyle.SECONDARY
                        )
                    }

                    LynkButton(
                        "Open Bottom Sheet",
                        onClick = {
                            haptic(AppHaptic.ImpactHeavy)
                            showBottomSheet = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        style = LynkButtonStyle.SECONDARY
                    )

                    LynkButton(
                        "Trigger Success Flash",
                        onClick = {
                            haptic(AppHaptic.Success)
                            scope.launch {
                                snackbarHostState.showFlashMessage(
                                    "Task completed successfully!",
                                    type = LynkFlashType.Success
                                )
                            }
                        },
                        style = LynkButtonStyle.TEXT
                    )
                }
            }

            Spacer(modifier = Modifier.height(padding.calculateBottomPadding() + 80.dp))
        }
    }

    // Overlays
    LynkActionSheet(
        visible = showActionSheet,
        onDismissRequest = { showActionSheet = false },
        title = "Choose an option",
        items = listOf(
            LynkActionSheetItem("Edit Profile", onClick = { haptic(AppHaptic.Selection) }),
            LynkActionSheetItem(
                "Sign Out",
                onClick = { haptic(AppHaptic.Warning) },
                isDestructive = true
            )
        )
    )

    if (showDialog) {
        LynkDialog(
            onDismissRequest = { showDialog = false },
            title = "Delete Account?",
            message = "This action cannot be undone. All your data will be permanently removed.",
            confirmText = "Delete",
            onConfirm = {
                haptic(AppHaptic.Error)
            },
            isDestructive = true,
            dismissText = "Cancel"
        )
    }

    if (showBottomSheet) {
        LynkBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LynkText("Bottom Sheet Content", style = LynkTheme.LynkTypography.titleLarge)
                LynkText(
                    "This is a LynkBottomSheet demonstrating tertiary accents in dark mode.",
                    style = LynkTheme.LynkTypography.bodyLarge,
                    color = LynkTheme.colors.textMuted
                )
                LynkButton(
                    "Tertiary Action",
                    onClick = {
                        haptic(AppHaptic.Success)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ColorRow(
    label: String,
    mainColor: Color,
    containerColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(mainColor)
        )
        Box(
            modifier = Modifier
                .height(40.dp)
                .weight(1f)
                .clip(LynkTheme.shapes.medium)
                .background(containerColor)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            LynkText(label, style = LynkTheme.LynkTypography.labelLarge, color = Color.White)
        }
    }
}

@Composable
private fun ComponentSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        LynkText(
            text = title.uppercase(),
            style = LynkTheme.LynkTypography.labelSmall,
            color = LynkTheme.colors.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = LynkTheme.colors.outlineVariant, thickness = 0.5.dp)
    }
}