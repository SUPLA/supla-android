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

@JvmInline
value class ValuePrecision internal constructor(private val value: Int) {

  val min: Int
    get() = value.shr(SHIFT).and(MASK)

  val max: Int
    get() = value.and(MASK)

  companion object {
    fun exact(value: Int): ValuePrecision = pack(value, value)
    fun atMost(value: Int): ValuePrecision = pack(0, value)
    fun between(min: Int, max: Int): ValuePrecision = pack(min, max)

    private fun pack(min: Int, max: Int): ValuePrecision {
      return ValuePrecision(max.and(MASK).or(min.and(MASK).shl(SHIFT)))
    }

    private const val MASK = 0xFFFF
    private const val SHIFT = 16
  }
}

val DefaultPrecision = ValuePrecision.exact(1)

val PressurePrecision = ValuePrecision.exact(0)
val GpmPrecision = ValuePrecision.exact(2)
val VoltagePrecision = ValuePrecision.exact(2)
val RainPrecision = ValuePrecision.exact(2)
val WeightPrecision = ValuePrecision.exact(2)
val ImpulseCounterPrecision = ValuePrecision.exact(3)

val DistanceMilliPrecision = ValuePrecision.exact(0)
val DistanceCentiPrecision = ValuePrecision.exact(1)
val DistanceDefaultPrecision = ValuePrecision.exact(2)
