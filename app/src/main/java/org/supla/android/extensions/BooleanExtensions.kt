package org.supla.android.extensions

fun <T> Boolean.ifTrue(value: T): T? = if (this) {
  value
} else {
  null
}

fun <T> Boolean.ifFalse(value: T): T? = if (this.not()) {
  value
} else {
  null
}

fun ifTrue(value: Boolean, callback: () -> Unit) {
  if (value) {
    callback()
  }
}
