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

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.supla.android.R
import org.supla.android.core.shared.data.model.lists.resource
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.formatting.LocalPercentageFormatter
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.thermostatdetail.general.ThermostatGeneralViewState
import org.supla.android.features.details.thermostatdetail.ui.ThermometersValues
import org.supla.android.images.ImageId
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.lists.data.warning
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.android.ui.views.channelstate.ChannelCurrentState
import org.supla.android.ui.views.channelstate.ChannelProgramData
import org.supla.android.ui.views.channelstate.ChannelProgramState
import org.supla.android.ui.views.thermostat.TemperatureControlButton
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import org.supla.android.ui.views.tools.ThermostatControl
import org.supla.android.usecases.thermostat.MeasurementValue
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.channel.valueformatter.DefaultValueFormatter

interface ThermostatGeneralScope {
  fun heatingModeChanged()
  fun coolingModeChanged()
  fun heatingAndCoolingModeChanged()
  fun setpointTemperatureChanged(heatPercentage: Float?, coolPercentage: Float?)
  fun changeSetpointTemperature(correction: TemperatureCorrection)
  fun turnOnOffClicked()
  fun manualModeClicked()
  fun weeklyScheduledModeClicked()
  fun getTemperatureText(minPercentage: Float?, maxPercentage: Float?, state: ThermostatGeneralViewState): LocalizedString
  fun markChanging()
}

private val indicatorSize = 20.dp

@Composable
fun ThermostatGeneralScope.View(viewState: ThermostatGeneralViewState) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(color = MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
    ) {
      if (viewState.temperatures.isNotEmpty()) {
        ThermometersValues(temperatures = viewState.temperatures)
        Shadow(orientation = ShadowOrientation.STARTING_TOP)
      } else {
        // To avoid screen jumping
        EmptyThermometerValues()
        Shadow(orientation = ShadowOrientation.STARTING_TOP)
      }

      ThermostatView(viewState = viewState, modifier = Modifier.weight(1f))
    }

    if (viewState.loadingState.loading) {
      LoadingScrim()
    }
  }
}

@Composable
private fun ThermostatGeneralScope.ThermostatView(viewState: ThermostatGeneralViewState, modifier: Modifier = Modifier) {
  BoxWithConstraints(modifier = modifier) {
    if (maxHeight < 298.dp) {
      if (viewState.isOff.not() && viewState.isAutoFunction && !viewState.programmedModeActive) {
        HeatingCoolingRowSmallScreen(viewState = viewState)
      }
      TemperatureControlRow(viewState, true)
      BottomButtonsForSmallScreen(viewState, modifier = Modifier.align(Alignment.BottomStart))
    } else if (maxHeight < 450.dp) {
      Column {
        Box(modifier = Modifier.weight(1f)) {
          if (viewState.isOff.not() && viewState.isAutoFunction && !viewState.programmedModeActive) {
            HeatingCoolingRowSmallScreen(viewState = viewState)
          }
          TemperatureControlRow(viewState, false)
          WarningsRow(viewState.issues, smallScreen = true, modifier = Modifier.align(Alignment.BottomStart))
        }
        BottomButtonsRow(viewState)
      }
    } else {
      Column {
        if (viewState.isOff.not() && viewState.isAutoFunction && !viewState.programmedModeActive) {
          if (viewState.viewModelState?.timerEndDate != null) {
            ChannelCurrentState(
              data = viewState.deviceState,
              modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
            )
          } else {
            HeatingCoolingRow(viewState = viewState)
          }
        } else if (viewState.sensorIssue != null) {
          SensorIssueView(sensorIssue = viewState.sensorIssue)
        } else if (viewState.isOffline.not() && viewState.viewModelState?.timerEndDate != null) {
          ChannelCurrentState(
            data = viewState.deviceState,
            modifier = Modifier
              .fillMaxWidth()
              .height(80.dp)
          )
        } else if (viewState.temporaryProgramInfo.isNotEmpty()) {
          ChannelProgramState(infos = viewState.temporaryProgramInfo)
        } else {
          Spacer(modifier = Modifier.height(80.dp))
        }
        TemperatureControlRow(viewState, false, modifier = Modifier.weight(1f))
        WarningsRow(viewState.issues)
        BottomButtonsRow(viewState)
      }
    }
  }
}

