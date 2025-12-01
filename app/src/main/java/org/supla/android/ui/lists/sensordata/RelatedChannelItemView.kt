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
import org.supla.core.shared.infrastructure.localizedString

sealed interface RelatedChannelData {
  val channelId: Int
  val profileId: Long
  val onlineState: ListOnlineState
  val icon: ImageId?
  val caption: LocalizedString
  val userCaption: String
  val batteryIcon: IssueIcon?
  val showChannelStateIcon: Boolean

  data object Invisible : RelatedChannelData {
    override val channelId: Int = 0
    override val profileId: Long = 0
    override val onlineState: ListOnlineState = ListOnlineState.UNKNOWN
    override val icon: ImageId = ImageId(R.drawable.ic_unknown_channel)
    override val caption: LocalizedString = localizedString(R.string.channel_caption_invisible)
    override val userCaption: String = ""
    override val batteryIcon: IssueIcon? = null
    override val showChannelStateIcon: Boolean = false
  }

  data class Visible(
    override val channelId: Int,
    override val profileId: Long,
    override val onlineState: ListOnlineState,
    override val icon: ImageId?,
    override val caption: LocalizedString,
    override val userCaption: String,
    override val batteryIcon: IssueIcon?,
    override val showChannelStateIcon: Boolean
  ) : RelatedChannelData
}

@Composable
fun RelatedChannelItemView(
  channel: RelatedChannelData,
  scale: Float = 1f,
  onCaptionLongPress: (RelatedChannelData) -> Unit = {},
  onInfoClick: (RelatedChannelData) -> Unit = {}
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
    channel.icon?.let { ListItemIcon(imageId = it, scale = scale) }
    ListItemTitle(
      text = channel.caption(LocalContext.current),
      onItemClick = {},
      onLongClick = {
        if (channel is RelatedChannelData.Visible) {
          onCaptionLongPress(channel)
        }
      },
      modifier = Modifier
        .padding(start = Distance.tiny)
        .weight(1f),
      scale = scale,
      maxLines = 2
    )
    channel.batteryIcon?.let {
      Image(
        drawableId = it.resource,
        modifier = Modifier
          .padding(end = Distance.small)
          .size(dimensionResource(R.dimen.icon_default_size))
      )
    }
    if (channel.showChannelStateIcon) {
      ListItemInfoIcon(
        onClick = { onInfoClick(channel) },
        modifier = Modifier.padding(end = Distance.small)
      )
    }
    ListItemDot(
      onlineState = channel.onlineState,
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
      RelatedChannelItemView(
        channel = RelatedChannelData.Visible(
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
      RelatedChannelItemView(
        channel = RelatedChannelData.Visible(
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
      RelatedChannelItemView(
        channel = RelatedChannelData.Invisible
      )
    }
  }
}
