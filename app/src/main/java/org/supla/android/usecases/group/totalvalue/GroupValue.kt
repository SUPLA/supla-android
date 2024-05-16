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
  val openSensorActive: Boolean
) : GroupValue() {

  override val values: List<Value<*>>
    get() = listOf(
      IntegerValue(position),
      BooleanValue(openSensorActive)
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

data class FacadeBlindGroupValue(
  val position: Int,
  val tilt: Int
) : GroupValue() {
  override val values: List<Value<*>>
    get() = listOf(
      IntegerValue(position),
      IntegerValue(tilt)
    )

  companion object {
    operator fun invoke(stringValue: String): FacadeBlindGroupValue {
      val values = stringValue.split(SEPARATOR)
      if (values.size != 2) {
        throw IllegalArgumentException("Given string value is not correct `$stringValue`")
      }
      return FacadeBlindGroupValue(values[0].toInt(), values[1].toInt())
    }
  }
}

class GeneralGroupValue(vararg staticValues: Value<*>) : GroupValue() {

  override val values: List<Value<*>>

  init {
    values = staticValues.asList()
  }
}
