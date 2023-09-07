package org.supla.android.features.thermostatdetail.thermostatgeneral.ui
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.core.ui.BaseViewProxy
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.temperature.TemperatureCorrection
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.thermostatdetail.thermostatgeneral.ThermostatGeneralViewState
import org.supla.android.features.thermostatdetail.thermostatgeneral.ThermostatTemperature
import org.supla.android.features.thermostatdetail.thermostatgeneral.data.ThermostatIssueItem
import org.supla.android.features.thermostatdetail.ui.ThermometersValues
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.buttons.AnimatableButtonType
import org.supla.android.ui.views.buttons.AnimationMode
import org.supla.android.ui.views.buttons.MinusIconButton
import org.supla.android.ui.views.buttons.PlusIconButton
import org.supla.android.ui.views.buttons.RoundedControlButton
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import org.supla.android.ui.views.tools.ThermostatControl

interface ThermostatGeneralViewProxy : BaseViewProxy<ThermostatGeneralViewState> {
  fun heatingModeChanged()
  fun coolingModeChanged()
  fun setpointTemperatureChanged(minPercentage: Float?, maxPercentage: Float?)
  fun changeSetpointTemperature(correction: TemperatureCorrection)
  fun turnOnOffClicked()
  fun manualModeClicked()
  fun weeklyScheduledModeClicked()
  fun getTemperatureText(minPercentage: Float?, maxPercentage: Float?, state: ThermostatGeneralViewState): StringProvider
  fun markChanging()
}

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
        Shadow(orientation = ShadowOrientation.STARTING_TOP)
        EmptyThermometerValues()
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
      if (viewState.isOff.not() && viewState.isAutoFunction) {
        HeatingCoolingRowSmallScreen(viewState = viewState, viewProxy = viewProxy)
      }
      TemperatureControlRow(viewState, viewProxy)
      WarningsRow(viewState.issues, modifier = Modifier.align(Alignment.BottomStart))
    } else {
      Column {
        if (viewState.isOff.not() && viewState.isAutoFunction) {
          HeatingCoolingRow(viewState = viewState, viewProxy = viewProxy)
        } else {
          Spacer(modifier = Modifier.height(96.dp))
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
    val (control, row) = createRefs()
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
      minSetpoint = viewState.setpointMinTemperaturePercentage,
      maxSetpoint = viewState.setpointMaxTemperaturePercentage,
      currentValue = viewState.currentTemperaturePercentage,
      isHeating = viewState.isCurrentlyHeating,
      isCooling = viewState.isCurrentlyCooling,
      isOff = viewState.isOff,
      isOffline = viewState.isOffline,
      isInManualMode = viewState.manualModeActive,
      onPositionChangeStarted = { viewProxy.markChanging() },
      onPositionChangeEnded = { minPercentage, maxPercentage -> viewProxy.setpointTemperatureChanged(minPercentage, maxPercentage) }
    )

    if (viewState.manualModeActive) {
      Row(
        modifier = Modifier.constrainAs(row) { bottom.linkTo(control.bottom, margin = 60.dp) },
        horizontalArrangement = Arrangement.spacedBy(40.dp)
      ) {
        Spacer(modifier = Modifier.weight(1f))
        MinusIconButton(disabled = viewState.canDecreaseTemperature.not()) {
          viewProxy.changeSetpointTemperature(TemperatureCorrection.DOWN)
        }
        PlusIconButton(disabled = viewState.canIncreaseTemperature.not()) {
          viewProxy.changeSetpointTemperature(TemperatureCorrection.UP)
        }
        Spacer(modifier = Modifier.weight(1f))
      }
    }
  }
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
private fun WarningsRow(warnings: List<ThermostatIssueItem>, modifier: Modifier = Modifier) {
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
    }
  }
}

@Composable
private fun BottomButtonsRow(viewState: ThermostatGeneralViewState, viewProxy: ThermostatGeneralViewProxy) {
  Row(
    modifier = Modifier
      .padding(all = dimensionResource(id = R.dimen.distance_default)),
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
      if (viewState.isOffline.not() && viewState.programmedModeActive.not()) {
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
private fun PreviewSmall() {
  SuplaTheme {
    Box(modifier = Modifier.height(500.dp)) {
      ThermostatDetail(PreviewProxy())
    }
  }
}

private class PreviewProxy : ThermostatGeneralViewProxy {
  override fun getViewState(): StateFlow<ThermostatGeneralViewState> =
    MutableStateFlow(
      value = ThermostatGeneralViewState(
        isAutoFunction = true,
        loadingState = LoadingTimeoutManager.LoadingState(loading = false),
        temperatures = listOf(
          ThermostatTemperature(123, { ResourcesCompat.getDrawable(it.resources, R.drawable.thermometer, null)!!.toBitmap() }, "12.3")
        )
      )
    )

  override fun heatingModeChanged() {}

  override fun coolingModeChanged() {}

  override fun setpointTemperatureChanged(minPercentage: Float?, maxPercentage: Float?) {}

  override fun changeSetpointTemperature(correction: TemperatureCorrection) {}

  override fun turnOnOffClicked() {}

  override fun manualModeClicked() {}

  override fun weeklyScheduledModeClicked() {}

  override fun getTemperatureText(minPercentage: Float?, maxPercentage: Float?, state: ThermostatGeneralViewState): StringProvider = { "" }
  override fun markChanging() {}
}