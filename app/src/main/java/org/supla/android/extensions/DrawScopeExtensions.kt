package org.supla.android.extensions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.data.ValuesFormatter

private val textFont = FontFamily(Font(R.font.open_sans_semibold))

val DrawScope.defaultFont: FontFamily
  get() = textFont

fun DrawScope.drawPercentage(
  value: Float,
  textMeasurer: TextMeasurer,
  color: Color,
  center: Offset = this.center,
  font: FontFamily = textFont,
  textSize: Dp = 15.dp
) {
  drawText(
    text = ValuesFormatter.getPercentageString(value),
    textMeasurer = textMeasurer,
    color = color,
    center = center,
    font = font,
    textSize = textSize
  )
}

fun DrawScope.drawText(
  text: String,
  textMeasurer: TextMeasurer,
  color: Color,
  center: Offset = this.center,
  font: FontFamily = textFont,
  textSize: Dp = 15.dp
) {
  val textLayout = textMeasurer.measure(
    text = text,
    style = TextStyle(
      fontSize = (textSize.toPx() / density / fontScale).sp,
      fontFamily = font
    )
  )
  drawText(
    textLayoutResult = textLayout,
    color = color,
    topLeft = Offset(
      x = center.x - textLayout.size.width.div(2f),
      y = center.y - textLayout.size.height.div(2f)
    )
  )
}
