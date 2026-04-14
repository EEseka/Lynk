package com.eeseka.lynk.profile_setup.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.AtSign
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import lynk.feature.profile_setup.generated.resources.Res
import lynk.feature.profile_setup.generated.resources.available
import lynk.feature.profile_setup.generated.resources.display_name
import lynk.feature.profile_setup.generated.resources.display_name_placeholder
import lynk.feature.profile_setup.generated.resources.email
import lynk.feature.profile_setup.generated.resources.unavailable
import lynk.feature.profile_setup.generated.resources.username
import lynk.feature.profile_setup.generated.resources.username_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileFormSection(
    email: String,
    displayNameState: TextFieldState,
    usernameState: TextFieldState,
    displayNameErrorMessage: String?,
    usernameErrorMessage: String?,
    isCheckingUsername: Boolean,
    isUsernameAvailable: Boolean?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Email Banner
        LynkCard(
            style = LynkCardStyle.FILLED,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.Mail,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    LynkText(
                        text = stringResource(Res.string.email),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LynkText(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display Name Field
        LynkTextField(
            state = displayNameState,
            label = stringResource(Res.string.display_name),
            placeholder = stringResource(Res.string.display_name_placeholder),
            isError = displayNameErrorMessage != null,
            errorMessage = displayNameErrorMessage,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username Field
        LynkTextField(
            state = usernameState,
            label = stringResource(Res.string.username),
            placeholder = stringResource(Res.string.username_placeholder),
            isError = usernameErrorMessage != null,
            errorMessage = usernameErrorMessage,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Icon(
                    imageVector = Lucide.AtSign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (isCheckingUsername) {
                    LynkProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (isUsernameAvailable == true) {
                    Icon(
                        imageVector = Lucide.CircleCheck,
                        contentDescription = stringResource(Res.string.available),
                        tint = MaterialTheme.colorScheme.extended.success
                    )
                } else if (isUsernameAvailable == false || usernameErrorMessage != null) {
                    Icon(
                        imageVector = Lucide.CircleAlert,
                        contentDescription = stringResource(Res.string.unavailable),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileFormSectionPreview() {
    LynkTheme {
        ProfileFormSection(
            email = "john.doe@example.com",
            displayNameState = TextFieldState("John Doe"),
            displayNameErrorMessage = null,
            usernameState = TextFieldState("john_doe"),
            usernameErrorMessage = null,
            isCheckingUsername = false,
            isUsernameAvailable = true,
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileFormSectionPreviewError() {
    LynkTheme {
        ProfileFormSection(
            email = "john.doe@example.com",
            displayNameState = TextFieldState("John Doe"),
            displayNameErrorMessage = "Too Long",
            usernameState = TextFieldState("john_doe"),
            usernameErrorMessage = "Username Taken",
            isCheckingUsername = false,
            isUsernameAvailable = false,
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)
        )
    }
}