@Composable
private fun ThermostatGeneralScope.HeatingCoolingRow(viewState: ThermostatGeneralViewState) {
  Row(modifier = Modifier.padding(all = Distance.default), horizontalArrangement = Arrangement.spacedBy(Distance.small)) {
    Spacer(modifier = Modifier.weight(1f))
    HeatingIcon(active = viewState.heatingModeActive) { heatingModeChanged() }
    CoolingIcon(active = viewState.coolingModeActive) { coolingModeChanged() }
    HeatingAndCoolingIcon(active = viewState.heatingAndCoolingActive) { heatingAndCoolingModeChanged() }
  }
}

@Composable
private fun ThermostatGeneralScope.HeatingCoolingRowSmallScreen(viewState: ThermostatGeneralViewState) {
  Row(modifier = Modifier.padding(all = Distance.small)) {
    HeatingIcon(active = viewState.heatingModeActive) { heatingModeChanged() }
    Spacer(modifier = Modifier.weight(1f))
    Column(verticalArrangement = Arrangement.spacedBy(Distance.small), horizontalAlignment = Alignment.End) {
      HeatingAndCoolingIcon(active = viewState.heatingAndCoolingActive) { heatingAndCoolingModeChanged() }
      CoolingIcon(active = viewState.coolingModeActive) { coolingModeChanged() }
    }
  }
}

@Composable
private fun ThermostatGeneralScope.TemperatureControlRow(
  viewState: ThermostatGeneralViewState,
  isSmallScreen: Boolean,
  modifier: Modifier = Modifier
) {
  ConstraintLayout(
    constraintSet = Constraints.build(viewState, isSmallScreen, Distance.toStatic()),
    modifier = modifier.fillMaxWidth()
  ) {
    val context = LocalContext.current

    ThermostatControl(
      modifier = Modifier
        .aspectRatio(1f)
        .layoutId(Constraints.CONTROL_WHEEL),
      mainTemperatureTextProvider = { min, max -> getTemperatureText(min, max, viewState)(context) },
      minTemperature = viewState.configMinTemperatureString,
      maxTemperature = viewState.configMaxTemperatureString,
      minSetpoint = viewState.setpointHeatTemperaturePercentage,
      maxSetpoint = viewState.setpointCoolTemperaturePercentage,
      currentValue = viewState.currentTemperaturePercentage,
      isHeating = viewState.showHeatingIndicator,
      isCooling = viewState.showCoolingIndicator,
      isOff = viewState.isOff,
      currentPower = viewState.currentPower,
      isOffline = viewState.isOffline,
      isDisabled = viewState.isOffline || viewState.buttonsDisabled,
      onPositionChangeStarted = { markChanging() },
      onPositionChangeEnded = { minPercentage, maxPercentage -> setpointTemperatureChanged(minPercentage, maxPercentage) }
    )

    ThermostatIndicators(viewState)
    ThermostatWheelControlButtons(viewState)
  }
}

@Composable
private fun ThermostatIndicators(viewState: ThermostatGeneralViewState) {
  if (viewState.showHeatingIndicator) {
    ThermostatHeatingIndicator(viewState = viewState)
  }
  if (viewState.showCoolingIndicator) {
    ThermostatCoolingIndicator(viewState = viewState)
  }
  viewState.pumpSwitchIcon?.let {
    Image(
      imageId = it,
      modifier = Modifier.layoutId(Constraints.PUMP_ICON),
      size = 32.dp
    )
  }
  viewState.heatOrColdSourceSwitchIcon?.let {
    Image(
      imageId = it,
      modifier = Modifier.layoutId(Constraints.SOURCE_ICON),
      size = 32.dp
    )
  }
}

