package org.supla.android.features.details.rgbanddimmer.dimmer
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.toGrayColor
import org.supla.android.features.details.rgbanddimmer.common.LinearColorSelector
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.tools.SuplaSmallPreview
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.views.DeviceState
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.ui.views.buttons.SwitchButtons
import org.supla.core.shared.infrastructure.localizedString
import kotlin.math.roundToInt

interface DimmerDetailScope {
  fun onBrightnessSelectionStarted()
  fun onBrightnessSelecting(brightness: Int)
  fun onBrightnessSelected()
  fun turnOn()
  fun turnOff()
}

@Composable
fun DimmerDetailScope.View(
  state: DimmerDetailViewState
) {
  val isTablet = currentWindowAdaptiveInfo().windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && !isTablet) {
      Landscape(state)
    } else {
      Portrait(state)
    }

    if (state.loading) {
      LoadingScrim()
    }
  }
}

@Composable
private fun DimmerDetailScope.Landscape(
  state: DimmerDetailViewState
) {
  Row {
    Column(
      modifier = Modifier.weight(1f)
    ) {
      state.deviceStateData?.let { DeviceState(data = it) }
      state.channelIssues?.let { issues -> ChannelIssuesView(issues) }

      BrightnessBox(state.value)

      Spacer(Modifier.weight(1f))

      SwitchButtons(
        leftButton = state.offButtonState,
        rightButton = state.onButtonState,
        disabled = state.offline || state.loading,
        leftButtonClick = { turnOff() },
        rightButtonClick = { turnOn() },
      )
    }
    Column(
      modifier = Modifier.weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      LinearColorSelector(
        value = state.value.brightness?.div(100f)?.coerceIn(0f, 1f),
        selectedColor = state.value.brightness?.toGrayColor(),
        enabled = !state.offline,
        onValueChangeStarted = { onBrightnessSelectionStarted() },
        onValueChanging = { onBrightnessSelecting(brightness = it.times(100).roundToInt()) },
        onValueChanged = { onBrightnessSelected() },
        modifier = Modifier
          .padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
          .width(40.dp)
          .fillMaxHeight()
      )
    }
  }
}

@Composable
private fun DimmerDetailScope.Portrait(
  state: DimmerDetailViewState
) {
  BoxWithConstraints {
    if (maxHeight < 550.dp) {
      LowPortrait(state)
    } else {
      HighPortrait(state)
    }
  }
}

@Composable
private fun DimmerDetailScope.LowPortrait(state: DimmerDetailViewState) {
  Column {
    state.deviceStateData?.let { DeviceState(data = it) }
    state.channelIssues?.let { issues -> ChannelIssuesView(issues) }

    BrightnessBox(state.value)

    LinearColorSelector(
      value = state.value.brightness?.div(100f)?.coerceIn(0f, 1f),
      selectedColor = state.value.brightness?.toGrayColor(),
      enabled = !state.offline,
      valueMarkers = state.value.markers.map { it.div(100f).coerceIn(0f, 1f) },
      onValueChangeStarted = { onBrightnessSelectionStarted() },
      onValueChanging = { onBrightnessSelecting(brightness = it.times(100).roundToInt()) },
      onValueChanged = { onBrightnessSelected() },
      modifier = Modifier
        .padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
        .weight(1f)
        .width(40.dp)
        .align(Alignment.CenterHorizontally)
    )

    SwitchButtons(
      leftButton = state.offButtonState,
      rightButton = state.onButtonState,
      disabled = state.offline || state.loading,
      leftButtonClick = { turnOff() },
      rightButtonClick = { turnOn() },
    )
  }
}

@Composable
private fun DimmerDetailScope.HighPortrait(state: DimmerDetailViewState) {
  Column {
    state.deviceStateData?.let { DeviceState(data = it) }
    state.channelIssues?.let { issues -> ChannelIssuesView(issues) }

    Spacer(Modifier.weight(0.2f))

    BrightnessBox(state.value)

    LinearColorSelector(
      value = state.value.brightness?.div(100f)?.coerceIn(0f, 1f),
      selectedColor = state.value.brightness?.toGrayColor(),
      enabled = !state.offline,
      valueMarkers = state.value.markers.map { it.div(100f).coerceIn(0f, 1f) },
      onValueChangeStarted = { onBrightnessSelectionStarted() },
      onValueChanging = { onBrightnessSelecting(brightness = it.times(100).roundToInt()) },
      onValueChanged = { onBrightnessSelected() },
      modifier = Modifier
        .padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
        .weight(1f)
        .width(40.dp)
        .align(Alignment.CenterHorizontally)
    )

    Spacer(Modifier.weight(0.2f))

    SwitchButtons(
      leftButton = state.offButtonState,
      rightButton = state.onButtonState,
      disabled = state.offline || state.loading,
      leftButtonClick = { turnOff() },
      rightButtonClick = { turnOn() },
    )
  }
}

@Composable
private fun BrightnessBox(value: DimmerValue, modifier: Modifier = Modifier) =
  Row(modifier = modifier) {
    Spacer(Modifier.weight(1f))
    BrightnessField(value)
    Spacer(Modifier.weight(1f))
  }

@Composable
private fun BrightnessField(value: DimmerValue) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
    modifier = Modifier
      .background(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .padding(vertical = Distance.small, horizontal = Distance.default)
  ) {
    Text(
      text = stringResource(R.string.rgb_detail_brightness),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value.brightnessString,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }

private val previewScope = object : DimmerDetailScope {
  override fun onBrightnessSelectionStarted() {}
  override fun onBrightnessSelecting(brightness: Int) {}
  override fun onBrightnessSelected() {}
  override fun turnOn() {}
  override fun turnOff() {}
}

@SuplaPreviewLandscape
@SuplaPreview
@SuplaSmallPreview
@PreviewScreenSizes
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      state = DimmerDetailViewState(
        deviceStateData = DeviceStateData(
          label = localizedString(R.string.details_timer_state_label),
          icon = ImageId(R.drawable.fnc_dimmer_on),
          value = localizedString(R.string.details_timer_device_on)
        ),
        offButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_dimmer_off),
          textRes = R.string.channel_btn_off,
        ),
        onButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_dimmer_on),
          textRes = R.string.channel_btn_on,
        ),
      )
    )
  }
}
