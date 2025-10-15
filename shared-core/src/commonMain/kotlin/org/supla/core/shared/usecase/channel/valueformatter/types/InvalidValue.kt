package org.supla.core.shared.usecase.channel.valueformatter.types
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

import kotlin.jvm.JvmInline

sealed interface InvalidValue {
  val value: Double
  fun isEqualTo(other: Double): Boolean

  @JvmInline
  value class Equals(override val value: Double) : InvalidValue {
    override fun isEqualTo(other: Double): Boolean = value == other
  }

  @JvmInline
  value class LowerThan(override val value: Double) : InvalidValue {
    override fun isEqualTo(other: Double): Boolean = other < value
  }

  @JvmInline
  value class NanOrLowerThan(override val value: Double) : InvalidValue {
    override fun isEqualTo(other: Double): Boolean = other < value || other.isNaN()
  }

  @JvmInline
  value class BiggerThan(override val value: Double) : InvalidValue {
    override fun isEqualTo(other: Double): Boolean = other > value
  }

  @JvmInline
  value class LowerThanOrEqual(override val value: Double) : InvalidValue {
    override fun isEqualTo(other: Double): Boolean = other <= value
  }

  @JvmInline
  value class NanOrLowerThanOrEqual(override val value: Double) : InvalidValue {
    override fun isEqualTo(other: Double): Boolean = other <= value || other.isNaN()
  }

  @JvmInline
  value class BiggerThanOrEqual(override val value: Double) : InvalidValue {
    override fun isEqualTo(other: Double): Boolean = other >= value
  }

  data class ValueOrNan(override val value: Double) : InvalidValue {
    override fun isEqualTo(other: Double): Boolean =
      value == other || other.isNaN()
  }

  data object NaN : InvalidValue {
    override val value: Double = Double.NaN
    override fun isEqualTo(other: Double): Boolean = other.isNaN()
  }

  data object None : InvalidValue {
    override val value: Double = 0.0
    override fun isEqualTo(other: Double): Boolean = false
  }

  companion object {
    val Thermometer = LowerThanOrEqual(-273.0)
    val Humidity = LowerThanOrEqual(-1.0)
    val ImpulseCounter = Equals(0.0)
    val ElectricityMeter = ValueOrNan(0.0)
    val Pressure = LowerThanOrEqual(-1.0)
    val Rain = LowerThanOrEqual(-1.0)
    val Wind = NanOrLowerThanOrEqual(-1.0)
    val Weight = NanOrLowerThanOrEqual(-1.0)
    val Distance = NanOrLowerThan(0.0)
  }
}
