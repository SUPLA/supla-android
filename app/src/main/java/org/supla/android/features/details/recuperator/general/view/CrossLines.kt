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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

private const val CROSS_PATH = "M56.5684 16.9707L45.2549 28.2842L56.5684 39.5977L39.5977 56.5684L28.2842 45.2549L16.9707 56.5684L0 " +
  "39.5977L11.3135 28.2842L0 16.9707L16.9707 0L28.2842 11.3135L39.5977 0L56.5684 16.9707Z"
private const val ARROW_PATH = "M0 60.3008H22L78 8.30078H102M96 16.3008L102 8.30078L96 0.300781"

private val crossPathParser = PathParser().parsePathString(CROSS_PATH)
private val arrowPathParser = PathParser().parsePathString(ARROW_PATH)

fun RecuperatorDrawScope.drawCrossLines(scaleMatrix: Matrix): Triple<Float, Float, Rect> {
  val colors = colors
  val scale = scale

  val crossPath = crossPathParser.toPath()
  crossPath.transform(scaleMatrix)
  withTransform({
    translate(left = 145 * scale, top = 72 * scale)
  }) {
    drawPath(path = crossPath, color = colors.outline, style = Fill)
  }

  val arrowPath = arrowPathParser.toPath()
  arrowPath.transform(scaleMatrix)

  val arrowLeft = 122f * scale
  val arrowTop = 66f * scale
  val bounds = arrowPath.getBounds()
  withTransform({
    translate(left = arrowLeft, top = arrowTop)
  }) {
    val brush = Brush.linearGradient(
      colors = listOf(colors.error, colors.secondary),
      start = Offset.Zero,
      end = Offset(bounds.width, 0f)
    )
    val stroke = Stroke(
      width = 4.dp.toPx(),
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
    drawPath(path = arrowPath, brush = brush, style = stroke)

    arrowPath.transform(
      Matrix().apply {
        scale(-1f, 1f)
        translate(-bounds.width, 0f)
      }
    )
    drawPath(path = arrowPath, brush = brush, style = stroke)
  }

  return Triple(arrowLeft, arrowTop, bounds)
}
