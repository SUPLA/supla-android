package org.supla.android.features.details.rgbanddimmer.common.ui.dimmer
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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.rgbanddimmer.common.ui.OUTER_SURFACE_WIDTH
import org.supla.android.tools.SuplaComponentPreview

@Composable
fun CircularColorSelector(
  value: Float?,
  selectedColor: Color?,
  modifier: Modifier = Modifier,
  valueMarkers: List<Float> = emptyList(),
  enabled: Boolean = true,
  startColor: Color = Color.White,
  endColor: Color = Color.Black,
  onValueChangeStarted: () -> Unit = {},
  onValueChanging: (Float) -> Unit = {},
  onValueChanged: () -> Unit = {}
) =
  CircularColorSelector(
    value = value,
    selectedColor = selectedColor,
    colors = listOf(Pair(0f, endColor), Pair(1f, startColor)),
    modifier = modifier,
    valueMarkers = valueMarkers,
    enabled = enabled,
    onValueChangeStarted = onValueChangeStarted,
    onValueChanging = onValueChanging,
    onValueChanged = onValueChanged
  )

@Composable
fun CircularColorSelector(
  value: Float?,
  selectedColor: Color?,
  colors: List<Pair<Float, Color>>,
  modifier: Modifier = Modifier,
  valueMarkers: List<Float> = emptyList(),
  enabled: Boolean = true,
  onValueChangeStarted: () -> Unit = {},
  onValueChanging: (Float) -> Unit = {},
  onValueChanged: () -> Unit = {}
) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  val drawer = remember {
    CircularColorSelectorDrawer(
      outerMarginDp = OUTER_SURFACE_WIDTH,
      surfaceColor = surfaceColor,
      onValueChangeStarted = onValueChangeStarted,
      onValueChanging = onValueChanging,
      onValueChanged = onValueChanged
    )
  }

  Canvas(
    modifier = modifier
      .pointerInput(enabled) {
        detectDragGestures(
          onDragStart = { drawer.handleDragStart(this, enabled, it) },
          onDragEnd = { drawer.handleDragEnd(enabled) },
          onDragCancel = { drawer.handleDragCancel() },
          onDrag = { change, _ -> drawer.handleDrag(this, change, enabled) }
        )
      }
      .pointerInput(enabled) {
        detectTapGestures { drawer.handleTap(this, enabled, it) }
      }
  ) {
    drawer.drawCircularSelector(this, value, selectedColor, colors, valueMarkers, enabled)
  }
}

@SuplaComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      modifier = Modifier.background(Color.LightGray)
    ) {
      CircularColorSelector(
        value = 0.3f,
        selectedColor = Color.Gray,
        modifier = Modifier
          .height(350.dp)
          .fillMaxWidth()
      )
      CircularColorSelector(
        value = null,
        selectedColor = Color.Gray,
        modifier = Modifier
          .height(350.dp)
          .fillMaxWidth()
      )
      CircularColorSelector(
        value = null,
        selectedColor = Color.Gray,
        modifier = Modifier
          .height(350.dp)
          .fillMaxWidth(),
        valueMarkers = listOf(0f, 0.5f, 1f)
      )
    }
  }
}
