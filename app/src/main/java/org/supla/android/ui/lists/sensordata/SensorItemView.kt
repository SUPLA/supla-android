package org.supla.android.ui.lists.sensordata
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.data.model.lists.resource
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.list.ListItemDot
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemInfoIcon
import org.supla.android.ui.views.list.components.ListItemTitle
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId

data class SensorItemData(
  val channelId: Int,
  val profileId: Long,
  val onlineState: ListOnlineState,
  val icon: ImageId?,
  val caption: LocalizedString,
  val userCaption: String,
  val batteryIcon: IssueIcon?,
  val showChannelStateIcon: Boolean
)

@Composable
fun SensorItemView(
  sensor: SensorItemData,
  scale: Float = 1f,
  onCaptionLongPress: (SensorItemData) -> Unit = {},
  onInfoClick: (SensorItemData) -> Unit = {}
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 1.dp)
      .background(MaterialTheme.colorScheme.surface)
      .padding(start = Distance.small, end = Distance.default)
      .padding(vertical = Distance.tiny)
  ) {
    sensor.icon?.let { ListItemIcon(imageId = it, scale = scale) }
    ListItemTitle(
      text = sensor.caption(LocalContext.current),
      onItemClick = {},
      onLongClick = { onCaptionLongPress(sensor) },
      modifier = Modifier
        .padding(start = Distance.tiny)
        .weight(1f),
      scale = scale,
      maxLines = 2
    )
    sensor.batteryIcon?.let {
      Image(
        drawableId = it.resource,
        modifier = Modifier
          .padding(end = Distance.small)
          .size(dimensionResource(R.dimen.icon_default_size))
      )
    }
    if (sensor.showChannelStateIcon) {
      ListItemInfoIcon(
        onClick = { onInfoClick(sensor) },
        modifier = Modifier.padding(end = Distance.small)
      )
    }
    ListItemDot(
      onlineState = sensor.onlineState,
      withButton = false,
      paddingValues = PaddingValues(0.dp)
    )
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column {
      SensorItemView(
        sensor = SensorItemData(
          channelId = 123,
          profileId = 123L,
          onlineState = ListOnlineState.ONLINE,
          icon = ImageId(R.drawable.fnc_container_level_sensor_on),
          caption = LocalizedString.WithId(LocalizedStringId.CHANNEL_CAPTION_CONTAINER_LEVEL_SENSOR),
          userCaption = "",
          batteryIcon = IssueIcon.Battery50,
          showChannelStateIcon = true
        )
      )
      SensorItemView(
        sensor = SensorItemData(
          channelId = 123,
          profileId = 123L,
          onlineState = ListOnlineState.OFFLINE,
          icon = ImageId(R.drawable.fnc_container_level_sensor_off),
          caption = LocalizedString.WithId(LocalizedStringId.CHANNEL_CAPTION_CONTAINER_LEVEL_SENSOR),
          userCaption = "",
          batteryIcon = null,
          showChannelStateIcon = false
        )
      )
    }
  }
}
