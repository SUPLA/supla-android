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

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R

private val textFont = FontFamily(Font(R.font.open_sans_semibold))

fun RecuperatorDrawScope.drawTextInRoundRect(
  text: String,
  offsetProvider: (width: Float, height: Float) -> Offset = { _, _ -> Offset.Zero }
) {
  val fontSizePx = 14f * scale
  val textLayout = textMeasurer.measure(
    text = text,
    style = TextStyle(
      fontSize = (fontSizePx / density / fontScale).sp,
      fontFamily = textFont
    )
  )
  val textHorizontalPadding = 8 * scale
  val textVerticalPadding = 4 * scale

  val textRectWidth = textLayout.size.width + 2 * textHorizontalPadding
  val textRectHeight = textLayout.size.height + 2 * textVerticalPadding
  val textRectRadius = textRectHeight / 2

  val offset = offsetProvider(textRectWidth, textRectHeight)
  val colors = colors

  withTransform({
    translate(left = offset.x, top = offset.y)
  }) {
    drawRoundRect(
      color = colors.background,
      size = Size(textRectWidth, textRectHeight),
      cornerRadius = CornerRadius(textRectRadius, textRectRadius),
      style = Fill
    )

    drawRoundRect(
      color = colors.outline,
      size = Size(textRectWidth, textRectHeight),
      cornerRadius = CornerRadius(textRectRadius, textRectRadius),
      style = Stroke(width = 1.dp.toPx())
    )

    drawText(
      textLayoutResult = textLayout,
      color = colors.onSurface,
      topLeft = Offset(textHorizontalPadding, textVerticalPadding)
    )
  }
}
