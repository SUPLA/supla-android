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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.data.model.lists.resource
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.data.error
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.ui.views.buttons.SwitchButtons
import org.supla.android.ui.views.list.ListItemDot
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemInfoIcon
import org.supla.android.ui.views.list.components.ListItemTitle
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.infrastructure.LocalizedString

data class ValveGeneralDetailViewState(
  val icon: ImageId? = null,
  val stateStringRes: Int? = null,
  val issues: List<ChannelIssueItem> = emptyList(),
  val sensors: List<SensorData> = emptyList(),
  val offline: Boolean = false,
  val scale: Float = 1f,

  val leftButtonState: SwitchButtonState? = null,
  val rightButtonState: SwitchButtonState? = null
)

data class SensorData(
  val channelId: Int,
  val onlineState: ListOnlineState,
  val icon: ImageId?,
  val caption: LocalizedString,
  val batteryIcon: IssueIcon?,
  val showChannelStateIcon: Boolean
)

@Composable
fun ValveGeneralDetailView(
  state: ValveGeneralDetailViewState,
  onInfoClick: (SensorData) -> Unit = {},
  onCloseClick: () -> Unit = {},
  onOpenClick: () -> Unit = {}
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Distance.default),
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
      Issues(state.issues)
      Sensors(state.sensors, state.scale, onInfoClick)
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

@Composable
private fun Issues(issues: List<ChannelIssueItem>) {
  issues.forEach {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.default),
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      Image(drawableId = it.icon.resource)
      Text(it.message.invoke(LocalContext.current), textAlign = TextAlign.Justify)
    }
  }
}

@Composable
private fun Sensors(sensors: List<SensorData>, scale: Float, onInfoClick: (SensorData) -> Unit) {
  if (sensors.isNotEmpty()) {
    Column(modifier = Modifier.padding(top = Distance.small)) {
      Text(
        text = stringResource(id = R.string.valve_detail_sensors).uppercase(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(start = Distance.default, bottom = Distance.tiny, end = Distance.default)
      )
      sensors.forEach { sensor ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(bottom = 1.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = Distance.small, end = Distance.default)
            .padding(vertical = Distance.tiny)
        ) {
          sensor.icon?.let { ListItemIcon(imageId = it, scale = scale) }
          ListItemTitle(
            text = sensor.caption(LocalContext.current),
            onItemClick = {},
            onLongClick = {},
            modifier = Modifier.padding(start = Distance.tiny),
            scale = scale
          )
          Spacer(modifier = Modifier.weight(1f))
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
    }
  }
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0x00F5F6F7)
@Composable
private fun Preview() {
  SuplaTheme {
    ValveGeneralDetailView(
      ValveGeneralDetailViewState(
        icon = ImageId(R.drawable.valveopen),
        stateStringRes = R.string.state_opened,
        issues = listOf(
          ChannelIssueItem.error(R.string.flooding_alarm_message),
          ChannelIssueItem.error(R.string.valve_warning_flooding_short),
          ChannelIssueItem.error(R.string.valve_warning_manually_closed_short)
        ),
        sensors = listOf(
          SensorData(
            channelId = 123,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_flood_sensor_on),
            caption = LocalizedString.Constant("Flood sensor"),
            batteryIcon = IssueIcon.Battery50,
            showChannelStateIcon = true
          ),
          SensorData(
            channelId = 123,
            onlineState = ListOnlineState.OFFLINE,
            icon = ImageId(R.drawable.fnc_flood_sensor_off),
            caption = LocalizedString.Constant("Flood sensor"),
            batteryIcon = IssueIcon.Battery25,
            showChannelStateIcon = false
          ),
          SensorData(
            channelId = 123,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_on),
            caption = LocalizedString.Constant("Flood sensor"),
            batteryIcon = IssueIcon.Battery50,
            showChannelStateIcon = true
          ),
          SensorData(
            channelId = 123,
            onlineState = ListOnlineState.OFFLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_off),
            caption = LocalizedString.Constant("Flood sensor"),
            batteryIcon = IssueIcon.Battery25,
            showChannelStateIcon = false
          ),
          SensorData(
            channelId = 123,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_on),
            caption = LocalizedString.Constant("Flood sensor"),
            batteryIcon = IssueIcon.Battery50,
            showChannelStateIcon = true
          ),
          SensorData(
            channelId = 123,
            onlineState = ListOnlineState.OFFLINE,
            icon = ImageId(R.drawable.fnc_container_level_sensor_off),
            caption = LocalizedString.Constant("Flood sensor"),
            batteryIcon = IssueIcon.Battery25,
            showChannelStateIcon = false
          )
        ),
        rightButtonState = SwitchButtonState(ImageId(R.drawable.valveclosed), R.string.channel_btn_close, false),
        leftButtonState = SwitchButtonState(ImageId(R.drawable.valveopen), R.string.channel_btn_open, false)
      )
    )
  }
}
