package org.supla.android.extensions

import android.os.Bundle

fun Bundle.getLongOrNull(key: String): Long? {
  return if (containsKey(key)) {
    getLong(key, 0)
  } else {
    null
  }
}
