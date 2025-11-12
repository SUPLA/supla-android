package org.supla.android.features.details.gatedetail.general
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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.channelissues.ChannelIssueView
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.lists.sensordata.RelatedChannelData
import org.supla.android.ui.lists.sensordata.RelatedChannelsView
import org.supla.android.ui.views.DeviceState
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButton
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.ui.views.buttons.SwitchButtons
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

data class GateGeneralViewState(
  val deviceStateData: DeviceStateData? = null,
  val relatedChannelsData: List<RelatedChannelData>? = null,
  val channelIssues: List<ChannelIssueItem>? = null,
  val mainButtonLabel: LocalizedString = localizedString(R.string.channel_btn_step_by_step),
  val closeButtonState: SwitchButtonState? = null,
  val openButtonState: SwitchButtonState? = null,
  val showOpenAndCloseWarning: Boolean = false,
  val offline: Boolean = false,
  val scale: Float = 1f
)

interface GateGeneralScope {
  fun onOpenClose()
  fun onOpen()
  fun onClose()
}

@Composable
fun GateGeneralScope.View(
  state: GateGeneralViewState,
  onInfoClick: (RelatedChannelData) -> Unit = {},
  onCaptionLongPress: (RelatedChannelData) -> Unit = {}
) {
  Column {
    state.deviceStateData?.let {
      DeviceState(data = it)
      state.channelIssues?.let { issues -> ChannelIssuesView(issues) }
      Spacer(modifier = Modifier.weight(1f))
    }

    state.relatedChannelsData?.let { relatedChannels ->
      Text(
        text = stringResource(R.string.widget_group).uppercase(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(start = Distance.default, top = Distance.default, end = Distance.default)
      )
      RelatedChannelsView(
        channels = relatedChannels,
        onInfoClick = { onInfoClick(it) },
        onCaptionLongPress = { onCaptionLongPress(it) },
        modifier = Modifier.weight(1f),
        scale = state.scale
      )
    }

    if (state.showOpenAndCloseWarning) {
      ChannelIssueView(
        iconId = R.drawable.channel_warning_level1,
        message = stringResource(R.string.gate_general_open_and_close_warning),
        modifier = Modifier.padding(all = Distance.default),
      )
    }
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
      PortraitButtons(state)
    } else {
      LandscapeButtons(state)
    }
  }
}

@Composable
private fun GateGeneralScope.PortraitButtons(state: GateGeneralViewState) {
  if (state.openButtonState != null && state.closeButtonState != null) {
    SwitchButtons(
      leftButton = state.closeButtonState,
      rightButton = state.openButtonState,
      disabled = state.offline,
      leftButtonClick = { onClose() },
      rightButtonClick = { onOpen() },
      leftColors = SuplaButtonDefaults.primaryColors(contentDisabled = MaterialTheme.colorScheme.onSurface)
    )
  }

  SuplaButton(
    text = state.mainButtonLabel(LocalContext.current),
    onClick = { onOpenClose() },
    disabled = state.offline,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = Distance.default, end = Distance.default, bottom = Distance.default)
  )
}

@Composable
private fun GateGeneralScope.LandscapeButtons(state: GateGeneralViewState) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.default),
    modifier = Modifier
      .padding(horizontal = Distance.default)
      .padding(bottom = Distance.small, top = Distance.small)
  ) {
    state.closeButtonState?.let {
      SwitchButton(
        icon = it.icon,
        text = stringResource(id = it.textRes),
        colors = SuplaButtonDefaults.primaryColors(contentDisabled = MaterialTheme.colorScheme.onSurface),
        disabled = state.offline,
        pressed = it.pressed,
        onClick = { onClose() },
        modifier = Modifier.weight(1f)
      )
    } ?: Spacer(Modifier.weight(1f))
    SuplaButton(
      text = stringResource(R.string.channel_btn_step_by_step),
      onClick = { onOpenClose() },
      disabled = state.offline
    )
    state.openButtonState?.let {
      SwitchButton(
        icon = it.icon,
        text = stringResource(id = it.textRes),
        colors = SuplaButtonDefaults.primaryColors(contentDisabled = MaterialTheme.colorScheme.onSurface),
        disabled = state.offline,
        pressed = it.pressed,
        onClick = { onClose() },
        modifier = Modifier.weight(1f)
      )
    } ?: Spacer(Modifier.weight(1f))
  }
}

private val previewScope = object : GateGeneralScope {
  override fun onOpenClose() {}
  override fun onOpen() {}
  override fun onClose() {}
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      state = GateGeneralViewState(
        deviceStateData = DeviceStateData(
          label = localizedString(R.string.details_timer_state_label),
          icon = ImageId(id = R.drawable.fnc_garage_door_closed),
          value = localizedString(R.string.state_closed)
        ),
        closeButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_garage_door_closed),
          textRes = R.string.channel_btn_close,
          pressed = false
        ),
        openButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_garage_door_opened),
          textRes = R.string.channel_btn_open,
          pressed = false
        )
      )
    )
  }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewGroup() {
  SuplaTheme {
    previewScope.View(
      state = GateGeneralViewState(
        relatedChannelsData = listOf(
          RelatedChannelData(
            channelId = 1,
            profileId = 1L,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_garage_door_closed),
            caption = LocalizedString.Constant("Garage Door"),
            userCaption = "",
            batteryIcon = null,
            showChannelStateIcon = true
          )
        )
      )
    )
  }
}
