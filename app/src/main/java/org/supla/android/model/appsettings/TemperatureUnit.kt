package org.supla.android.model.appsettings

enum class TemperatureUnit {
  CELSIUS, FAHRENHEIT;

  fun position(): Int {
    for ((position, unit) in TemperatureUnit.values().withIndex()) {
      if (unit == this) {
        return position
      }
    }

    throw IllegalStateException("Position not found!")
  }

  companion object {
    fun forPosition(position: Int): TemperatureUnit {
      for ((i, unit) in TemperatureUnit.values().withIndex()) {
        if (i == position) {
          return unit
        }
      }

      throw IllegalStateException("Position not found!")
    }
  }
}
