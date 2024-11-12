package org.supla.android.extensions

val Any.TAG: String
  get() {
    val tag = javaClass.simpleName
    return if (tag.length <= 23) tag else tag.substring(0, 23)
  }

val Any?.isNull: Boolean
  get() = this == null

val Any?.isNotNull: Boolean
  get() = this != null
