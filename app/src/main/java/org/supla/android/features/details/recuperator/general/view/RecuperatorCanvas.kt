package org.supla.android.features.details.recuperator.general.view
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

class RecuperatorDrawScope(
  private val drawScope: DrawScope,
  val textMeasurer: TextMeasurer,
  val colors: Colors,
  val scale: Float
) : DrawScope by drawScope

@Composable
fun RecuperatorCanvas(
  modifier: Modifier = Modifier,
  onDraw: RecuperatorDrawScope.() -> Unit
) {
  Layout(
    modifier = modifier,
    content = {
      val textMeasurer = rememberTextMeasurer()
      val colors = Colors.create()

      Canvas(modifier = Modifier.fillMaxSize()) {
        val vectorHeight = 156f
        val scale = size.height / vectorHeight

        val scope = RecuperatorDrawScope(
          drawScope = this,
          textMeasurer = textMeasurer,
          colors = colors,
          scale = scale
        )

        with(scope) {
          onDraw()
        }
      }
    }
  ) { measurables, constraints ->

    val maxW = constraints.maxWidth
    val maxH = constraints.maxHeight

    val widthFromHeight = maxH.times(1.9).roundToInt()

    val (width, height) =
      if (widthFromHeight <= maxW) {
        Pair(widthFromHeight, maxH)
      } else {
        Pair(maxW, maxW / 2)
      }

    val placeable = measurables.first().measure(
      Constraints.fixed(width, height)
    )

    layout(width, height) {
      placeable.placeRelative(0, 0)
    }
  }
}

data class Colors(
  val background: Color,
  val outline: Color,
  val error: Color,
  val secondary: Color,
  val onSurface: Color
) {
  companion object {
    @Composable
    fun create() = Colors(
      background = MaterialTheme.colorScheme.background,
      outline = MaterialTheme.colorScheme.outline,
      error = MaterialTheme.colorScheme.error,
      secondary = MaterialTheme.colorScheme.secondary,
      onSurface = MaterialTheme.colorScheme.onSurface
    )
  }
}
