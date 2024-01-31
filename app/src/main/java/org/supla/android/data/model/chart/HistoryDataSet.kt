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

import androidx.annotation.ColorRes
import org.supla.android.core.ui.BitmapProvider
import org.supla.android.data.model.chart.marker.ChartEntryDetails
import org.supla.android.usecases.channel.AggregatedEntity
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter

data class HistoryDataSet(
  val setId: Id,
  val iconProvider: BitmapProvider,
  val value: String,
  val valueFormatter: ChannelValueFormatter,
  @ColorRes val color: Int,
  val entities: List<List<AggregatedEntity>> = emptyList(),
  val active: Boolean = true
) {

  fun toDetails(entity: AggregatedEntity) =
    ChartEntryDetails(
      entity.aggregation,
      entity.type,
      entity.date,
      entity.min,
      entity.max,
      entity.open,
      entity.close,
      valueFormatter
    )

  data class Id(
    val remoteId: Int,
    val type: ChartEntryType
  )
}
