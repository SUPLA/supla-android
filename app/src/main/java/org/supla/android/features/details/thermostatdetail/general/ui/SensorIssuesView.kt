package org.supla.android.features.details.thermostatdetail.general.ui
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.thermostatdetail.general.data.SensorIssue
import org.supla.android.images.ImageId
import org.supla.android.ui.views.Image

@Composable
fun SensorIssueView(sensorIssue: SensorIssue) {
  Row(
    modifier = Modifier
      .padding(
        start = dimensionResource(id = R.dimen.distance_default),
        top = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default)
      )
      .height(36.dp),
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny)),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val imageId = sensorIssue.imageId
    if (imageId != null) {
      Box(modifier = Modifier.size(36.dp)) {
        Image(
          imageId = imageId,
          contentDescription = null,
          modifier = Modifier
            .size(dimensionResource(id = R.dimen.icon_big_size))
            .align(Alignment.TopCenter)
        )
        Image(
          drawableId = R.drawable.ic_sensor_alert_circle,
          contentDescription = null,
          modifier = Modifier.align(Alignment.BottomStart)
        )
      }
    } else {
      Image(
        drawableId = R.drawable.ic_sensor_alert_circle,
        contentDescription = null,
        modifier = Modifier.size(size = dimensionResource(id = R.dimen.icon_small_size))
      )
    }
    Text(
      text = sensorIssue.textProvider(LocalContext.current),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onBackground
    )
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
      SensorIssueView(
        sensorIssue = SensorIssue(
          imageId = ImageId(R.drawable.fnc_hotel_card_on),
          textProvider = { "Wyłączone przez czujnik" }
        )
      )
    }
  }
}
