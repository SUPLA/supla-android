package org.supla.android.ui.views.schedule
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

import android.content.res.Resources
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.toHour

private val textSize = 12.sp
private val textFont = FontFamily(Font(R.font.open_sans_regular))
private val textFontBold = FontFamily(Font(R.font.open_sans_bold))

data class DrawableText<T>(
  val value: T,
  val textLayoutResult: TextLayoutResult,
  var isCurrent: Boolean
) {

  companion object {
    fun get(dayOfWeek: DayOfWeek, resources: Resources, textMeasurer: TextMeasurer, isCurrentDay: Boolean): DrawableText<DayOfWeek> =
      DrawableText(
        value = dayOfWeek,
        textLayoutResult = labelText(resources.getString(dayOfWeek.shortText), textMeasurer, useBold = isCurrentDay),
        isCurrent = isCurrentDay
      )

    fun get(hour: Int, textMeasurer: TextMeasurer, isCurrentHour: Boolean): DrawableText<Int> =
      DrawableText(
        value = hour,
        textLayoutResult = labelText(hour.toHour(), textMeasurer, useBold = isCurrentHour),
        isCurrent = isCurrentHour
      )
  }
}

private fun labelText(text: String, textMeasurer: TextMeasurer, useBold: Boolean = false): TextLayoutResult {
  val annotatedString = buildAnnotatedString {
    withStyle(
      style = SpanStyle(
        fontSize = textSize,
        fontFamily = if (useBold) textFontBold else textFont
      )
    ) {
      append(text)
    }
  }
  return textMeasurer.measure(annotatedString)
}
