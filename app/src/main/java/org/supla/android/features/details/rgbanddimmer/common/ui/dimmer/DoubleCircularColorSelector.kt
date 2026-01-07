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
fun DoubleCircularColorSelector(
  outerValue: Float?,
  outerSelectedColor: Color?,
  outerColors: List<Pair<Float, Color>>,
  innerValue: Float?,
  innerSelectedColor: Color?,
  innerColors: List<Pair<Float, Color>>,
  modifier: Modifier = Modifier,
  outerValueMarkers: List<Float> = emptyList(),
  innerValueMarkers: List<Float> = emptyList(),
  enabled: Boolean = true,
  onOuterValueChangeStarted: () -> Unit = {},
  onOuterValueChanging: (Float) -> Unit = {},
  onOuterValueChanged: () -> Unit = {},
  onInnerValueChangeStarted: () -> Unit = {},
  onInnerValueChanging: (Float) -> Unit = {},
  onInnerValueChanged: () -> Unit = {}
) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  val outerDrawer = remember {
    CircularColorSelectorDrawer(
      outerMarginDp = OUTER_SURFACE_WIDTH,
      surfaceColor = surfaceColor,
      onValueChangeStarted = onOuterValueChangeStarted,
      onValueChanging = onOuterValueChanging,
      onValueChanged = onOuterValueChanged
    )
  }
  val innerDrawer = remember {
    CircularColorSelectorDrawer(
      outerMarginDp = 50.dp,
      surfaceColor = surfaceColor,
      onValueChangeStarted = onInnerValueChangeStarted,
      onValueChanging = onInnerValueChanging,
      onValueChanged = onInnerValueChanged
    )
  }

  Canvas(
    modifier = modifier
      .pointerInput(enabled) {
        detectDragGestures(
          onDragStart = {
            outerDrawer.handleDragStart(this, enabled, it)
            innerDrawer.handleDragStart(this, enabled, it)
          },
          onDragEnd = {
            outerDrawer.handleDragEnd(enabled)
            innerDrawer.handleDragEnd(enabled)
          },
          onDragCancel = {
            outerDrawer.handleDragCancel()
            innerDrawer.handleDragCancel()
          },
          onDrag = { change, _ ->
            outerDrawer.handleDrag(this, change, enabled)
            innerDrawer.handleDrag(this, change, enabled)
          }
        )
      }
      .pointerInput(enabled) {
        detectTapGestures {
          outerDrawer.handleTap(this, enabled, it)
          innerDrawer.handleTap(this, enabled, it)
        }
      }
  ) {
    outerDrawer.drawCircularSelector(this, outerValue, outerSelectedColor, outerColors, outerValueMarkers, enabled)
    innerDrawer.drawCircularSelector(this, innerValue, innerSelectedColor, innerColors, innerValueMarkers, enabled)
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
      DoubleCircularColorSelector(
        outerValue = 0.3f,
        outerSelectedColor = Color.Gray,
        outerColors = listOf(Pair(0f, Color.White), Pair(1f, Color.Black)),
        innerValue = 0.5f,
        innerSelectedColor = Color.Gray,
        innerColors = listOf(Pair(0f, Color.White), Pair(1f, Color.Black)),
        modifier = Modifier
          .height(350.dp)
          .fillMaxWidth()
      )
      DoubleCircularColorSelector(
        outerValue = null,
        outerSelectedColor = Color.Gray,
        outerColors = listOf(Pair(0f, Color.White), Pair(1f, Color.Black)),
        innerValue = null,
        innerSelectedColor = Color.Gray,
        innerColors = listOf(Pair(0f, Color.White), Pair(1f, Color.Black)),
        modifier = Modifier
          .height(350.dp)
          .fillMaxWidth()
      )
      DoubleCircularColorSelector(
        outerValue = null,
        outerSelectedColor = Color.Gray,
        outerColors = listOf(Pair(0f, Color.White), Pair(1f, Color.Black)),
        innerValue = null,
        innerSelectedColor = Color.Gray,
        innerColors = listOf(Pair(0f, Color.White), Pair(1f, Color.Black)),
        modifier = Modifier
          .height(350.dp)
          .fillMaxWidth(),
        innerValueMarkers = listOf(0f, 0.5f, 1f)
      )
    }
  }
}
