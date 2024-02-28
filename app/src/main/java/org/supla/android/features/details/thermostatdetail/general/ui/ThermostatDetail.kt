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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.core.ui.BaseViewProxy
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.blue
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.thermostatdetail.general.MeasurementValue
import org.supla.android.features.details.thermostatdetail.general.ThermostatGeneralViewState
import org.supla.android.features.details.thermostatdetail.general.data.ThermostatIssueItem
import org.supla.android.features.details.thermostatdetail.general.data.ThermostatProgramInfo
import org.supla.android.features.details.thermostatdetail.ui.ThermometersValues
import org.supla.android.features.details.thermostatdetail.ui.TimerHeader
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.buttons.AnimatableButtonType
import org.supla.android.ui.views.buttons.AnimationMode
import org.supla.android.ui.views.buttons.RoundedControlButton
import org.supla.android.ui.views.thermostat.TemperatureControlButton
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import org.supla.android.ui.views.tools.THERMOSTAT_VERTICAL_POSITION_CORRECTION
import org.supla.android.ui.views.tools.ThermostatControl

interface ThermostatGeneralViewProxy : BaseViewProxy<ThermostatGeneralViewState> {
  fun heatingModeChanged()
  fun coolingModeChanged()
  fun setpointTemperatureChanged(heatPercentage: Float?, coolPercentage: Float?)
  fun changeSetpointTemperature(correction: TemperatureCorrection)
  fun turnOnOffClicked()
  fun manualModeClicked()
  fun weeklyScheduledModeClicked()
  fun getTemperatureText(minPercentage: Float?, maxPercentage: Float?, state: ThermostatGeneralViewState): StringProvider
  fun markChanging()
}

private val indicatorSize = 20.dp

@Composable
fun ThermostatDetail(viewProxy: ThermostatGeneralViewProxy) {
  val viewState by viewProxy.getViewState().collectAsState()

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(color = MaterialTheme.colors.background)
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

      ThermostatView(viewState = viewState, viewProxy = viewProxy, modifier = Modifier.weight(1f))

      BottomButtonsRow(viewState, viewProxy)
    }

    if (viewState.loadingState.loading) {
      LoadingScrim()
    }
  }
}

context (ColumnScope)
@Composable
private fun ThermostatView(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy, modifier: Modifier = Modifier) {
  BoxWithConstraints(modifier = modifier) {
    if (maxHeight < 350.dp) {
      if (viewState.isOff.not() && viewState.isAutoFunction && !viewState.programmedModeActive) {
        HeatingCoolingRowSmallScreen(viewState = viewState, viewProxy = viewProxy)
      }
      TemperatureControlRow(viewState, viewProxy)
      WarningsRow(viewState.issues, smallScreen = true, modifier = Modifier.align(Alignment.BottomStart))
    } else {
      Column {
        if (viewState.isOff.not() && viewState.isAutoFunction && !viewState.programmedModeActive) {
          HeatingCoolingRow(viewState = viewState, viewProxy = viewProxy)
        } else if (viewState.sensorIssue != null) {
          SensorIssueView(sensorIssue = viewState.sensorIssue)
        } else if (viewState.isOffline.not() && viewState.viewModelState?.timerEndDate != null) {
          TimerHeader(
            state = viewState,
            modifier = Modifier
              .fillMaxWidth()
              .height(80.dp)
          )
        } else if (viewState.temporaryProgramInfo.isNotEmpty()) {
          ProgramInfoRow(infos = viewState.temporaryProgramInfo)
        } else {
          Spacer(modifier = Modifier.height(80.dp))
        }
        TemperatureControlRow(viewState, viewProxy)
        WarningsRow(viewState.issues)
      }
    }
  }
}

@Composable
private fun HeatingCoolingRow(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy) {
  Row {
    Spacer(modifier = Modifier.weight(1f))
    HeatingIcon(active = viewState.heatingModeActive) { viewProxy.heatingModeChanged() }
    CoolingIcon(active = viewState.coolingModeActive) { viewProxy.coolingModeChanged() }
  }
}

@Composable
private fun HeatingCoolingRowSmallScreen(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy) {
  Row {
    HeatingIcon(active = viewState.heatingModeActive) { viewProxy.heatingModeChanged() }
    Spacer(modifier = Modifier.weight(1f))
    CoolingIcon(active = viewState.coolingModeActive) { viewProxy.coolingModeChanged() }
  }
}

