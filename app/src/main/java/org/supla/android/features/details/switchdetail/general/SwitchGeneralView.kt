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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterMetricsView
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterState
import org.supla.android.features.details.detailbase.electricitymeter.PhaseWithMeasurements
import org.supla.android.features.details.detailbase.impulsecounter.ImpulseCounterMetricsView
import org.supla.android.images.ImageId
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.lists.sensordata.RelatedChannelData
import org.supla.android.ui.lists.sensordata.RelatedChannelsView
import org.supla.android.ui.views.DeviceState
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButton
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.ui.views.buttons.SwitchButtonsLayout
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.android.ui.views.card.SummaryCardData
import org.supla.android.ui.views.icons.LockIcon
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

interface SwitchGeneralScope {
  fun onTurnOn()
  fun onTurnOff()
  fun onIntroductionClose()

  fun onForce()
  fun onManual()
  fun onWeekly()
  fun onAuto()
}

@Composable
fun SwitchGeneralScope.View(
  state: SwitchGeneralViewState,
  onInfoClick: (RelatedChannelData) -> Unit = {},
  onCaptionLongPress: (RelatedChannelData) -> Unit = {}
) {
  Box {
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
        WithRelatedChannels(
          relatedChannelsData = state.relatedChannelsData,
          channelIssues = state.channelIssues,
          scale = state.scale,
          onInfoClick = onInfoClick,
          onCaptionLongPress = onCaptionLongPress
        )
      } else if (state.deviceStateData != null) {
        DeviceState(data = state.deviceStateData, modifier = Modifier.padding(vertical = Distance.vertical))
        state.channelIssues?.let { ChannelIssuesView(it) }
        Spacer(modifier = Modifier.weight(1f))
      } else {
        state.channelIssues?.let { ChannelIssuesView(it) }
        Spacer(modifier = Modifier.weight(1f))
      }

      state.operatingMode?.let { OperatingButtons(it) }
      ControlButtons(state)
    }
  }
}

@Composable
private fun ColumnScope.WithRelatedChannels(
  relatedChannelsData: List<RelatedChannelData>,
  channelIssues: List<ChannelIssueItem>?,
  scale: Float,
  onInfoClick: (RelatedChannelData) -> Unit = {},
  onCaptionLongPress: (RelatedChannelData) -> Unit = {}
) {
  Text(
    text = stringResource(R.string.widget_group).uppercase(),
    style = MaterialTheme.typography.bodyMedium,
    modifier = Modifier.padding(start = Distance.default, top = Distance.default, end = Distance.default)
  )
  RelatedChannelsView(
    channels = relatedChannelsData,
    onInfoClick = onInfoClick,
    onCaptionLongPress = onCaptionLongPress,
    modifier = Modifier.weight(1f),
    scale = scale
  )
  channelIssues?.let { ChannelIssuesView(it, modifier = Modifier.padding(top = Distance.default)) }
}

@Composable
private fun SwitchGeneralScope.OperatingButtons(operatingMode: OperatingMode) {
  SwitchButtonsLayout(
    modifier = Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
  ) {
    operatingMode.manualAllowed.ifTrue {
      SwitchButton(
        icon = null,
        text = stringResource(R.string.thermostat_detail_mode_manual),
        colors = SuplaButtonDefaults.primaryColors(),
        pressed = operatingMode.manualActive,
        onClick = { onManual() },
        modifier = Modifier.widthIn(max = 120.dp)
      )
    }

    operatingMode.weeklyAllowed.ifTrue {
      SwitchButton(
        icon = null,
        text = stringResource(R.string.thermostat_detail_mode_weekly_schedule),
        colors = SuplaButtonDefaults.primaryColors(),
        pressed = operatingMode.weeklyActive,
        onClick = { onWeekly() },
        modifier = Modifier.widthIn(max = 120.dp)
      )
    }

    operatingMode.autoAllowed.ifTrue {
      SwitchButton(
        icon = null,
        text = stringResource(R.string.auto),
        colors = SuplaButtonDefaults.primaryColors(),
        pressed = operatingMode.autoActive,
        onClick = { onAuto() },
        modifier = Modifier.widthIn(max = 120.dp)
      )
    }
  }
}

