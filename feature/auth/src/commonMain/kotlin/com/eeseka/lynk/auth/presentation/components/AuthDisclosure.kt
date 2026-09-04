package com.eeseka.lynk.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.auth.generated.resources.Res
import lynk.feature.auth.generated.resources.auth_disclosure_conjunction
import lynk.feature.auth.generated.resources.auth_disclosure_prefix
import lynk.feature.auth.generated.resources.privacy_policy
import lynk.feature.auth.generated.resources.terms_of_service
import org.jetbrains.compose.resources.stringResource

private const val TERMS_URL = "https://example.com/terms"
private const val PRIVACY_URL = "https://example.com/privacy"

@Composable
fun AuthDisclosure(modifier: Modifier = Modifier) {
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )

    LynkText(
        text = buildAnnotatedString {
            append(stringResource(Res.string.auth_disclosure_prefix))

            withLink(LinkAnnotation.Url(url = TERMS_URL, styles = linkStyles)) {
                append(stringResource(Res.string.terms_of_service))
            }

            append(stringResource(Res.string.auth_disclosure_conjunction))

            withLink(LinkAnnotation.Url(url = PRIVACY_URL, styles = linkStyles)) {
                append(stringResource(Res.string.privacy_policy))
            }
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun AuthDisclosurePreview() {
    LynkTheme {
        AuthDisclosure(modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}