context (ColumnScope)
@Composable
private fun TemperatureControlRow(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy) {
  ConstraintLayout(
    modifier = Modifier
      .weight(1f)
      .fillMaxWidth()
  ) {
    val (control, heating, cooling, row) = createRefs()
    val context = LocalContext.current

    ThermostatControl(
      modifier = Modifier
        .aspectRatio(1f)
        .fillMaxWidth()
        .constrainAs(control) {
          top.linkTo(parent.top)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
        },
      mainTemperatureTextProvider = { min, max -> viewProxy.getTemperatureText(min, max, viewState)(context) },
      minTemperature = viewState.configMinTemperatureString,
      maxTemperature = viewState.configMaxTemperatureString,
      minSetpoint = viewState.setpointHeatTemperaturePercentage,
      maxSetpoint = viewState.setpointCoolTemperaturePercentage,
      currentValue = viewState.currentTemperaturePercentage,
      isHeating = viewState.showHeatingIndicator,
      isCooling = viewState.showCoolingIndicator,
      isOff = viewState.isOff,
      isOffline = viewState.isOffline,
      onPositionChangeStarted = { viewProxy.markChanging() },
      onPositionChangeEnded = { minPercentage, maxPercentage -> viewProxy.setpointTemperatureChanged(minPercentage, maxPercentage) }
    )

    ThermostatIndicators(viewState, listOf(control, heating, cooling))

    if ((!viewState.isOff || viewState.programmedModeActive) && !viewState.isOffline) {
      Row(
        modifier = Modifier.constrainAs(row) { bottom.linkTo(control.bottom, margin = 60.dp) },
        horizontalArrangement = Arrangement.spacedBy(40.dp)
      ) {
        val color = if (viewState.viewModelState?.lastChangedHeat == false) MaterialTheme.colors.blue else MaterialTheme.colors.error
        Spacer(modifier = Modifier.weight(1f))
        TemperatureControlButton(icon = R.drawable.ic_minus, color = color, disabled = viewState.canDecreaseTemperature.not()) {
          viewProxy.changeSetpointTemperature(TemperatureCorrection.DOWN)
        }
        TemperatureControlButton(icon = R.drawable.ic_plus, color = color, disabled = viewState.canIncreaseTemperature.not()) {
          viewProxy.changeSetpointTemperature(TemperatureCorrection.UP)
        }
        Spacer(modifier = Modifier.weight(1f))
      }
    }
  }
}

context (ConstraintLayoutScope)
@Composable
private fun ThermostatIndicators(viewState: ThermostatGeneralViewState, constraints: List<ConstrainedLayoutReference>) {
  val (control, heating, cooling) = constraints
  val yCorrection = THERMOSTAT_VERTICAL_POSITION_CORRECTION.times(2)
  val distanceFromCenterPoint = 94.dp

  if (viewState.showHeatingIndicator) {
    val margin = yCorrection.minus(indicatorSize).minus(distanceFromCenterPoint)
    IndicatorIcon(
      iconRes = R.drawable.ic_heating,
      modifier = Modifier.constrainAs(heating) {
        top.linkTo(control.top, margin = margin)
        bottom.linkTo(control.bottom)
        start.linkTo(control.start)
        end.linkTo(control.end)
      }
    )
  }

  if (viewState.showCoolingIndicator) {
    val margin = yCorrection.plus(indicatorSize).plus(distanceFromCenterPoint)
    IndicatorIcon(
      iconRes = R.drawable.ic_cooling,
      modifier = Modifier.constrainAs(cooling) {
        top.linkTo(control.top, margin = margin)
        bottom.linkTo(control.bottom)
        start.linkTo(control.start)
        end.linkTo(control.end)
      }
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
    painter = painterResource(id = iconRes),
    contentDescription = null,
    alpha = alpha,
    modifier = modifier.size(indicatorSize)
  )
}

@Composable
private fun HeatingIcon(active: Boolean, onClick: () -> Unit) {
  RoundedControlButton(
    modifier = Modifier
      .padding(
        top = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default),
        bottom = dimensionResource(id = R.dimen.distance_default),
        start = dimensionResource(id = R.dimen.distance_default)
      )
      .width(dimensionResource(id = R.dimen.button_default_size)),
    onClick = onClick,
    icon = painterResource(id = R.drawable.ic_heat),
    type = AnimatableButtonType.NEGATIVE,
    iconAndTextColorSynced = true,
    animationMode = AnimationMode.Toggle(active = active)
  )
}

@Composable
private fun CoolingIcon(active: Boolean, onClick: () -> Unit) {
  RoundedControlButton(
    modifier = Modifier
      .padding(
        top = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default),
        bottom = dimensionResource(id = R.dimen.distance_default)
      )
      .width(dimensionResource(id = R.dimen.button_default_size)),
    onClick = onClick,
    icon = painterResource(id = R.drawable.ic_cool),
    type = AnimatableButtonType.BLUE,
    iconAndTextColorSynced = true,
    animationMode = AnimationMode.Toggle(active = active)
  )
}

