package com.eeseka.lynk.shared.presentation.spot.util

import androidx.compose.runtime.Composable
import com.eeseka.lynk.shared.domain.spot.model.SpotPriceRange
import com.eeseka.lynk.shared.presentation.util.toCurrencyString
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.spot_price_from
import lynk.shared.generated.resources.spot_price_range
import lynk.shared.generated.resources.spot_price_up_to
import org.jetbrains.compose.resources.stringResource

// "₦10,000 to ₦70,000", "₦10,000 and up" or "Up to ₦70,000"
@Composable
fun SpotPriceRange.toPriceRangeLabel(): String? {
    val start = startAmount?.toCurrencyString(currencyCode)
    val end = endAmount?.toCurrencyString(currencyCode)

    return when {
        start != null && end != null -> stringResource(Res.string.spot_price_range, start, end)
        start != null -> stringResource(Res.string.spot_price_from, start)
        end != null -> stringResource(Res.string.spot_price_up_to, end)
        else -> null
    }
}
