package org.supla.android.ui.views
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
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import org.supla.android.R

@Composable
fun NumberPicker(
  range: IntRange,
  selectedValue: Int = 0,
  formatter: (Context, Int) -> String = { _, i -> "$i" },
  onValueChanged: (Int) -> Unit = { }
) {
  AndroidView(factory = { context ->
    val view = LayoutInflater.from(context).inflate(R.layout.inc_number_picker, null)

    val numberPicker = view.findViewById<NumberPicker>(R.id.inc_number_picker)
    numberPicker.displayedValues =
      mutableListOf<String>().also {
        for (i in range) {
          it.add(formatter(context, i))
        }
      }.toTypedArray()
    numberPicker.minValue = range.first
    numberPicker.maxValue = range.last
    numberPicker.value = selectedValue
    numberPicker.setOnValueChangedListener { _, _, newValue -> onValueChanged(newValue) }

    return@AndroidView view
  })
}
