package org.supla.android.data.source.local.entity.measurements
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

import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.source.local.entity.custom.BalancedValue

fun List<ElectricityMeterLogEntity>.balanceHourly() =
  this
    .groupBy { item -> ChartDataAggregation.HOURS.aggregator(item) }
    .asSequence()
    .filter { group -> group.value.isNotEmpty() }
    .map { group ->
      val consumption =
        group.value.map { it.phase1Fae ?: 0f }.sum() +
          group.value.map { it.phase2Fae ?: 0f }.sum() +
          group.value.map { it.phase3Fae ?: 0f }.sum()
      val production =
        group.value.map { it.phase1Rae ?: 0f }.sum() +
          group.value.map { it.phase2Rae ?: 0f }.sum() +
          group.value.map { it.phase3Rae ?: 0f }.sum()
      val result = consumption - production

      BalancedValue(
        date = group.value.firstOrNull()!!.date,
        groupingString = group.value.firstOrNull()!!.groupingString,
        forwarded = if (result > 0) result else 0f,
        reversed = if (result < 0) -result else 0f
      )
    }
