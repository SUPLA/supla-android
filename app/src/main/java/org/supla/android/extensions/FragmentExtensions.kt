package org.supla.android.extensions

import android.util.TypedValue
import androidx.fragment.app.Fragment

context(Fragment)
fun Int.toPx(): Int {
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), resources.displayMetrics).toInt()
}
