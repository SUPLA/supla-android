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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.HsvColor
import org.supla.android.extensions.applyBrightness
import org.supla.android.features.details.rgbanddimmer.rgb.model.RgbDetailViewState
import org.supla.android.features.details.rgbanddimmer.rgb.model.RgbValue
import org.supla.android.features.details.rgbanddimmer.rgb.model.SavedColor
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.lists.channelissues.ChannelIssuesView
import org.supla.android.ui.views.DeviceState
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.ReorderableRow
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.ui.views.buttons.SwitchButtons
import org.supla.core.shared.infrastructure.localizedString

interface RgbDetailScope : RgbColorDialogScope {
  fun onColorSelectionStarted()
  fun onColorSelecting(color: HsvColor)
  fun onColorSelected(color: HsvColor)
  fun onSavedColorSelected(color: SavedColor)
  fun onSaveCurrentColor()
  fun onRemoveColor(positionOnList: Int)
  fun onMoveColors(from: Int, to: Int)
  fun turnOn()
  fun turnOff()
}

@Composable
fun RgbDetailScope.View(
  state: RgbDetailViewState
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

  state.colorDialogState?.let { ColorDialog(it) }
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

      SavedColors(state.savedColors, !state.offline)

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
private fun RgbDetailScope.Portrait(
  state: RgbDetailViewState
) {
  var color by remember { mutableStateOf(state.value.hsv) }

  LaunchedEffect(state.value) {
    color = state.value.hsv
  }

  Column {
    state.deviceStateData?.let { DeviceState(data = it) }
    state.channelIssues?.let { issues -> ChannelIssuesView(issues) }

    Spacer(Modifier.weight(1f))

    ColorAndBrightnessBox(color, state.value, !state.offline, Modifier.padding(Distance.default))

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
        .sizeIn(maxHeight = 450.dp)
        .padding(Distance.default)
    )

    Spacer(Modifier.weight(1f))

    SavedColors(state.savedColors, !state.offline)

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
private fun RgbDetailScope.SavedColors(savedColors: List<SavedColor>, online: Boolean) {
  ReorderableRow(
    items = savedColors,
    onRemove = { onRemoveColor(it) },
    onMove = { from, to -> onMoveColors(from, to) },
    leadingContent = { dragging, itemOver ->
      if (online) {
        val color = if (itemOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        SavedColorAction(color, dragging)
      }
    },
    modifier = Modifier
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = Distance.default)
  ) {
    SavedColorBox(it, online = online)
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

@Composable
private fun RgbDetailScope.SavedColorBox(color: SavedColor, online: Boolean) =
  Box(
    modifier = Modifier
      .padding(horizontal = Distance.tiny)
      .width(42.dp)
      .height(36.dp)
      .background(
        color = color.color.applyBrightness(color.brightness),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .clickable(enabled = online) { onSavedColorSelected(color) }
  )

@Composable
private fun RgbDetailScope.SavedColorAction(color: Color, dragging: Boolean) =
  Box(
    modifier = Modifier
      .padding(end = Distance.tiny)
      .width(42.dp)
      .height(36.dp)
      .border(
        width = 1.dp,
        color = color,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .clickable(enabled = !dragging) {
        onSaveCurrentColor()
      }
  ) {
    Icon(
      painter = painterResource(if (dragging) R.drawable.ic_delete else R.drawable.ic_plus),
      contentDescription = "Usuń",
      tint = color,
      modifier = Modifier
        .align(Alignment.Center)
        .size(if (dragging) 16.dp else 12.dp)
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
@PreviewScreenSizes
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
