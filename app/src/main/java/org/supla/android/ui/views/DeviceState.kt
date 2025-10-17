package org.supla.android.ui.views
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.images.ImageId
import org.supla.core.shared.infrastructure.LocalizedString

data class DeviceStateData(
  val label: LocalizedString,
  val icon: ImageId?,
  val value: LocalizedString
)

@Composable
fun DeviceState(data: DeviceStateData) {
  DeviceState(
    stateLabel = data.label(LocalContext.current),
    icon = data.icon,
    stateValue = data.value(LocalContext.current)
  )
}

@Composable
fun DeviceState(stateLabel: String, icon: ImageId?, stateValue: String) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
    modifier = Modifier.padding(top = Distance.default, bottom = Distance.default)
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Text(
      text = stateLabel.uppercase(),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    icon?.let {
      Image(
        imageId = it,
        contentDescription = null,
        modifier = Modifier.size(25.dp)
      )
    }
    Text(
      text = stateValue,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = Modifier.weight(1f))
  }
