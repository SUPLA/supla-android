package org.supla.android.extensions

import android.view.View

fun View.visibleIf(visible: Boolean) {
  visibility = if (visible) View.VISIBLE else View.GONE
}
