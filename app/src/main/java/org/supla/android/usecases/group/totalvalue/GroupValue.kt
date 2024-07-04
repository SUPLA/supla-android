package org.supla.android.usecases.group.totalvalue
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

sealed class GroupValue {
  abstract val values: List<Value<*>>

  fun asString(): String = values.joinToString(separator = SEPARATOR)

  companion object {
    const val SEPARATOR = ":"
  }
}

data class ShadingSystemGroupValue(
  val position: Int,
  val closeSensorActive: Boolean
) : GroupValue() {

  override val values: List<Value<*>>
    get() = listOf(
      IntegerValue(position),
      BooleanValue(closeSensorActive)
    )

  companion object {
    operator fun invoke(stringValue: String): ShadingSystemGroupValue {
      val values = stringValue.split(SEPARATOR)
      if (values.size != 2) {
        throw IllegalArgumentException("Given string value is not correct `$stringValue`")
      }
      return ShadingSystemGroupValue(values[0].toInt(), values[1].toInt() == 1)
    }
  }
}

data class ProjectorScreenGroupValue(
  val position: Int
) : GroupValue() {

  override val values: List<Value<*>>
    get() = listOf(IntegerValue(position))

  companion object {
    operator fun invoke(stringValue: String): ProjectorScreenGroupValue {
      val values = stringValue.split(SEPARATOR)
      if (values.size != 1) {
        throw IllegalArgumentException("Given string value is not correct `$stringValue`")
      }
      return ProjectorScreenGroupValue(values[0].toInt())
    }
  }
}

data class ShadowingBlindGroupValue(
  val position: Int,
  val tilt: Int
) : GroupValue() {
  override val values: List<Value<*>>
    get() = listOf(
      IntegerValue(position),
      IntegerValue(tilt)
    )

  companion object {
    operator fun invoke(stringValue: String): ShadowingBlindGroupValue {
      val values = stringValue.split(SEPARATOR)
      if (values.size != 2) {
        throw IllegalArgumentException("Given string value is not correct `$stringValue`")
      }
      return ShadowingBlindGroupValue(values[0].toInt(), values[1].toInt())
    }
  }
}

data class OpenedClosedGroupValue(
  val active: Boolean // active state normally means closed or on
) : GroupValue() {
  override val values: List<Value<*>>
    get() = listOf(BooleanValue(active))

  companion object {
    operator fun invoke(stringValue: String): OpenedClosedGroupValue =
      if (stringValue.trim() == "0") {
        OpenedClosedGroupValue(false)
      } else if (stringValue == "1") {
        OpenedClosedGroupValue(true)
      } else {
        throw IllegalArgumentException("Given string value is not correct `$stringValue`")
      }
  }
}

data class DimmerGroupValue(
  val brightness: Int
) : GroupValue() {
  override val values: List<Value<*>>
    get() = listOf(IntegerValue(brightness))

  companion object {
    operator fun invoke(stringValue: String): DimmerGroupValue =
      try {
        DimmerGroupValue(stringValue.toInt())
      } catch (exception: Exception) {
        throw IllegalArgumentException("Given string value is not correct `$stringValue`", exception)
      }
  }
}

data class RgbGroupValue(
  val color: Int,
  val brightness: Int
) : GroupValue() {
  override val values: List<Value<*>>
    get() = listOf(
      IntegerValue(color),
      IntegerValue(brightness)
    )

  companion object {
    operator fun invoke(stringValue: String): RgbGroupValue {
      val values = stringValue.split(SEPARATOR)
      if (values.size != 2) {
        throw IllegalArgumentException("Given string value is not correct `$stringValue`")
      }
      return RgbGroupValue(values[0].toInt(), values[1].toInt())
    }
  }
}

data class DimmerAndRgbGroupValue(
  val color: Int,
  val brightnessColor: Int,
  val brightness: Int
) : GroupValue() {
  override val values: List<Value<*>>
    get() = listOf(
      IntegerValue(color),
      IntegerValue(brightnessColor),
      IntegerValue(brightness)
    )

  companion object {
    operator fun invoke(stringValue: String): DimmerAndRgbGroupValue {
      val values = stringValue.split(SEPARATOR)
      if (values.size != 3) {
        throw IllegalArgumentException("Given string value is not correct `$stringValue`")
      }
      return DimmerAndRgbGroupValue(values[0].toInt(), values[1].toInt(), values[2].toInt())
    }
  }
}

data class HeatpolThermostatGroupValue(
  val isOn: Boolean,
  val measuredTemperature: Float,
  val presetTemperature: Float
) : GroupValue() {
  override val values: List<Value<*>>
    get() = listOf(
      BooleanValue(isOn),
      FloatValue(measuredTemperature),
      FloatValue(presetTemperature)
    )

  companion object {
    operator fun invoke(stringValue: String): HeatpolThermostatGroupValue {
      val values = stringValue.split(SEPARATOR)
      if (values.size != 3) {
        throw IllegalArgumentException("Given string value is not correct `$stringValue`")
      }
      return HeatpolThermostatGroupValue(values[0] == "1", values[1].toFloat(), values[2].toFloat())
    }
  }
}

class GeneralGroupValue(vararg staticValues: Value<*>) : GroupValue() {
  override val values: List<Value<*>> = staticValues.asList()
}
