package com.eeseka.lynk.profile.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldCheck
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedControl
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedItem
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedStyle
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSwitch
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.profile.presentation.mappers.getIcon
import com.eeseka.lynk.profile.presentation.mappers.getTitle
import com.eeseka.lynk.shared.domain.settings.AppTheme
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.about
import lynk.feature.profile.generated.resources.appearance
import lynk.feature.profile.generated.resources.delete_account
import lynk.feature.profile.generated.resources.deleting_account
import lynk.feature.profile.generated.resources.made_with_love
import lynk.feature.profile.generated.resources.preferences
import lynk.feature.profile.generated.resources.push_notifications
import lynk.feature.profile.generated.resources.push_notifications_message
import lynk.feature.profile.generated.resources.settings
import lynk.feature.profile.generated.resources.sign_out
import lynk.feature.profile.generated.resources.signing_out
import lynk.feature.profile.generated.resources.terms_and_privacy
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileSettingsSheet(
    theme: AppTheme,
    arePushNotificationsEnabled: Boolean,
    isGuest: Boolean,
    isSigningOut: Boolean,
    isDeletingAccount: Boolean,
    appVersion: String,
    onThemeSelected: (AppTheme) -> Unit,
    onPushNotificationsToggled: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        ProfileSettingsSheetContent(
            theme = theme,
            arePushNotificationsEnabled = arePushNotificationsEnabled,
            isGuest = isGuest,
            isSigningOut = isSigningOut,
            isDeletingAccount = isDeletingAccount,
            appVersion = appVersion,
            onThemeSelected = onThemeSelected,
            onPushNotificationsToggled = onPushNotificationsToggled,
            onTermsClick = onTermsClick,
            onSignOutClick = onSignOutClick,
            onDeleteAccountClick = onDeleteAccountClick
        )
    }
}

@Composable
private fun ProfileSettingsSheetContent(
    theme: AppTheme,
    arePushNotificationsEnabled: Boolean,
    isGuest: Boolean,
    isSigningOut: Boolean,
    isDeletingAccount: Boolean,
    appVersion: String,
    onThemeSelected: (AppTheme) -> Unit,
    onPushNotificationsToggled: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    val themeItems = AppTheme.entries.map {
        LynkSegmentedItem(title = it.getTitle(), icon = it.getIcon())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        LynkText(
            text = stringResource(Res.string.settings),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        ProfileSection(title = stringResource(Res.string.appearance)) {
            LynkSegmentedControl(
                items = themeItems,
                selectedIndex = AppTheme.entries.indexOf(theme),
                onItemSelected = { index ->
                    hapticFeedback(AppHaptic.Selection)
                    onThemeSelected(AppTheme.entries[index])
                },
                style = LynkSegmentedStyle.FIXED_BAR,
                contentPadding = PaddingValues(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileSection(title = stringResource(Res.string.preferences)) {
            ProfileSectionRow(
                icon = Lucide.Bell,
                title = stringResource(Res.string.push_notifications),
                subtitle = stringResource(Res.string.push_notifications_message),
                trailing = {
                    LynkSwitch(
                        checked = arePushNotificationsEnabled,
                        onCheckedChange = { isEnabled ->
                            hapticFeedback(AppHaptic.Selection)
                            onPushNotificationsToggled(isEnabled)
                        }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileSection(title = stringResource(Res.string.about)) {
            ProfileSectionRow(
                icon = Lucide.ShieldCheck,
                title = stringResource(Res.string.terms_and_privacy),
                onClick = onTermsClick
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        LynkButton(
            text = stringResource(Res.string.sign_out),
            loadingText = stringResource(Res.string.signing_out),
            onClick = {
                hapticFeedback(AppHaptic.ImpactMedium)
                onSignOutClick()
            },
            style = LynkButtonStyle.DESTRUCTIVE_SECONDARY,
            enabled = !isDeletingAccount,
            isLoading = if (isGuest) isDeletingAccount else isSigningOut,
            modifier = Modifier.fillMaxWidth()
        )

        if (!isGuest) {
            Spacer(modifier = Modifier.height(12.dp))

            LynkButton(
                text = stringResource(Res.string.delete_account),
                loadingText = stringResource(Res.string.deleting_account),
                onClick = {
                    hapticFeedback(AppHaptic.ImpactMedium)
                    onDeleteAccountClick()
                },
                style = LynkButtonStyle.DESTRUCTIVE_PRIMARY,
                enabled = !isSigningOut,
                isLoading = isDeletingAccount,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LynkText(
                text = stringResource(Res.string.made_with_love),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LynkText(
                text = appVersion,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileSettingsSheetPreview() {
    LynkTheme {
        ProfileSettingsSheetContent(
            theme = AppTheme.SYSTEM,
            arePushNotificationsEnabled = true,
            isGuest = false,
            isSigningOut = false,
            isDeletingAccount = false,
            appVersion = "Version 1.0.0",
            onThemeSelected = {},
            onPushNotificationsToggled = {},
            onTermsClick = {},
            onSignOutClick = {},
            onDeleteAccountClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileSettingsSheetGuestPreview() {
    LynkTheme {
        ProfileSettingsSheetContent(
            theme = AppTheme.SYSTEM,
            arePushNotificationsEnabled = false,
            isGuest = true,
            isSigningOut = false,
            isDeletingAccount = false,
            appVersion = "Version 1.0.0",
            onThemeSelected = {},
            onPushNotificationsToggled = {},
            onTermsClick = {},
            onSignOutClick = {},
            onDeleteAccountClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}