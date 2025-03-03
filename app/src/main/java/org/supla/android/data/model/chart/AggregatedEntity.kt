package org.supla.android.data.model.chart
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

import org.supla.android.data.source.local.entity.custom.Phase

sealed interface AggregatedValue {
  val valueMin: Float
  val valueMax: Float

  data class Single(
    val value: Float,
    val min: Float? = null,
    val max: Float? = null,
    val open: Float? = null,
    val close: Float? = null
  ) : AggregatedValue {

    override val valueMin: Float
      get() = value

    override val valueMax: Float
      get() = value
  }

  data class Multiple(
    val values: FloatArray
  ) : AggregatedValue {

    override val valueMin: Float
      get() = values.filter { it < 0 }.sum()

    override val valueMax: Float
      get() = values.filter { it > 0 }.sum()

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Multiple

      return values.contentEquals(other.values)
    }

    override fun hashCode(): Int {
      return values.contentHashCode()
    }
  }

  data class WithPhase(
    val value: Float,
    val min: Float? = null,
    val max: Float? = null,
    val phase: Phase
  ) : AggregatedValue {
    override val valueMin: Float
      get() = value

    override val valueMax: Float
      get() = value
  }
}

data class AggregatedEntity(
  val date: Long,
  val value: AggregatedValue,
)