@Composable
fun ThermostatHeatingIndicator(viewState: ThermostatGeneralViewState) {
  IndicatorIcon(
    iconRes = R.drawable.ic_heating,
    modifier = Modifier.layoutId(Constraints.HEATING_ICON)
  )
  viewState.currentPower?.let {
    Text(
      text = LocalPercentageFormatter.current.format(it),
      modifier = Modifier.layoutId(Constraints.HEATING_TEXT),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onBackground
    )
  }
}

@Composable
fun ThermostatCoolingIndicator(viewState: ThermostatGeneralViewState) {
  IndicatorIcon(
    iconRes = R.drawable.ic_cooling,
    modifier = Modifier.layoutId(Constraints.COOLING_ICON)
  )

  viewState.currentPower?.let {
    Text(
      text = LocalPercentageFormatter.current.format(it),
      modifier = Modifier.layoutId(Constraints.COOLING_TEXT),
      style = MaterialTheme.typography.labelLarge
    )
  }
}

@Composable
private fun IndicatorIcon(iconRes: Int, modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "Indicator alpha transition")
  val alpha by transition.animateFloat(
    initialValue = 1f,
    targetValue = 0.2f,
    animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
    label = "indicator alpha value"
  )

  Image(
    drawableId = iconRes,
    contentDescription = null,
    alpha = alpha,
    modifier = modifier.size(indicatorSize)
  )
}

@Composable
private fun ThermostatGeneralScope.ThermostatWheelControlButtons(viewState: ThermostatGeneralViewState) {
  if ((!viewState.isOff || viewState.programmedModeActive) && !viewState.isOffline && !viewState.buttonsDisabled) {
    val color = if (viewState.viewModelState?.lastChangedHeat == false) {
      MaterialTheme.colorScheme.secondary
    } else {
      MaterialTheme.colorScheme.error
    }
    TemperatureControlButton(
      icon = R.drawable.ic_plus,
      color = color,
      disabled = viewState.canIncreaseTemperature.not(),
      onClick = { changeSetpointTemperature(TemperatureCorrection.UP) },
      modifier = Modifier.layoutId(Constraints.INCREASE_BUTTON)
    )
    TemperatureControlButton(
      icon = R.drawable.ic_minus,
      color = color,
      disabled = viewState.canDecreaseTemperature.not(),
      onClick = { changeSetpointTemperature(TemperatureCorrection.DOWN) },
      modifier = Modifier.layoutId(Constraints.DECREASE_BUTTON)
    )
  }
}

@Composable
private fun HeatingIcon(active: Boolean, onClick: () -> Unit) {
  SuplaButton(
    iconRes = R.drawable.ic_heat,
    onClick = onClick,
    modifier = Modifier.width(dimensionResource(id = R.dimen.button_default_size)),
    pressed = active,
    colors = SuplaButtonDefaults.errorColors()
  )
}

@Composable
private fun CoolingIcon(active: Boolean, onClick: () -> Unit) {
  SuplaButton(
    iconRes = R.drawable.ic_cool,
    onClick = onClick,
    modifier = Modifier.width(dimensionResource(id = R.dimen.button_default_size)),
    pressed = active,
    colors = SuplaButtonDefaults.secondaryColors()
  )
}

@Composable
private fun HeatingAndCoolingIcon(active: Boolean, onClick: () -> Unit) {
  SuplaButton(
    onClick = onClick,
    modifier = Modifier.width(72.dp),
    active = active,
    colors = SuplaButtonDefaults.buttonColors()
  ) {
    Row(modifier = Modifier.align(Alignment.Center)) {
      Image(
        drawableId = R.drawable.ic_heat,
        contentDescription = null,
        modifier = Modifier
          .size(dimensionResource(id = R.dimen.icon_default_size)),
        tint = active.ifTrue { MaterialTheme.colorScheme.error } ?: MaterialTheme.colorScheme.onBackground
      )
      Image(
        drawableId = R.drawable.ic_cool,
        contentDescription = null,
        modifier = Modifier
          .size(dimensionResource(id = R.dimen.icon_default_size)),
        tint = active.ifTrue { MaterialTheme.colorScheme.secondary } ?: MaterialTheme.colorScheme.onBackground
      )
    }
  }
}

