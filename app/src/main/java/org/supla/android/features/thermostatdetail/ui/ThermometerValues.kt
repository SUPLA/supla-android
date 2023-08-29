package org.supla.android.features.thermostatdetail.ui
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.thermostatdetail.thermostatgeneral.ThermostatTemperature

class ThermometersValues @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  private var temperatures: List<ThermostatTemperature> by mutableStateOf(emptyList())

  @Composable
  override fun Content() {
    SuplaTheme {
      ThermometersValues(temperatures = temperatures)
    }
  }
}

@Composable
fun ThermometersValues(temperatures: List<ThermostatTemperature>) {
  val weight = 1f / temperatures.size
  Column(modifier = Modifier.background(color = MaterialTheme.colors.background)) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
      temperatures.forEach { ThermometerCell(temperature = it, weight = weight) }
      if (temperatures.size == 1) {
        Box(
          modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .height(80.dp)
            .padding(top = dimensionResource(id = R.dimen.distance_small))
            .weight(weight)
        )
      }
    }
  }
}

context(RowScope)
@Composable
private fun ThermometerCell(temperature: ThermostatTemperature, weight: Float) =
  Row(
    modifier = Modifier
      .background(MaterialTheme.colors.surface)
      .padding(vertical = dimensionResource(id = R.dimen.distance_small))
      .weight(weight),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Spacer(modifier = Modifier.weight(1f))
    ThermometerIcon(icon = temperature.iconProvider(LocalContext.current).asImageBitmap())
    Text(text = temperature.temperature, style = MaterialTheme.typography.h5)
    Spacer(modifier = Modifier.weight(1f))
  }

@Composable
private fun ThermometerIcon(icon: ImageBitmap) = Image(
  bitmap = icon,
  contentDescription = "",
  modifier = Modifier
    .width(48.dp)
    .height(48.dp),
  contentScale = ContentScale.Inside
)
