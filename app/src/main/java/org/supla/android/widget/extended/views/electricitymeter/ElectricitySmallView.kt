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
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import org.supla.android.R
import org.supla.android.data.formatting.LocalDateFormatter
import org.supla.android.features.widget.shared.GlanceDistance
import org.supla.android.features.widget.shared.GlanceTypography
import org.supla.android.widget.extended.WidgetValue
import java.util.Date

@Composable
fun ElectricitySmallView(value: WidgetValue.ElectricityMeter, updateTime: Date) {
  Column(
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    modifier = GlanceModifier.fillMaxHeight()
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth().padding(top = GlanceDistance.tiny),
      horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
      EnergyRow(R.drawable.ic_forward_energy, value.totalEnergy.forwarded)
    }
    value.totalEnergy.reversed?.let {
      Row(
        modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
      ) {
        EnergyRow(R.drawable.ic_reversed_energy, it)
      }
    }

    Spacer(modifier = GlanceModifier.defaultWeight())
    LocalDateFormatter.current.getHourString(updateTime)?.let {
      Text(
        text = it,
        style = GlanceTypography.bodySmall.copy(color = GlanceTheme.colors.onSurfaceVariant),
        modifier = GlanceModifier.padding(4.dp),
        maxLines = 1
      )
    }
  }
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
