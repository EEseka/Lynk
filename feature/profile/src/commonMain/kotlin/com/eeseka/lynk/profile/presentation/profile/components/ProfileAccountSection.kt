package com.eeseka.lynk.profile.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.AtSign
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.account
import lynk.feature.profile.generated.resources.email
import lynk.feature.profile.generated.resources.saved_spots
import lynk.feature.profile.generated.resources.username
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileAccountSection(
    email: String,
    username: String,
    onSavedSpotsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileSection(
        title = stringResource(Res.string.account),
        modifier = modifier
    ) {
        ProfileSectionRow(
            icon = Lucide.AtSign,
            title = stringResource(Res.string.username),
            subtitle = "@$username"
        )

        ProfileSectionDivider()

        ProfileSectionRow(
            icon = Lucide.Mail,
            title = stringResource(Res.string.email),
            subtitle = email
        )

        ProfileSectionDivider()

        ProfileSectionRow(
            icon = Lucide.Bookmark,
            title = stringResource(Res.string.saved_spots),
            onClick = onSavedSpotsClick
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileAccountSectionPreview() {
    LynkTheme {
        ProfileAccountSection(
            email = "john.doe@example.com",
            username = "johndoe",
            onSavedSpotsClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}