@Composable
private fun WarningsRow(warnings: List<ChannelIssueItem>, modifier: Modifier = Modifier, smallScreen: Boolean = false) {
  val defaultPadding = dimensionResource(id = R.dimen.distance_default)
  Column(
    modifier = modifier.padding(start = defaultPadding, end = defaultPadding),
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny))
  ) {
    warnings.forEach {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_small))
      ) {
        Image(
          drawableId = it.icon.resource,
          contentDescription = null,
          modifier = Modifier.size(dimensionResource(id = R.dimen.channel_warning_image_size))
        )
        Text(
          text = it.message(LocalContext.current),
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      if (smallScreen) {
        return
      }
    }
  }
}

@Composable
private fun ThermostatGeneralScope.BottomButtonsRow(viewState: ThermostatGeneralViewState, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .padding(
        start = Distance.default,
        end = Distance.default,
        bottom = Distance.default,
        top = Distance.tiny
      ),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    PowerButton(
      isOff = viewState.isOff && viewState.programmedModeActive.not(),
      disabled = viewState.buttonsDisabled
    ) { turnOnOffClicked() }
    SuplaButton(
      text = stringResource(id = R.string.thermostat_detail_mode_manual),
      modifier = Modifier.weight(0.5f),
      disabled = viewState.buttonsDisabled,
      pressed = viewState.manualModeActive
    ) {
      if (viewState.isOffline.not() && viewState.manualModeActive.not()) {
        manualModeClicked()
      }
    }
    SuplaButton(
      text = stringResource(id = R.string.thermostat_detail_mode_weekly_schedule),
      modifier = Modifier.weight(0.5f),
      disabled = viewState.buttonsDisabled,
      pressed = viewState.programmedModeActive
    ) {
      if (viewState.isOffline.not() && (viewState.programmedModeActive.not() || viewState.temporaryChangeActive)) {
        weeklyScheduledModeClicked()
      }
    }
  }
}

@Composable
private fun ThermostatGeneralScope.BottomButtonsForSmallScreen(
  viewState: ThermostatGeneralViewState,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .padding(all = Distance.small),
    verticalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    PowerButton(
      isOff = viewState.isOff && viewState.programmedModeActive.not(),
      disabled = viewState.isOffline
    ) { turnOnOffClicked() }
    SuplaButton(
      iconRes = R.drawable.ic_manual,
      disabled = viewState.isOffline,
      pressed = viewState.manualModeActive
    ) {
      if (viewState.isOffline.not() && viewState.manualModeActive.not()) {
        manualModeClicked()
      }
    }
    SuplaButton(
      iconRes = R.drawable.ic_schedule,
      disabled = viewState.isOffline,
      pressed = viewState.programmedModeActive
    ) {
      if (viewState.isOffline.not() && (viewState.programmedModeActive.not() || viewState.temporaryChangeActive)) {
        weeklyScheduledModeClicked()
      }
    }
  }
}

@Composable
private fun PowerButton(isOff: Boolean, disabled: Boolean, onClick: () -> Unit) {
  val color = if (isOff) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
  SuplaButton(
    iconRes = R.drawable.ic_power_button,
    disabled = disabled,
    colors = SuplaButtonDefaults.buttonColors(content = color, contentPressed = color),
    onClick = onClick
  )
}

@Composable
private fun EmptyThermometerValues() = Spacer(
  modifier = Modifier
    .fillMaxWidth()
    .background(color = MaterialTheme.colorScheme.surface)
    .height(dimensionResource(id = R.dimen.detail_top_component))
)

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    PreviewThermostatGeneralScope.View(
      ThermostatGeneralViewState(
        thermometerValueFormatter = DefaultValueFormatter,
        showHeatingIndicator = true,
        currentPower = 18f,
        pumpSwitchIcon = ImageId(R.drawable.fnc_pump_switch_on),
        heatOrColdSourceSwitchIcon = ImageId(R.drawable.fnc_heat_or_cold_source_switch_off)
      ).withPreviewData()
    )
  }
}