@Composable
private fun SwitchGeneralScope.ControlButtons(state: SwitchGeneralViewState) {
  SwitchButtonsLayout(
    modifier = Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
  ) {
    state.leftButtonState?.let {
      SwitchButton(
        icon = it.icon,
        text = stringResource(id = it.textRes),
        colors = SuplaButtonDefaults.errorColors(contentDisabled = MaterialTheme.colorScheme.onSurface),
        disabled = state.leftButtonDisabled,
        pressed = it.pressed,
        onClick = { onTurnOff() },
        modifier = Modifier.widthIn(max = 120.dp)
      )
    }

    state.forceSupported.ifTrue {
      LockButton(
        pressed = state.forceActive,
        onClick = { onForce() }
      )
    }

    state.rightButtonState?.let {
      SwitchButton(
        icon = it.icon,
        text = stringResource(id = it.textRes),
        colors = SuplaButtonDefaults.primaryColors(contentDisabled = MaterialTheme.colorScheme.onSurface),
        disabled = state.rightButtonDisabled,
        pressed = it.pressed,
        onClick = { onTurnOn() },
        modifier = Modifier.widthIn(max = 120.dp)
      )
    }
  }
}

@Composable
fun LockButton(
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  pressed: Boolean = false,
  onClick: () -> Unit
) {
  val colorDisabled = MaterialTheme.colorScheme.outline
  SuplaButton(
    onClick = onClick,
    modifier = modifier,
    disabled = disabled,
    active = pressed,
    colors = SuplaButtonDefaults.errorColors(),
    shape = SuplaButtonDefaults.allRoundedShape()
  ) { color ->
    LockIcon(
      color = disabled.forTrue { colorDisabled } ?: color,
      modifier = Modifier.align(Alignment.Center)
    )
  }
}

private val previewScope = object : SwitchGeneralScope {
  override fun onTurnOn() {}
  override fun onTurnOff() {}
  override fun onIntroductionClose() {}
  override fun onForce() {}
  override fun onManual() {}
  override fun onWeekly() {}
  override fun onAuto() {}
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
        ),
        forceSupported = true,
        forceActive = true,
        operatingMode = OperatingMode(
          manualAllowed = true,
          weeklyAllowed = true,
          autoAllowed = true,
          manualActive = true
        ),
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
        leftButtonDisabled = true,
        rightButtonDisabled = true,
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
        ),
      )
    )
  }
}

@Preview(name = "Electricity meter", showBackground = true)
@Preview(name = "Electricity meter", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_ElectricityMeter() {
  SuplaTheme {
    previewScope.View(
      state = SwitchGeneralViewState(
        electricityMeterState = ElectricityMeterState(
          online = true,
          totalForwardActiveEnergy = SummaryCardData("28.13 kWh"),
          currentMonthForwardActiveEnergy = SummaryCardData("0.38 kWh"),
          phaseMeasurementTypes = listOf(
            SuplaElectricityMeasurementType.VOLTAGE,
            SuplaElectricityMeasurementType.CURRENT,
            SuplaElectricityMeasurementType.POWER_ACTIVE,
            SuplaElectricityMeasurementType.POWER_REACTIVE,
            SuplaElectricityMeasurementType.POWER_APPARENT,
            SuplaElectricityMeasurementType.POWER_FACTOR,
            SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY
          ),
          phaseMeasurementValues = listOf(
            PhaseWithMeasurements(
              R.string.details_em_phase1,
              mapOf(
                SuplaElectricityMeasurementType.VOLTAGE to "227.90",
                SuplaElectricityMeasurementType.CURRENT to "0.03",
                SuplaElectricityMeasurementType.POWER_ACTIVE to "5.34",
                SuplaElectricityMeasurementType.POWER_REACTIVE to "4.92",
                SuplaElectricityMeasurementType.POWER_APPARENT to "7.26",
                SuplaElectricityMeasurementType.POWER_FACTOR to "0.735",
                SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY to "28.13400"
              )
            )
          )
        ),
        leftButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_switch_off),
          textRes = R.string.channel_btn_off
        ),
        rightButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_switch_on),
          textRes = R.string.channel_btn_on
        ),
      )
    )
  }
}
