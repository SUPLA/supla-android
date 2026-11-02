package org.supla.android.usecases.channel

private const val VALUE_HIDDEN = 0
private const val VALUE_VISIBLE = 1
private const val VALUE_VISIBLE_PROCESSING = 2

enum class VisibilityChange(val newVisibility: Int, val applyForVisibility: Int) {
  PROCESSING_TO_HIDE(VALUE_HIDDEN, VALUE_VISIBLE_PROCESSING),
  VISIBLE_TO_PROCESSING(VALUE_VISIBLE_PROCESSING, VALUE_VISIBLE)
}
