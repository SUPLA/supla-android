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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import org.supla.android.R
import org.supla.android.data.formatting.LocalDateFormatter
import org.supla.android.extensions.isNotNull
import org.supla.android.features.widget.shared.GlanceDistance
import org.supla.android.features.widget.shared.GlanceTypography
import org.supla.android.widget.extended.WidgetValue
import java.util.Date

private val FIRST_ROW_WIDTH = 50.dp

@Composable
fun ElectricityBigView(value: WidgetValue.ElectricityMeter, updateTime: Date) {
  Column(
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    modifier = GlanceModifier.fillMaxHeight()
  ) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
      if (value.phases.isNotEmpty()) {
        Spacer(modifier = GlanceModifier.width(FIRST_ROW_WIDTH))
      }
      EnergyHeader(
        iconRes = R.drawable.ic_forward_energy,
        stringRes = R.string.details_em_forwarded_energy,
        modifier = GlanceModifier.defaultWeight()
      )
      if (value.totalEnergy.reversed.isNotNull) {
        EnergyHeader(
          iconRes = R.drawable.ic_reversed_energy,
          stringRes = R.string.details_em_reversed_energy,
          modifier = GlanceModifier.defaultWeight()
        )
      }
    }
    DataRow(
      forwardEnergy = value.totalEnergy.forwarded,
      reverseEnergy = value.totalEnergy.reversed,
      textStyle = GlanceTypography.bodyLarge,
      label = if (value.phases.isEmpty()) RowLabel.Empty else RowLabel.Spacer
    )
    Spacer(modifier = GlanceModifier.height(GlanceDistance.tiny))
    value.phases.forEach {
      DataRow(
        forwardEnergy = it.value.forwarded,
        reverseEnergy = it.value.reversed,
        label = RowLabel.Text(LocalContext.current.getString(it.key.label))
      )
    }

    Spacer(modifier = GlanceModifier.defaultWeight())
    Text(
      text = LocalContext.current.getString(R.string.widget_update_time_long, LocalDateFormatter.current.getFullDateString(updateTime)),
      style = GlanceTypography.bodySmall.copy(color = GlanceTheme.colors.onSurfaceVariant),
      modifier = GlanceModifier.padding(GlanceDistance.tiny)
    )
  }
}

@Composable
private fun EnergyHeader(iconRes: Int, stringRes: Int, modifier: GlanceModifier) =
  Row(
    verticalAlignment = Alignment.Vertical.CenterVertically,
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    modifier = modifier
  ) {
    Image(
      provider = ImageProvider(iconRes),
      contentDescription = "",
      modifier = GlanceModifier.size(16.dp)
    )
    Text(
      text = LocalContext.current.getString(stringRes),
      style = GlanceTypography.bodySmall.copy(color = GlanceTheme.colors.onSurfaceVariant),
      modifier = GlanceModifier.padding(GlanceDistance.tiny)
    )
  }

private sealed interface RowLabel {
  data object Empty : RowLabel
  data object Spacer : RowLabel
  data class Text(val string: String) : RowLabel
}

@Composable
private fun DataRow(
  forwardEnergy: String,
  reverseEnergy: String?,
  label: RowLabel = RowLabel.Spacer,
  textStyle: TextStyle = GlanceTypography.bodySmall,
  padding: Dp = 4.dp
) =
  Row(modifier = GlanceModifier.fillMaxWidth().padding(vertical = padding)) {
    when (label) {
      RowLabel.Empty -> {}
      RowLabel.Spacer -> Spacer(modifier = GlanceModifier.width(FIRST_ROW_WIDTH))
      is RowLabel.Text ->
        Text(
          text = label.string,
          style = GlanceTypography.bodySmall,
          modifier = GlanceModifier.width(FIRST_ROW_WIDTH),
          maxLines = 1
        )
    }
    Text(
      text = forwardEnergy,
      style = textStyle.copy(textAlign = TextAlign.Center),
      modifier = GlanceModifier.defaultWeight(),
    )
    reverseEnergy?.let {
      Text(
        text = it,
        style = textStyle.copy(textAlign = TextAlign.Center),
        modifier = GlanceModifier.defaultWeight()
      )
    }
  }
