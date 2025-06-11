package org.supla.android.widget.extended.views.electricitymeter
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

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaGlanceTheme
import org.supla.android.features.widget.shared.GlanceTypography
import org.supla.android.widget.extended.WidgetValue

@Composable
fun ElectricityMicroView(value: WidgetValue.ElectricityMeter) =
  Row(
    modifier = GlanceModifier.fillMaxSize(),
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    verticalAlignment = Alignment.Vertical.CenterVertically
  ) {
    EnergyRow(R.drawable.ic_forward_energy, value.totalEnergy.forwarded)
  }

@Composable
private fun EnergyRow(iconRes: Int, value: String) {
  Image(
    provider = ImageProvider(iconRes),
    contentDescription = "",
    modifier = GlanceModifier.size(16.dp)
  )
  Text(
    text = value,
    style = GlanceTypography.bodySmall,
  )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 110, heightDp = 40)
@Composable
private fun Preview() {
  SuplaGlanceTheme {
    ElectricityMicroView(
      value = WidgetValue.ElectricityMeter(
        totalEnergy = WidgetValue.ElectricityMeter.Energy("280.0 kWh", "120.0 kWh"),
        phases = emptyMap()
      )
    )
  }
}
