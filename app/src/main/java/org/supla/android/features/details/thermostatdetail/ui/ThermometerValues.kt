package org.supla.android.features.details.thermostatdetail.ui
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

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.thermostatdetail.general.MeasurementValue
import org.supla.android.images.ImageId
import org.supla.android.ui.views.Image

class ThermometersValues @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  private var temperatures: List<MeasurementValue> by mutableStateOf(emptyList())

  @Composable
  override fun Content() {
    SuplaTheme {
      ThermometersValues(temperatures = temperatures)
    }
  }
}

@Composable
fun ThermometersValues(temperatures: List<MeasurementValue>) {
  val weight = 1f / temperatures.size
  val itemWidth = (LocalConfiguration.current.screenWidthDp - 32 - 8.times(temperatures.size - 1)).div(temperatures.size)
  val arrangement = if (itemWidth < 100) 0.dp else dimensionResource(id = R.dimen.distance_tiny)
  val startPadding = if (itemWidth < 100) dimensionResource(id = R.dimen.distance_tiny) else dimensionResource(id = R.dimen.distance_small)
  val endPadding = if (itemWidth < 100) dimensionResource(id = R.dimen.distance_small) else dimensionResource(id = R.dimen.distance_default)
  Row(
    modifier = Modifier
      .background(color = MaterialTheme.colorScheme.surface)
      .padding(start = startPadding, end = endPadding),
    horizontalArrangement = Arrangement.spacedBy(arrangement)
  ) {
    temperatures.forEach {
      TemperatureAndHumidityCell(temperature = it, weight = weight, small = temperatures.size > 3, availableWidthDp = itemWidth)
    }
    if (temperatures.size == 1) {
      Box(
        modifier = Modifier
          .background(MaterialTheme.colorScheme.surface)
          .height(dimensionResource(id = R.dimen.detail_top_component))
          .padding(top = dimensionResource(id = R.dimen.distance_small))
          .weight(weight)
      )
    }
  }
}

context(RowScope)
@Composable
private fun TemperatureAndHumidityCell(temperature: MeasurementValue, weight: Float, small: Boolean, availableWidthDp: Int) =
  Row(
    modifier = Modifier
      .background(MaterialTheme.colorScheme.surface)
      .height(dimensionResource(id = R.dimen.detail_top_component))
      .padding(vertical = dimensionResource(id = R.dimen.distance_small))
      .weight(weight),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Spacer(modifier = Modifier.weight(1f))
    ThermometerIcon(
      icon = temperature.imageId,
      size = if (small) min(24.dp, availableWidthDp.div(4).dp) else min(36.dp, availableWidthDp.div(4).dp)
    )
    CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale = 1f)) {
      Text(
        text = temperature.value,
        style = if (small) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.headlineMedium
      )
    }
    Spacer(modifier = Modifier.weight(1f))
  }

@Composable
private fun ThermometerIcon(icon: ImageId, size: Dp = 48.dp) = Image(
  imageId = icon,
  contentDescription = "",
  modifier = Modifier.size(size),
  contentScale = ContentScale.Inside
)
