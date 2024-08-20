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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle

@Composable
fun EnergySummaryBox(
  forwardEnergy: EnergyData?,
  reversedEnergy: EnergyData?,
  modifier: Modifier = Modifier,
  labelSuffix: String? = null
) {
  Column(
    modifier = modifier
      .suplaCard(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (forwardEnergy != null && reversedEnergy != null) {
      val labelPrefix = stringResource(id = R.string.details_em_active_energy)
      Label(
        text = labelSuffix?.let { "$labelPrefix $it" } ?: labelPrefix,
        modifier = Modifier.padding(all = Distance.tiny)
      )

      Separator(style = SeparatorStyle.OUTLINE)
    }

    val radius = dimensionResource(id = R.dimen.radius_default)
    val density = LocalDensity.current
    var spacerHeight by remember { mutableStateOf<Dp?>(null) }

    Row(
      modifier = Modifier
        .onGloballyPositioned {
          if (spacerHeight == null) {
            spacerHeight = with(density) { it.size.height.toDp() }
          }
        },
      horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
      if (forwardEnergy != null && reversedEnergy != null) {
        EnergyItemBox(
          iconRes = R.drawable.ic_forward_energy,
          label = stringResource(id = R.string.details_em_forwarded_energy),
          value = forwardEnergy.energy,
          price = forwardEnergy.price,
          modifier = Modifier
            .weight(0.5f)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(bottomStart = radius))
        )
        Spacer(height = spacerHeight)
        EnergyItemBox(
          iconRes = R.drawable.ic_reversed_energy,
          label = stringResource(id = R.string.details_em_reversed_energy),
          value = reversedEnergy.energy,
          price = reversedEnergy.price,
          modifier = Modifier
            .weight(0.5f)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(bottomEnd = radius))
        )
      } else if (forwardEnergy != null) {
        val labelPrefix = stringResource(id = R.string.details_em_total_forward_active_energy)
        val separator = if (forwardEnergy.price == null) " " else "\n"
        EnergyItemSingleBox(
          iconRes = R.drawable.ic_forward_energy,
          label = labelSuffix?.let { "${labelPrefix}${separator}$it" } ?: labelPrefix,
          value = forwardEnergy.energy,
          price = forwardEnergy.price,
          modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(radius))
        )
      } else if (reversedEnergy != null) {
        val labelPrefix = stringResource(id = R.string.details_em_total_reverse_reactive_energy)
        val separator = if (reversedEnergy.price == null) " " else "\n"
        EnergyItemSingleBox(
          iconRes = R.drawable.ic_reversed_energy,
          label = labelSuffix?.let { "${labelPrefix}${separator}$it" } ?: labelPrefix,
          value = reversedEnergy.energy,
          price = reversedEnergy.price,
          modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(radius))
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnergyItemBox(iconRes: Int, label: String, value: String, price: String?, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(Distance.small),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    IconAndLabel(iconRes = iconRes, label = label)
    Text(
      text = value,
      style = MaterialTheme.typography.labelLarge
    )
    price?.let {
      Separator(style = SeparatorStyle.OUTLINE)
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Label(text = stringResource(id = R.string.details_em_cost))
        Text(
          text = it,
          style = MaterialTheme.typography.labelMedium
        )
      }
    }
  }
}

@Composable
private fun EnergyItemSingleBox(iconRes: Int, label: String, value: String, price: String?, modifier: Modifier = Modifier) {
  var spacerHeight by remember { mutableStateOf<Dp?>(null) }
  val density = LocalDensity.current
  Row(
    modifier = modifier
      .padding(Distance.small)
      .fillMaxWidth()
      .onGloballyPositioned {
        if (spacerHeight == null) {
          spacerHeight = with(density) { it.size.height.toDp() }
        }
      },
    horizontalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f)
    ) {
      IconAndLabel(iconRes = iconRes, label = label)
      EnergyValue(text = value)
    }
    price?.let {
      Spacer(height = spacerHeight)
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
      ) {
        Label(text = stringResource(id = R.string.details_em_cost))
        PriceValue(text = it)
      }
    }
  }
}

@Composable
private fun Spacer(height: Dp?) =
  Spacer(
    modifier = Modifier
      .width(1.dp)
      .height(height = height ?: 0.dp)
      .background(MaterialTheme.colorScheme.outline)
  )

@Composable
private fun IconAndLabel(iconRes: Int, label: String) =
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
    Image(drawableId = iconRes, modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small_size)))
    Label(text = label)
  }

@Composable
private fun Label(text: String, modifier: Modifier = Modifier) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = modifier
  )

@Composable
private fun PriceValue(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.labelMedium
  )

@Composable
private fun EnergyValue(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.labelLarge
  )

@Preview(showBackground = true, heightDp = 1000)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, heightDp = 1000)
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
      EnergySummaryBox(forwardEnergy = EnergyData("12,34 kWh", "15,00 PLN"), reversedEnergy = EnergyData("23,45 kWh", "25,00 PLN"))
      EnergySummaryBox(forwardEnergy = EnergyData("12,34 kWh", "15,00 PLN"), reversedEnergy = EnergyData("23,45 kWh", null))
      EnergySummaryBox(forwardEnergy = EnergyData("12,34 kWh", null), reversedEnergy = EnergyData("23,45 kWh", null))
      EnergySummaryBox(forwardEnergy = EnergyData("12,34 kWh", "15,00 PLN"), reversedEnergy = null)
      EnergySummaryBox(forwardEnergy = EnergyData("12,34 kWh", null), reversedEnergy = null)
      EnergySummaryBox(forwardEnergy = null, reversedEnergy = EnergyData("23,45 kWh", "25,00 PLN"), labelSuffix = "(test)")
      EnergySummaryBox(forwardEnergy = null, reversedEnergy = EnergyData("23,45 kWh", null), labelSuffix = "(test)")
    }
  }
}