@Composable
private fun WarningsRow(warnings: List<ThermostatIssueItem>, modifier: Modifier = Modifier, smallScreen: Boolean = false) {
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
          painter = painterResource(id = it.issueIconType.icon),
          contentDescription = null,
          modifier = Modifier.size(dimensionResource(id = R.dimen.channel_warning_image_size))
        )
        Text(text = stringResource(id = it.descriptionRes), style = MaterialTheme.typography.body2)
      }

      if (smallScreen) {
        return
      }
    }
  }
}

@Composable
private fun BottomButtonsRow(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy) {
  Row(
    modifier = Modifier
      .padding(
        start = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default),
        bottom = dimensionResource(id = R.dimen.distance_default)
      ),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    PowerButton(
      isOff = viewState.isOff && viewState.programmedModeActive.not(),
      disabled = viewState.isOffline
    ) { viewProxy.turnOnOffClicked() }
    RoundedControlButton(
      text = stringResource(id = R.string.thermostat_detail_mode_manual),
      modifier = Modifier.weight(0.5f),
      disabled = viewState.isOffline,
      animationMode = AnimationMode.Stated(active = viewState.manualModeActive)
    ) {
      if (viewState.isOffline.not() && viewState.manualModeActive.not()) {
        viewProxy.manualModeClicked()
      }
    }
    RoundedControlButton(
      text = stringResource(id = R.string.thermostat_detail_mode_weekly_schedule),
      modifier = Modifier.weight(0.5f),
      disabled = viewState.isOffline,
      animationMode = AnimationMode.Stated(active = viewState.programmedModeActive)
    ) {
      if (viewState.isOffline.not() && (viewState.programmedModeActive.not() || viewState.temporaryChangeActive)) {
        viewProxy.weeklyScheduledModeClicked()
      }
    }
  }
}

@Composable
private fun PowerButton(isOff: Boolean, disabled: Boolean, onClick: () -> Unit) {
  RoundedControlButton(
    modifier = Modifier.width(dimensionResource(id = R.dimen.button_default_size)),
    onClick = onClick,
    icon = painterResource(id = R.drawable.ic_power_button),
    iconColor = if (isOff) MaterialTheme.colors.error else MaterialTheme.colors.primary,
    disabled = disabled
  )
}

@Composable
private fun EmptyThermometerValues() = Spacer(
  modifier = Modifier
    .fillMaxWidth()
    .background(color = MaterialTheme.colors.surface)
    .height(80.dp)
)

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    ThermostatDetail(PreviewProxy())
  }
}

@Preview
@Composable
private fun PreviewTemporaryOverride() {
  SuplaTheme {
    ThermostatDetail(
      PreviewProxy(
        ThermostatGeneralViewState(
          temporaryChangeActive = true,
          temporaryProgramInfo = listOf(
            ThermostatProgramInfo(
              ThermostatProgramInfo.Type.CURRENT,
              { "vor 7 hours 10 min." },
              R.drawable.ic_heat,
              R.color.red,
              { "22.7" },
              true
            ),
            ThermostatProgramInfo(ThermostatProgramInfo.Type.NEXT, null, R.drawable.ic_power_button, R.color.gray, { "Turn off" })
          )
        )
      )
    )
  }
}

@Preview
@Composable
private fun PreviewSmall() {
  SuplaTheme {
    Box(modifier = Modifier.height(500.dp)) {
      ThermostatDetail(PreviewProxy())
    }
  }
}

private class PreviewProxy(private var initialState: ThermostatGeneralViewState = ThermostatGeneralViewState(isAutoFunction = true)) :
  ThermostatGeneralViewProxy {
  override fun getViewState(): StateFlow<ThermostatGeneralViewState> =
    MutableStateFlow(
      value = initialState.copy(
        loadingState = LoadingTimeoutManager.LoadingState(loading = false),
        temperatures = listOf(
          MeasurementValue(
            remoteId = 123,
            iconProvider = { ResourcesCompat.getDrawable(it.resources, R.drawable.thermometer, null)!!.toBitmap() },
            value = "12.3"
          )
        )
      )
    )

  override fun heatingModeChanged() {}

  override fun coolingModeChanged() {}

  override fun setpointTemperatureChanged(heatPercentage: Float?, coolPercentage: Float?) {}

  override fun changeSetpointTemperature(correction: TemperatureCorrection) {}

  override fun turnOnOffClicked() {}

  override fun manualModeClicked() {}

  override fun weeklyScheduledModeClicked() {}

  override fun getTemperatureText(minPercentage: Float?, maxPercentage: Float?, state: ThermostatGeneralViewState): StringProvider = { "" }
  override fun markChanging() {}
}
