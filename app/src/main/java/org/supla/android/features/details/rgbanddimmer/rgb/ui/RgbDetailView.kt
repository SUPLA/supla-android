package org.supla.android.features.details.rgbanddimmer.rgb.ui
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.LocalSizeClassProvider
import org.supla.android.core.ui.SizeClass
import org.supla.android.core.ui.SizeClassBox
import org.supla.android.core.ui.isSmall
import org.supla.android.core.ui.isSquare
import org.supla.android.core.ui.padding
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.HsvColor
import org.supla.android.features.details.rgbanddimmer.common.SavedColor
import org.supla.android.features.details.rgbanddimmer.common.SavedColorListScope
import org.supla.android.features.details.rgbanddimmer.common.SavedColors
import org.supla.android.features.details.rgbanddimmer.rgb.model.RgbDetailViewState
import org.supla.android.features.details.rgbanddimmer.rgb.model.RgbValue
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.tools.SuplaSizeClassPreview
import org.supla.android.ui.extensions.ifFalse
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.views.DeviceState
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.ui.views.buttons.SwitchButtons
import org.supla.android.ui.views.buttons.SwitchIconButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.core.shared.infrastructure.localizedString

interface RgbDetailScope : RgbColorDialogScope, SavedColorListScope {
  fun onColorSelectionStarted()
  fun onColorSelecting(color: HsvColor)
  fun onColorSelected(color: HsvColor)
  fun turnOn()
  fun turnOff()
}

@Composable
fun RgbDetailScope.View(
  state: RgbDetailViewState
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

    state.colorDialogState?.let { ColorDialog(it) }
  }
}

@Composable
private fun RgbDetailScope.Landscape(
  state: RgbDetailViewState
) {
  var color by remember { mutableStateOf(state.value.hsv) }

  LaunchedEffect(state.value) {
    color = state.value.hsv
  }

  Row {
    Column(
      modifier = Modifier.weight(1f)
    ) {
      state.deviceStateData?.let { DeviceState(data = it) }
      state.channelIssues?.let { issues -> ChannelIssuesView(issues) }

      ColorAndBrightnessBox(color, state.value, !state.offline)

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
      modifier = Modifier.weight(1f)
    ) {
      Spacer(Modifier.weight(1f))

      ColorPickerComponent(
        color = state.value.hsv,
        enabled = !state.offline,
        onColorSelectionStarted = { onColorSelectionStarted() },
        onColorSelecting = { hsv ->
          color = hsv
          onColorSelecting(hsv)
        },
        onColorSelected = { hsv -> onColorSelected(hsv) },
        modifier = Modifier
          .padding(horizontal = Distance.horizontal, vertical = Distance.vertical)
      )

      Spacer(Modifier.weight(1f))
    }
  }
}

@Composable
private fun RgbDetailScope.LandscapeNarrow(state: RgbDetailViewState) {
  var color by remember { mutableStateOf(state.value.hsv) }

  LaunchedEffect(state.value) {
    color = state.value.hsv
  }

  Column {
    ColorAndBrightnessBox(color, state.value, !state.offline, Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.small))

    Row(modifier = Modifier.weight(1f).padding(horizontal = Distance.horizontal)) {
      ColorPickerComponent(
        color = state.value.hsv,
        enabled = !state.offline,
        onColorSelectionStarted = { onColorSelectionStarted() },
        markers = state.value.markers,
        onColorSelecting = { hsv ->
          color = hsv
          onColorSelecting(hsv)
        },
        onColorSelected = { hsv -> onColorSelected(hsv) },
        modifier = Modifier
          .fillMaxHeight()
          .weight(1f)
      )
      Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxHeight()) {
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
private fun RgbDetailScope.Portrait(state: RgbDetailViewState) {
  var color by remember { mutableStateOf(state.value.hsv) }
  val sizeClass = LocalSizeClassProvider.current

  LaunchedEffect(state.value) {
    color = state.value.hsv
  }

  Column {
    state.deviceStateData?.let { DeviceState(data = it, modifier = Modifier.padding(vertical = sizeClass.padding)) }
    sizeClass.isSmall.ifFalse {
      state.channelIssues?.let { issues -> ChannelIssuesView(issues) }
    }

    ColorAndBrightnessBox(color, state.value, !state.offline, Modifier.padding(horizontal = Distance.horizontal))

    sizeClass.isSquare.ifFalse { Spacer(Modifier.weight(1f)) }

    ColorPickerComponent(
      color = state.value.hsv,
      enabled = !state.offline,
      onColorSelectionStarted = { onColorSelectionStarted() },
      markers = state.value.markers,
      onColorSelecting = { hsv ->
        color = hsv
        onColorSelecting(hsv)
      },
      onColorSelected = { hsv -> onColorSelected(hsv) },
      modifier = Modifier
        .fillMaxWidth()
        .let { if (sizeClass.isSquare) it.weight(1f) else it }
        .padding(horizontal = Distance.horizontal, vertical = sizeClass.padding)
    )

    sizeClass.isSquare.ifFalse { Spacer(Modifier.weight(1f)) }

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
private fun RgbDetailScope.ColorAndBrightnessBox(color: HsvColor?, value: RgbValue, online: Boolean, modifier: Modifier = Modifier) =
  Row(
    modifier = modifier
  ) {
    Spacer(Modifier.weight(1f))
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.default),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clickable(enabled = online) { onOpenColorDialog() }
        .background(
          color = MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
        )
        .padding(vertical = Distance.small, horizontal = Distance.default)
    ) {
      ColorRow(color?.color)
      BrightnessBox(color?.value?.let { ValuesFormatter.getPercentageString(it) } ?: value.brightnessString)
    }
    Spacer(Modifier.weight(1f))
  }

@Composable
private fun ColorRow(color: Color?) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    Text(
      text = stringResource(R.string.rgb_detail_color),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    ColorBox(color)
  }

@Composable
private fun BrightnessBox(brightness: String) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    Text(
      text = stringResource(R.string.rgb_detail_brightness),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = brightness,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }

private val previewScope = object : RgbDetailScope {
  override fun onColorSelectionStarted() {}
  override fun onColorSelecting(color: HsvColor) {}
  override fun onColorSelected(color: HsvColor) {}
  override fun onSavedColorSelected(color: SavedColor) {}
  override fun onSaveCurrentColor() {}
  override fun onRemoveColor(positionOnList: Int) {}
  override fun onMoveColors(from: Int, to: Int) {}
  override fun turnOn() {}
  override fun turnOff() {}
  override fun onColorDialogDismiss() {}
  override fun onColorDialogConfirm() {}
  override fun onColorDialogInputChange(value: String) {}
  override fun onOpenColorDialog() {}
}

@SuplaPreviewLandscape
@SuplaPreview
@SuplaSizeClassPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      state = RgbDetailViewState(
        deviceStateData = DeviceStateData(
          label = localizedString(R.string.details_timer_state_label),
          icon = ImageId(R.drawable.fnc_rgb_on),
          value = localizedString(R.string.details_timer_device_on)
        ),
        offButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_rgb_off),
          textRes = R.string.channel_btn_off,
        ),
        onButtonState = SwitchButtonState(
          icon = ImageId(R.drawable.fnc_rgb_on),
          textRes = R.string.channel_btn_on,
        ),
        savedColors = listOf(savedColor(0xFFFF0000), savedColor(0xFF00FF00), savedColor(0xFF0000FF))
      )
    )
  }
}

private fun savedColor(color: Long) = SavedColor(0, Color(color), 100)
