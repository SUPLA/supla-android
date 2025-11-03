package org.supla.android.features.details.switchdetail.general
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterMetricsView
import org.supla.android.features.details.detailbase.impulsecounter.ImpulseCounterMetricsView
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.lists.sensordata.RelatedChannelData
import org.supla.android.ui.lists.sensordata.RelatedChannelsView
import org.supla.android.ui.views.DeviceState
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.ui.views.buttons.SwitchButtons
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

interface SwitchGeneralScope {
  fun onTurnOn()
  fun onTurnOff()
  fun onIntroductionClose()
}

@Composable
fun SwitchGeneralScope.View(
  state: SwitchGeneralViewState,
  onInfoClick: (RelatedChannelData) -> Unit = {},
  onCaptionLongPress: (RelatedChannelData) -> Unit = {}
) {
  Column {
    if (state.electricityMeterState != null) {
      state.channelIssues?.let { ChannelIssuesView(it, modifier = Modifier.padding(top = Distance.default)) }
      Box(modifier = Modifier.weight(1f)) {
        ElectricityMeterMetricsView(
          state = state.electricityMeterState,
          onIntroductionClose = { onIntroductionClose() }
        )
        Shadow(orientation = ShadowOrientation.STARTING_BOTTOM, modifier = Modifier.align(Alignment.BottomCenter))
      }
    } else if (state.impulseCounterState != null) {
      state.channelIssues?.let { ChannelIssuesView(it, modifier = Modifier.padding(top = Distance.default)) }
      Box(modifier = Modifier.weight(1f)) {
        ImpulseCounterMetricsView(state = state.impulseCounterState)
        Shadow(orientation = ShadowOrientation.STARTING_BOTTOM, modifier = Modifier.align(Alignment.BottomCenter))
      }
    } else if (state.relatedChannelsData != null) {
      Text(
        text = stringResource(R.string.widget_group).uppercase(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(start = Distance.default, top = Distance.default, end = Distance.default)
      )
      RelatedChannelsView(
        channels = state.relatedChannelsData,
        onInfoClick = onInfoClick,
        onCaptionLongPress = onCaptionLongPress,
        modifier = Modifier.weight(1f),
        scale = state.scale
      )
      state.channelIssues?.let { ChannelIssuesView(it, modifier = Modifier.padding(top = Distance.default)) }
    } else if (state.deviceStateData != null) {
      DeviceState(data = state.deviceStateData)
      state.channelIssues?.let { ChannelIssuesView(it) }
      Spacer(modifier = Modifier.weight(1f))
    } else {
      state.channelIssues?.let { ChannelIssuesView(it) }
      Spacer(modifier = Modifier.weight(1f))
    }

    if (state.leftButtonState != null || state.rightButtonState != null) {
      SwitchButtons(
        leftButton = state.leftButtonState,
        rightButton = state.rightButtonState,
        disabled = state.online == false,
        leftButtonClick = { onTurnOff() },
        rightButtonClick = { onTurnOn() }
      )
    }
  }
}

private val previewScope = object : SwitchGeneralScope {
  override fun onTurnOn() {}
  override fun onTurnOff() {}
  override fun onIntroductionClose() {}
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      state = SwitchGeneralViewState(
        deviceStateData = DeviceStateData(
          label = localizedString(R.string.details_timer_state_label),
          icon = ImageId(R.drawable.fnc_switch_on),
          value = LocalizedString.Empty
        ),
        leftButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_switch_off),
          textRes = R.string.channel_btn_off
        ),
        rightButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_switch_on),
          textRes = R.string.channel_btn_on
        )
      )
    )
  }
}

@Preview(name = "Disabled", showBackground = true)
@Preview(name = "Disabled", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_Disabled() {
  SuplaTheme {
    previewScope.View(
      state = SwitchGeneralViewState(
        online = false,
        deviceStateData = DeviceStateData(
          label = localizedString(R.string.details_timer_state_label),
          icon = ImageId(R.drawable.fnc_switch_on),
          value = LocalizedString.Empty
        ),
        leftButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_switch_off),
          textRes = R.string.channel_btn_off
        ),
        rightButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_switch_on),
          textRes = R.string.channel_btn_on
        )
      )
    )
  }
}
