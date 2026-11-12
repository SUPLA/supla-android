package org.supla.android.ui.views.forms
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

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.supla.android.R
import org.supla.core.shared.extensions.forTrue

@Composable
fun NumberPicker(
  range: IntRange,
  selectedValue: Int = 0,
  enabled: Boolean = true,
  formatter: (Context, Int) -> String = { _, i -> "$i" },
  onValueChanged: (Int) -> Unit = { }
) {
  AndroidView(
    factory = { context ->
      val view = LayoutInflater.from(context).inflate(R.layout.inc_number_picker, null)

      val numberPicker = view.findViewById<NumberPicker>(R.id.inc_number_picker)
      numberPicker.minValue = range.first
      numberPicker.maxValue = range.last
      numberPicker.displayedValues = range.map { formatter(context, it) }.toTypedArray()
      numberPicker.value = selectedValue.coerceIn(range)
      numberPicker.enabled = enabled
      numberPicker.setOnValueChangedListener { _, _, newValue -> enabled.forTrue { onValueChanged(newValue) } }

      view
    },
    update = { view ->
      val numberPicker = view.findViewById<NumberPicker>(R.id.inc_number_picker)
      val formattedValues = range.map { formatter(view.context, it) }.toTypedArray()
      val rangeChanged = numberPicker.minValue != range.first || numberPicker.maxValue != range.last
      val valuesChanged = numberPicker.displayedValues?.contentEquals(formattedValues) != true

      numberPicker.enabled = enabled
      numberPicker.setOnValueChangedListener { _, _, newValue -> enabled.forTrue { onValueChanged(newValue) } }

      if (rangeChanged || valuesChanged) {
        val temporaryMin = minOf(numberPicker.minValue, range.first)
        val temporaryMax = maxOf(numberPicker.maxValue, range.last)
        numberPicker.displayedValues = null
        numberPicker.minValue = temporaryMin
        numberPicker.maxValue = temporaryMax
        numberPicker.value = selectedValue.coerceIn(range)
        numberPicker.minValue = range.first
        numberPicker.maxValue = range.last
        numberPicker.displayedValues = formattedValues
      } else if (numberPicker.value != selectedValue) {
        numberPicker.value = selectedValue.coerceIn(range)
      }
    }
  )
}

@Composable
fun BoxScope.NumberPickerHighlight() =
  Box(
    modifier = Modifier
      .height(40.dp)
      .fillMaxWidth()
      .background(
        color = colorResource(id = R.color.gray_lighter),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default))
      )
      .align(Alignment.Center)
  )

private var NumberPicker.enabled: Boolean
  get() = false
  set(value) {
    val color = if (value) R.color.on_surface else R.color.disabled

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      textColor = context.resources.getColor(color, null)
    } else {
      findEditText()?.setTextColor(context.resources.getColor(color, null))
    }
  }

private fun View.findEditText(): EditText? {
  if (this is EditText) return this

  if (this is ViewGroup) {
    for (index in 0 until childCount) {
      getChildAt(index).findEditText()?.let { return it }
    }
  }

  return null
}
