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

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment

@Suppress("UnusedReceiverParameter")
fun Fragment.clearEdgeToEdgePaddings(view: View) {
  ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
    view.setPadding(0, 0, 0, 0)
    insets
  }
}

interface IntConverter {
  fun requireContext(): Context

  fun Int.toPx(): Int {
    return toPx(requireContext())
  }
}

fun Int.toPx(context: Context): Int {
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).toInt()
}
