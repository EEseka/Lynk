package com.eeseka.lynk.discover.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import kotlin.math.roundToInt

@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier
) {
    val fullStars = rating.toInt()
    val hasHalfStar = rating - fullStars >= 0.5
    val emptyStars = 5 - fullStars - (if (hasHalfStar) 1 else 0)

    val displayRating = ((rating * 10.0).roundToInt() / 10.0).toString()

    val starTint = Color(0xFFFFB800)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = starTint
            )
        }

        if (hasHalfStar) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.StarHalf,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = starTint
            )
        }

        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        LynkText(
            text = displayRating,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@PreviewLightDark
@Composable
private fun RatingStarsPreview() {
    LynkTheme {
        RatingStars(3.543, modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}