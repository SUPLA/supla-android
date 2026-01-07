package org.supla.android.features.details.rgbanddimmer.dimmercct
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.toGrayColor
import org.supla.android.features.details.rgbanddimmer.common.SavedColor
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerDetailScope
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerDetailViewState
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerSelectorType
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerValue
import org.supla.android.features.details.rgbanddimmer.common.ui.LinearColorSelector
import org.supla.android.features.details.rgbanddimmer.common.ui.SavedColorListScope
import org.supla.android.features.details.rgbanddimmer.common.ui.ValuesCard
import org.supla.android.features.details.rgbanddimmer.common.ui.dimmer.BrightnessValueView
import org.supla.android.features.details.rgbanddimmer.common.ui.dimmer.DoubleCircularColorSelector
import org.supla.android.features.details.rgbanddimmer.common.ui.dimmer.Scaffold
import org.supla.android.features.details.rgbanddimmer.common.ui.dimmer.ValueLabel
import org.supla.android.features.details.rgbanddimmer.common.ui.dimmer.ValueRow
import org.supla.android.features.details.rgbanddimmer.rgb.ui.ColorBox
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.tools.SuplaSizeClassPreview
import org.supla.android.tools.SuplaSmallPreview
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.core.shared.infrastructure.localizedString
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val CCT_START_COLOR = Color(0xFFB1E1FF)
private val CCT_END_COLOR = Color(0xFFFFDF00)

private val CCT_SELECTOR_COLORS = listOf(
  Pair(0f, CCT_START_COLOR),
  Pair(0.45f, Color.White),
  Pair(0.55f, Color.White),
  Pair(1f, CCT_END_COLOR)
)
private val CCT_CIRCULAR_SELECTOR_COLORS = listOf(
  Pair(0f, CCT_END_COLOR),
  Pair(0.45f, Color.White),
  Pair(0.55f, Color.White),
  Pair(1f, CCT_START_COLOR)
)

interface DimmerCctDetailScope : DimmerDetailScope {
  fun onCctSelectionStarted()
  fun onCctSelecting(cct: Int)
  fun onCctSelected()
}

@Composable
fun DimmerCctDetailScope.View(
  state: DimmerDetailViewState
) {
  Scaffold(
    state = state,
    brightnessControl = { state, modifier -> Selectors(state, modifier) },
    savedColorItemContent = { color, online -> SavedColorBox(color, online) },
    brightnessBox = { value, modifier -> BrightnessBox(value, modifier) }
  )
}

@Composable
private fun DimmerCctDetailScope.Selectors(state: DimmerDetailViewState, modifier: Modifier = Modifier) =
  when (state.selectorType) {
    DimmerSelectorType.LINEAR -> LinearSelectors(state, modifier)
    DimmerSelectorType.CIRCULAR -> CircularSelectors(state, modifier)
  }

@Composable
private fun DimmerCctDetailScope.LinearSelectors(state: DimmerDetailViewState, modifier: Modifier = Modifier) =
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    LinearColorSelector(
      value = state.value.brightness?.div(100f)?.coerceIn(0f, 1f),
      selectedColor = state.value.brightness?.toGrayColor(),
      enabled = !state.offline,
      valueMarkers = state.value.brightnessMarkers.map { it.div(100f).coerceIn(0f, 1f) },
      onValueChangeStarted = { onBrightnessSelectionStarted() },
      onValueChanging = { onBrightnessSelecting(brightness = it.times(100).roundToInt()) },
      onValueChanged = { onBrightnessSelected() },
      modifier = Modifier
        .fillMaxHeight()
        .width(40.dp)
    )
    state.value.cct?.div(100f)?.coerceIn(0f, 1f).let { cct ->
      LinearColorSelector(
        value = cct,
        selectedColor = cct?.toCctColor(),
        colors = CCT_SELECTOR_COLORS,
        enabled = !state.offline,
        valueMarkers = state.value.cctMarkers.map { it.div(100f).coerceIn(0f, 1f) },
        onValueChangeStarted = { onCctSelectionStarted() },
        onValueChanging = { onCctSelecting(cct = it.times(100).roundToInt()) },
        onValueChanged = { onCctSelected() },
        modifier = Modifier
          .fillMaxHeight()
          .width(40.dp)
      )
    }
  }

