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

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.toGrayColor
import org.supla.android.features.details.rgbanddimmer.common.SavedColor
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerDetailScope
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerDetailViewState
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerSelectorType
import org.supla.android.features.details.rgbanddimmer.common.ui.LinearColorSelector
import org.supla.android.features.details.rgbanddimmer.common.ui.SavedColorBox
import org.supla.android.features.details.rgbanddimmer.common.ui.dimmer.CircularColorSelector
import org.supla.android.features.details.rgbanddimmer.common.ui.dimmer.Scaffold
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.tools.SuplaSizeClassPreview
import org.supla.android.tools.SuplaSmallPreview
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.core.shared.infrastructure.localizedString
import kotlin.math.roundToInt

@Composable
fun DimmerDetailScope.View(
  state: DimmerDetailViewState
) {
  Scaffold(
    state = state,
    brightnessControl = { state, modifier -> BrightnessSelector(state, modifier) },
    savedColorItemContent = { color, online -> SavedColorBox(color, online) }
  )
}

@Composable
private fun DimmerDetailScope.BrightnessSelector(state: DimmerDetailViewState, modifier: Modifier = Modifier) =
  when (state.selectorType) {
    DimmerSelectorType.LINEAR ->
      LinearColorSelector(
        value = state.value.brightness?.div(100f)?.coerceIn(0f, 1f),
        selectedColor = state.value.brightness?.toGrayColor(),
        enabled = !state.offline,
        valueMarkers = state.value.brightnessMarkers.map { it.div(100f).coerceIn(0f, 1f) },
        onValueChangeStarted = { onBrightnessSelectionStarted() },
        onValueChanging = { onBrightnessSelecting(brightness = it.times(100).roundToInt()) },
        onValueChanged = { onBrightnessSelected() },
        modifier = modifier
          .fillMaxHeight()
          .width(40.dp)
      )

    DimmerSelectorType.CIRCULAR ->
      CircularColorSelector(
        value = state.value.brightness?.div(100f)?.coerceIn(0f, 1f),
        selectedColor = state.value.brightness?.toGrayColor(),
        enabled = !state.offline,
        valueMarkers = state.value.brightnessMarkers.map { it.div(100f).coerceIn(0f, 1f) },
        onValueChangeStarted = { onBrightnessSelectionStarted() },
        onValueChanging = { onBrightnessSelecting(brightness = it.times(100).roundToInt()) },
        onValueChanged = { onBrightnessSelected() },
        modifier = Modifier
          .fillMaxSize()
      )
  }

private val previewScope = object : DimmerDetailScope {
  override fun onBrightnessSelectionStarted() {}
  override fun onBrightnessSelecting(brightness: Int) {}
  override fun onBrightnessSelected() {}
  override fun turnOn() {}
  override fun turnOff() {}
  override fun toggleSelectorType() {}
  override fun onSavedColorSelected(color: SavedColor) {}
  override fun onSaveCurrentColor() {}
  override fun onRemoveColor(positionOnList: Int) {}
  override fun onMoveColors(from: Int, to: Int) {}
}

@SuplaPreviewLandscape
@SuplaSmallPreview
@SuplaPreview
@SuplaSizeClassPreview
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

@SuplaPreviewLandscape
@SuplaSmallPreview
@SuplaPreview
@SuplaSizeClassPreview
@Composable
private fun PreviewCircular() {
  SuplaTheme {
    previewScope.View(
      state = DimmerDetailViewState(
        selectorType = DimmerSelectorType.CIRCULAR,
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
