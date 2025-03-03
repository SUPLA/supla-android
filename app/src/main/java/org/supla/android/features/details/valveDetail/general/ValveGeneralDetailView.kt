@file:OptIn(ExperimentalFoundationApi::class)

package org.supla.android.features.details.valveDetail.general
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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.tools.BACKGROUND_COLOR
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.lists.data.error
import org.supla.android.ui.lists.sensordata.SensorItemData
import org.supla.android.ui.lists.sensordata.SensorsItemsView
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.ui.views.buttons.SwitchButtons
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.infrastructure.LocalizedString

data class ValveGeneralDetailViewState(
  val icon: ImageId? = null,
  val stateStringRes: Int? = null,
  val issues: List<ChannelIssueItem> = emptyList(),
  val sensors: List<SensorItemData> = emptyList(),
  val offline: Boolean = false,
  val scale: Float = 1f,

  val leftButtonState: SwitchButtonState? = null,
  val rightButtonState: SwitchButtonState? = null
)

@Composable
fun ValveGeneralDetailView(
  state: ValveGeneralDetailViewState,
  onInfoClick: (SensorItemData) -> Unit = {},
  onCloseClick: () -> Unit = {},
  onOpenClick: () -> Unit = {},
  onCaptionLongPress: (SensorItemData) -> Unit = {}
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .weight(1f)
        .fillMaxWidth()
    ) {
      DeviceState(
        icon = state.icon,
        stateValue = state.stateStringRes?.let { stringResource(it) },
        offline = state.offline
      )
      ChannelIssuesView(state.issues)
      SensorsItemsView(state.sensors, state.scale, onInfoClick, onCaptionLongPress)
    }

    SwitchButtons(
      leftButton = state.leftButtonState,
      rightButton = state.rightButtonState,
      disabled = state.offline,
      leftButtonClick = onCloseClick,
      rightButtonClick = onOpenClick
    )
  }
}

@Composable
private fun DeviceState(icon: ImageId?, stateValue: String?, offline: Boolean) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly,
    modifier = Modifier
      .padding(top = Distance.default)
      .fillMaxWidth()
      .height(120.dp)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = stringResource(R.string.details_timer_state_label).uppercase(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      stateValue?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }
    icon?.let {
      Image(
        imageId = it,
        contentDescription = null,
        modifier = Modifier.size(60.dp),
        tint = if (offline) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onBackground
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview() {
  SuplaTheme {
    ValveGeneralDetailView(
      ValveGeneralDetailViewState(
        icon = ImageId(R.drawable.fnc_valve_opened),
        stateStringRes = R.string.state_opened,
        issues = listOf(
          ChannelIssueItem.error(R.string.flooding_alarm_message),
          ChannelIssueItem.error(R.string.valve_warning_flooding_short),
          ChannelIssueItem.error(R.string.valve_warning_manually_closed_short),
          ChannelIssueItem.LowBattery(listOf(LocalizedString.Constant("Low battery 1"), LocalizedString.Constant("Low battery 2")))
        ),
        sensors = listOf(
          SensorItemData(
            channelId = 123,
            profileId = 1L,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_flood_sensor_on),
            caption = LocalizedString.Constant("Flood sensor"),
            userCaption = "",
            batteryIcon = IssueIcon.Battery50,
            showChannelStateIcon = true
          ),
          SensorItemData(
            channelId = 123,
            profileId = 1L,
            onlineState = ListOnlineState.OFFLINE,
            icon = ImageId(R.drawable.fnc_flood_sensor_off),
            caption = LocalizedString.Constant("Flood sensor"),
            userCaption = "",
            batteryIcon = IssueIcon.Battery25,
            showChannelStateIcon = false
          ),
          SensorItemData(
            channelId = 123,
            profileId = 1L,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_on),
            caption = LocalizedString.Constant("Flood sensor flood sensor flood sensor flood sensor"),
            userCaption = "",
            batteryIcon = IssueIcon.Battery50,
            showChannelStateIcon = true
          ),
          SensorItemData(
            channelId = 123,
            profileId = 1L,
            onlineState = ListOnlineState.OFFLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_off),
            caption = LocalizedString.Constant("Flood sensor"),
            userCaption = "",
            batteryIcon = IssueIcon.Battery25,
            showChannelStateIcon = false
          ),
          SensorItemData(
            channelId = 123,
            profileId = 1L,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_on),
            caption = LocalizedString.Constant("Flood sensor"),
            userCaption = "",
            batteryIcon = IssueIcon.Battery50,
            showChannelStateIcon = true
          ),
          SensorItemData(
            channelId = 123,
            profileId = 1L,
            onlineState = ListOnlineState.OFFLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_off),
            caption = LocalizedString.Constant("Flood sensor"),
            userCaption = "",
            batteryIcon = IssueIcon.Battery25,
            showChannelStateIcon = false
          )
        ),
        rightButtonState = SwitchButtonState(ImageId(R.drawable.fnc_valve_closed), R.string.channel_btn_close, false),
        leftButtonState = SwitchButtonState(ImageId(R.drawable.fnc_valve_opened), R.string.channel_btn_open, false)
      )
    )
  }
}