@Composable
private fun DimmerCctDetailScope.CircularSelectors(state: DimmerDetailViewState, modifier: Modifier = Modifier) {
  val cct = state.value.cct?.div(100f)?.coerceIn(0f, 1f)
  DoubleCircularColorSelector(
    outerValue = state.value.brightness?.div(100f)?.coerceIn(0f, 1f),
    outerSelectedColor = state.value.brightness?.toGrayColor(),
    outerColors = listOf(Pair(0f, Color.Black), Pair(1f, Color.White)),
    outerValueMarkers = state.value.brightnessMarkers.map { it.div(100f).coerceIn(0f, 1f) },
    innerValue = cct,
    innerSelectedColor = cct?.toCctColor(),
    innerColors = CCT_CIRCULAR_SELECTOR_COLORS,
    innerValueMarkers = state.value.cctMarkers.map { it.div(100f).coerceIn(0f, 1f) },
    modifier = modifier.fillMaxSize(),
    onOuterValueChangeStarted = { onBrightnessSelectionStarted() },
    onOuterValueChanging = { onBrightnessSelecting(brightness = it.times(100).roundToInt()) },
    onOuterValueChanged = { onBrightnessSelected() },
    onInnerValueChangeStarted = { onCctSelectionStarted() },
    onInnerValueChanging = { onCctSelecting(cct = it.times(100).roundToInt()) },
    onInnerValueChanged = { onCctSelected() },
  )
}

@Composable
private fun SavedColorListScope.SavedColorBox(color: SavedColor, online: Boolean) =
  Box(
    modifier = Modifier
      .padding(horizontal = Distance.tiny)
      .width(42.dp)
      .height(36.dp)
      .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .clickable(enabled = online) { onSavedColorSelected(color) }
  ) {
    val radius = dimensionResource(R.dimen.radius_default)
    Box(
      modifier = Modifier
        .width(21.dp)
        .fillMaxHeight()
        .background(
          color = color.brightness.toGrayColor(),
          shape = RoundedCornerShape(topStart = radius, bottomStart = radius)
        )
        .align(Alignment.TopStart)
    )
    Box(
      modifier = Modifier
        .width(21.dp)
        .fillMaxHeight()
        .background(
          color = color.color.div(100f).coerceIn(0f, 1f).toCctColor(),
          shape = RoundedCornerShape(topEnd = radius, bottomEnd = radius)
        )
        .align(Alignment.TopEnd)
    )
  }

@Composable
private fun BrightnessBox(value: DimmerValue, modifier: Modifier = Modifier) =
  ValuesCard(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(Distance.default)
  ) {
    ValueRow {
      BrightnessValueView(value.brightnessString)
    }
    ValueRow {
      ValueLabel(R.string.dimmer_detail_temperature)
      ColorBox(value.cct?.div(100f)?.coerceIn(0f, 1f)?.toCctColor())
    }
  }

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

private fun Float.toCctColor(): Color =
  colorAt(this, CCT_END_COLOR, Color.White, CCT_START_COLOR)

private fun colorAt(t: Float, start: Color, middle: Color, end: Color): Color =
  if (t <= 0.5f) {
    colorAt(t * 2, start, middle)
  } else {
    colorAt(t * 2 - 1, middle, end)
  }

private fun colorAt(t: Float, start: Color, end: Color): Color {
  val tt = max(0f, min(1f, t))
  return Color(
    red = lerp(start.red, end.red, tt),
    green = lerp(start.green, end.green, tt),
    blue = lerp(start.blue, end.blue, tt),
    alpha = lerp(start.alpha, end.alpha, tt)
  )
}

private val previewScope = object : DimmerCctDetailScope {
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
  override fun onCctSelectionStarted() {}
  override fun onCctSelecting(cct: Int) {}
  override fun onCctSelected() {}
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
          icon = ImageId(R.drawable.fnc_dimmer_cct_on),
          value = localizedString(R.string.details_timer_device_on)
        ),
        offButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_dimmer_cct_off),
          textRes = R.string.channel_btn_off,
        ),
        onButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_dimmer_cct_on),
          textRes = R.string.channel_btn_on,
        ),
        savedColors = listOf(SavedColor(1, 85, 85))
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
          icon = ImageId(R.drawable.fnc_dimmer_cct_on),
          value = localizedString(R.string.details_timer_device_on)
        ),
        offButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_dimmer_cct_off),
          textRes = R.string.channel_btn_off,
        ),
        onButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_dimmer_cct_on),
          textRes = R.string.channel_btn_on,
        ),
      )
    )
  }
}
