package org.supla.android.features.details.detailbase.electricitymeter
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.card.DoubleSummaryCard
import org.supla.android.ui.views.card.SingleSummaryCard
import org.supla.android.ui.views.card.SummaryCardData

@Composable
fun EnergySummaryBox(
  forwardEnergy: SummaryCardData?,
  reversedEnergy: SummaryCardData? = null,
  modifier: Modifier = Modifier,
  labelSuffix: String? = null,
  loading: Boolean = false
) {
  if (forwardEnergy != null && reversedEnergy != null) {
    val labelPrefix = stringResource(id = R.string.details_em_active_energy)
    DoubleSummaryCard(
      firstData = forwardEnergy,
      secondData = reversedEnergy,
      modifier = modifier,
      label = labelSuffix?.let { "$labelPrefix $it" } ?: labelPrefix,
      loading = loading
    )
  } else if (forwardEnergy != null) {
    val labelPrefix = stringResource(id = R.string.details_em_forward_active_energy)
    val separator = if (forwardEnergy.price == null) " " else "\n"
    SingleSummaryCard(
      label = labelSuffix?.let { "${labelPrefix}${separator}$it" } ?: labelPrefix,
      data = forwardEnergy,
      iconRes = R.drawable.ic_forward_energy,
      modifier = modifier,
      loading = loading
    )
  } else if (reversedEnergy != null) {
    val labelPrefix = stringResource(id = R.string.details_em_reverse_reactive_energy)
    val separator = if (reversedEnergy.price == null) " " else "\n"
    SingleSummaryCard(
      label = labelSuffix?.let { "${labelPrefix}${separator}$it" } ?: labelPrefix,
      data = reversedEnergy,
      iconRes = R.drawable.ic_reversed_energy,
      modifier = modifier,
      loading = loading
    )
  }
}

@Preview(showBackground = true, heightDp = 1100)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, heightDp = 1100)
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
      EnergySummaryBox(
        forwardEnergy = SummaryCardData("12,34 kWh", "15,00 PLN"),
        reversedEnergy = SummaryCardData("23,45 kWh", "25,00 PLN")
      )
      EnergySummaryBox(forwardEnergy = SummaryCardData("12,34 kWh", "15,00 PLN"), reversedEnergy = SummaryCardData("23,45 kWh", null))
      EnergySummaryBox(forwardEnergy = SummaryCardData("12,34 kWh", null), reversedEnergy = SummaryCardData("23,45 kWh", null))
      EnergySummaryBox(forwardEnergy = SummaryCardData("12,34 kWh", "15,00 PLN"), reversedEnergy = null)
      EnergySummaryBox(forwardEnergy = SummaryCardData("12,34 kWh", null), reversedEnergy = null)
      EnergySummaryBox(forwardEnergy = null, reversedEnergy = SummaryCardData("23,45 kWh", "25,00 PLN"), labelSuffix = "(test)")
      EnergySummaryBox(forwardEnergy = null, reversedEnergy = SummaryCardData("23,45 kWh", null), labelSuffix = "(test)")
      EnergySummaryBox(forwardEnergy = null, reversedEnergy = SummaryCardData("23,45 kWh", null), labelSuffix = "(test)", loading = true)
    }
  }
}
