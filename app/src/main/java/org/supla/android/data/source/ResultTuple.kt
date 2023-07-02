package org.supla.android.data.source

class ResultTuple(vararg results: Any) {
  private val results = results.asList()

  fun asBoolean(itemNo: Int): Boolean? {
    if (itemNo < results.size) {
      return results[itemNo] as? Boolean
    }

    return null
  }
}