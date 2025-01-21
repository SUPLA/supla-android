package org.supla.android.usecases.channel.measurementsprovider
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

import org.supla.android.data.model.chart.AggregatedEntity
import org.supla.android.data.model.chart.AggregatedValue
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.source.local.entity.measurements.BaseHumidityEntity
import org.supla.android.extensions.toTimestamp

internal fun <T : BaseHumidityEntity> ChannelMeasurementsProvider.aggregatingHumidity(
  measurements: List<T>,
  aggregation: ChartDataAggregation
): List<AggregatedEntity> {
  if (aggregation == ChartDataAggregation.MINUTES) {
    return measurements
      .filter { it.humidity != null }
      .map { AggregatedEntity(it.date.toTimestamp(), AggregatedValue.Single(it.humidity!!)) }
  }

  return measurements
    .asSequence()
    .filter { it.humidity != null }
    .groupBy { item -> aggregation.aggregator(item) }
    .filter { group -> group.value.isNotEmpty() }
    .map { group ->
      AggregatedEntity(
        date = aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
        value = AggregatedValue.Single(
          value = group.value.map { it.humidity!! }.average().toFloat(),
          min = group.value.minOf { it.humidity!! },
          max = group.value.maxOf { it.humidity!! },
          open = group.value.firstOrNull()?.humidity,
          close = group.value.lastOrNull()?.humidity
        )
      )
    }
    .toList()
}