@Preview
@Composable
private fun PreviewCooling() {
  SuplaTheme {
    PreviewThermostatGeneralScope.View(
      ThermostatGeneralViewState(
        thermometerValueFormatter = DefaultValueFormatter,
        showCoolingIndicator = true,
        currentPower = 18f,
        pumpSwitchIcon = ImageId(R.drawable.fnc_pump_switch_off),
        heatOrColdSourceSwitchIcon = ImageId(R.drawable.fnc_heat_or_cold_source_switch_on)
      ).withPreviewData()
    )
  }
}

@Preview
@Composable
private fun PreviewTemporaryOverride() {
  SuplaTheme {
    PreviewThermostatGeneralScope.View(
      ThermostatGeneralViewState(
        thermometerValueFormatter = DefaultValueFormatter,
        temporaryChangeActive = true,
        temporaryProgramInfo = listOf(
          ChannelProgramData(
            ChannelProgramData.Type.CURRENT,
            LocalizedString.Constant("vor 7 hours 10 min."),
            R.drawable.ic_heat,
            R.color.red,
            LocalizedString.Constant("22.7"),
            R.drawable.ic_manual,
            R.color.primary
          ),
          ChannelProgramData(
            ChannelProgramData.Type.NEXT,
            null,
            R.drawable.ic_power_button,
            R.color.on_surface_variant,
            LocalizedString.Constant("Turn off")
          )
        ),
        coolingModeActive = true,
        isAutoFunction = true,
        issues = listOf(
          ChannelIssueItem.warning(R.string.thermostat_detail_mode_manual)
        )
      ).withPreviewData()
    )
  }
}

@Preview
@Composable
private fun PreviewSmall() {
  SuplaTheme {
    Box(modifier = Modifier.height(500.dp)) {
      PreviewThermostatGeneralScope.View(
        ThermostatGeneralViewState(
          thermometerValueFormatter = DefaultValueFormatter,
          issues = listOf(
            ChannelIssueItem.warning(R.string.thermostat_detail_mode_manual)
          ),
          coolingModeActive = true,
          isAutoFunction = true,
        ).withPreviewData()
      )
    }
  }
}

@Preview
@Composable
private fun PreviewVerySmall() {
  SuplaTheme {
    Box(modifier = Modifier.height(381.dp)) {
      PreviewThermostatGeneralScope.View(
        ThermostatGeneralViewState(
          thermometerValueFormatter = DefaultValueFormatter,
          showHeatingIndicator = true,
          showCoolingIndicator = true,
          currentPower = 18f,
          pumpSwitchIcon = ImageId(R.drawable.fnc_pump_switch_on),
          heatOrColdSourceSwitchIcon = ImageId(R.drawable.fnc_heat_or_cold_source_switch_off),
          coolingModeActive = true,
          isAutoFunction = true,
        ).withPreviewData()
      )
    }
  }
}

private object PreviewThermostatGeneralScope : ThermostatGeneralScope {
  override fun heatingModeChanged() {}
  override fun coolingModeChanged() {}
  override fun heatingAndCoolingModeChanged() {}
  override fun setpointTemperatureChanged(heatPercentage: Float?, coolPercentage: Float?) {}
  override fun changeSetpointTemperature(correction: TemperatureCorrection) {}
  override fun turnOnOffClicked() {}
  override fun manualModeClicked() {}
  override fun weeklyScheduledModeClicked() {}
  override fun getTemperatureText(
    minPercentage: Float?,
    maxPercentage: Float?,
    state: ThermostatGeneralViewState
  ) = LocalizedString.Constant("25,0°")

  override fun markChanging() {}
}

private fun ThermostatGeneralViewState.withPreviewData() = copy(
  loadingState = LoadingTimeoutManager.LoadingState(loading = false),
  temperatures = listOf(
    MeasurementValue(
      remoteId = 123,
      imageId = ImageId(R.drawable.fnc_thermometer),
      value = "12.3"
    )
  )
)
