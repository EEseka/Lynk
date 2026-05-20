package com.eeseka.lynk.shared.presentation.hangout.mappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CupSoda
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PartyPopper
import com.composables.icons.lucide.Sofa
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Utensils
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.active
import lynk.shared.generated.resources.chill
import lynk.shared.generated.resources.drinks
import lynk.shared.generated.resources.food
import lynk.shared.generated.resources.gaming
import lynk.shared.generated.resources.other
import lynk.shared.generated.resources.party
import org.jetbrains.compose.resources.stringResource

@Composable
fun HangoutVibe.getTitle(): String {
    return stringResource(
        when (this) {
            HangoutVibe.CHILL -> Res.string.chill
            HangoutVibe.FOOD -> Res.string.food
            HangoutVibe.DRINKS -> Res.string.drinks
            HangoutVibe.GAMING -> Res.string.gaming
            HangoutVibe.ACTIVE -> Res.string.active
            HangoutVibe.PARTY -> Res.string.party
            HangoutVibe.OTHER -> Res.string.other
        }
    )
}

fun HangoutVibe.getIcon(): ImageVector {
    return when (this) {
        HangoutVibe.CHILL -> Lucide.Sofa
        HangoutVibe.FOOD -> Lucide.Utensils
        HangoutVibe.DRINKS -> Lucide.CupSoda
        HangoutVibe.GAMING -> Lucide.Gamepad2
        HangoutVibe.ACTIVE -> Lucide.Flame
        HangoutVibe.PARTY -> Lucide.PartyPopper
        HangoutVibe.OTHER -> Lucide.Sparkles
    }
}