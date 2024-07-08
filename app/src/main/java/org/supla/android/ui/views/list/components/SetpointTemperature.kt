package org.supla.android.ui.views.list.components
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.supla.android.extensions.max

@Composable
fun SetpointTemperature(indicatorIcon: Int?, subValue: String, scale: Float) =
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
    SetpointIndicator(indicatorIcon, scale)
    SetpointText(subValue = subValue, scale = scale)
  }

@Composable
fun SetpointIndicator(indicatorIcon: Int?, scale: Float) =
  indicatorIcon?.let {
    val indicatorSize = max(12.dp, 12.dp.times(scale))
    Image(
      painter = painterResource(id = it),
      contentDescription = null,
      modifier = Modifier
        .width(indicatorSize)
        .height(indicatorSize),
      contentScale = ContentScale.Fit
    )
  }

@Composable
fun SetpointText(subValue: String, scale: Float) {
  val subValueSize = MaterialTheme.typography.bodyMedium.fontSize.let { max(it, it.times(scale)) }
  Text(
    text = subValue,
    style = MaterialTheme.typography.bodyMedium.copy(fontSize = subValueSize),
    color = MaterialTheme.colorScheme.onBackground
  )
}
