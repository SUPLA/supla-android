package org.supla.android.lib
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

import kotlinx.serialization.Serializable
import org.supla.android.tools.UsedFromNativeCode

@Serializable
class SuplaChannelImpulseCounterValue internal constructor(
  val impulsesPerUnit: Int,
  val counter: Long,
  val calculatedValue: Double,
  val pricePerUnit: Double,
  val totalCost: Double,
  val currency: String,
  val unit: String?
) {

  @UsedFromNativeCode
  constructor(
    impulsesPerUnit: Int,
    counter: Long,
    calculatedValue: Long,
    totalCost: Int,
    pricePerUnit: Int,
    currency: String?,
    unit: String?
  ) : this(
    impulsesPerUnit = impulsesPerUnit,
    counter = counter,
    calculatedValue = calculatedValue / 1000.0,
    pricePerUnit = pricePerUnit / 10000.0,
    totalCost = totalCost / 100.0,
    currency = currency ?: "",
    unit = unit
  )
}
