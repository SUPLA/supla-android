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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.LocalSizeClassProvider
import org.supla.android.core.ui.SizeClass
import org.supla.android.core.ui.SizeClassBox
import org.supla.android.core.ui.isSmall
import org.supla.android.core.ui.padding
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.toGrayColor
import org.supla.android.features.details.rgbanddimmer.common.CircularColorSelector
import org.supla.android.features.details.rgbanddimmer.common.LinearColorSelector
import org.supla.android.features.details.rgbanddimmer.common.SavedColor
import org.supla.android.features.details.rgbanddimmer.common.SavedColorListScope
import org.supla.android.features.details.rgbanddimmer.common.SavedColors
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.tools.SuplaSizeClassPreview
import org.supla.android.tools.SuplaSmallPreview
import org.supla.android.ui.extensions.ifFalse
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.views.DeviceState
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.ui.views.buttons.SwitchButtons
import org.supla.android.ui.views.buttons.SwitchIconButton
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.core.shared.infrastructure.localizedString
import kotlin.math.roundToInt

interface DimmerDetailScope : SavedColorListScope {
  fun onBrightnessSelectionStarted()
  fun onBrightnessSelecting(brightness: Int)
  fun onBrightnessSelected()
  fun turnOn()
  fun turnOff()
  fun toggleSelectorType()
}

@Composable
fun DimmerDetailScope.View(
  state: DimmerDetailViewState
) {
  SizeClassBox(Modifier.fillMaxSize()) {
    when (it) {
      SizeClass.LANDSCAPE_SMALL -> LandscapeNarrow(state)

      SizeClass.SQUARE_SMALL,
      SizeClass.SQUARE_MEDIUM,
      SizeClass.SQUARE_BIG,
      SizeClass.PORTRAIT_SMALL,
      SizeClass.PORTRAIT_MEDIUM,
      SizeClass.PORTRAIT_BIG -> Portrait(state)

      SizeClass.LANDSCAPE_MEDIUM,
      SizeClass.LANDSCAPE_BIG -> Landscape(state)
    }

    if (state.loading) {
      LoadingScrim()
    }
  }
}

@Composable
private fun DimmerDetailScope.LandscapeNarrow(
  state: DimmerDetailViewState
) {
  Column {
    BrightnessBox(state.value, Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.small))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = Distance.horizontal)
    ) {
      BrightnessSelector(
        state = state,
        modifier = Modifier.align(Alignment.Center)
      )

      Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd)
      ) {
        BrightnessSelectorTypeButton(state)
        state.offButtonState?.let {
          SwitchIconButton(
            state = it,
            disabled = state.offline || state.loading,
            colors = SuplaButtonDefaults.errorColors(),
            onClick = { turnOff() }
          )
        }
        state.onButtonState?.let {
          SwitchIconButton(
            state = it,
            disabled = state.offline || state.loading,
            colors = SuplaButtonDefaults.primaryColors(),
            onClick = { turnOn() }
          )
        }
      }
    }

    SavedColors(state.savedColors, !state.offline, Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.small))
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

      SavedColors(state.savedColors, !state.offline, Modifier.padding(horizontal = Distance.horizontal))

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
      BrightnessSelectorBox(
        state = state,
        modifier = Modifier
          .padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
          .fillMaxSize()
      )
    }
  }
}

@Composable
private fun DimmerDetailScope.Portrait(state: DimmerDetailViewState) {
  val sizeClass = LocalSizeClassProvider.current

  Column {
    state.deviceStateData?.let { DeviceState(data = it, modifier = Modifier.padding(vertical = sizeClass.padding)) }
    sizeClass.isSmall.ifFalse {
      state.channelIssues?.let { issues -> ChannelIssuesView(issues) }
    }

    BrightnessBox(state.value, Modifier.padding(horizontal = Distance.horizontal))

    BrightnessSelectorBox(
      state = state,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.horizontal, vertical = sizeClass.padding)
        .weight(1f)
        .align(Alignment.CenterHorizontally)
    )

    SavedColors(state.savedColors, !state.offline, Modifier.padding(horizontal = Distance.horizontal))

    SwitchButtons(
      leftButton = state.offButtonState,
      rightButton = state.onButtonState,
      disabled = state.offline || state.loading,
      leftButtonClick = { turnOff() },
      rightButtonClick = { turnOn() },
      modifier = Modifier.padding(horizontal = Distance.horizontal, vertical = sizeClass.padding)
    )
  }
}

@Composable
private fun DimmerDetailScope.BrightnessSelectorBox(state: DimmerDetailViewState, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
  ) {
    BrightnessSelector(state, modifier = Modifier.align(Alignment.Center))
    BrightnessSelectorTypeButton(state, modifier = Modifier.align(Alignment.TopEnd))
  }
}

@Composable
private fun DimmerDetailScope.BrightnessSelector(state: DimmerDetailViewState, modifier: Modifier = Modifier) =
  when (state.selectorType) {
    DimmerSelectorType.LINEAR ->
      LinearColorSelector(
        value = state.value.brightness?.div(100f)?.coerceIn(0f, 1f),
        selectedColor = state.value.brightness?.toGrayColor(),
        enabled = !state.offline,
        valueMarkers = state.value.markers.map { it.div(100f).coerceIn(0f, 1f) },
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
        valueMarkers = state.value.markers.map { it.div(100f).coerceIn(0f, 1f) },
        onValueChangeStarted = { onBrightnessSelectionStarted() },
        onValueChanging = { onBrightnessSelecting(brightness = it.times(100).roundToInt()) },
        onValueChanged = { onBrightnessSelected() },
        modifier = Modifier
          .fillMaxSize()
      )
  }

@Composable
private fun DimmerDetailScope.BrightnessSelectorTypeButton(state: DimmerDetailViewState, modifier: Modifier = Modifier) =
  SuplaButton(
    onClick = { toggleSelectorType() },
    modifier = modifier
  ) {
    Image(
      painter = painterResource(id = state.selectorType.swapIconRes),
      contentDescription = null,
      alignment = Alignment.Center,
      modifier = Modifier
        .size(dimensionResource(id = R.dimen.icon_default_size))
        .align(Alignment.Center),
    )
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
