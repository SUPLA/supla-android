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

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.compose.runtime.Composable
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.supla.android.core.ui.BitmapProvider
import org.supla.android.data.model.chart.marker.ChartEntryDetails
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter

data class HistoryDataSet(
  val type: ChartEntryType,
  val label: Label,
  val valueFormatter: ChannelValueFormatter,
  val active: Boolean = true,
  val entities: List<List<AggregatedEntity>> = emptyList()
) {

  val min: Float?
    get() = entities.minOfOrNull { list -> list.minOf { it.value.valueMin } }

  val max: Float?
    get() = entities.maxOfOrNull { list -> list.maxOf { it.value.valueMax } }

  val isEmpty: Boolean
    get() = entities.isEmpty()

  val minDate: Long
    get() = entities.minOfOrNull { list -> list.minOfOrNull { it.date } ?: 0L } ?: 0L

  val maxDate: Long
    get() = entities.maxOfOrNull { list -> list.maxOfOrNull { it.date } ?: 0L } ?: 0L

  fun asLineChartData(
    aggregation: ChartDataAggregation,
    timeToCoordinateConverter: ((Float) -> Float)? = null,
    toSetConverter: (set: List<Entry>) -> LineDataSet
  ): List<ILineDataSet>? {
    if (!active || entities.isEmpty()) {
      return null
    }

    return mutableListOf<ILineDataSet>().apply {
      entities.forEach { aggregatedEntities ->
        val entries = aggregatedEntities.map { entity ->
          val x = timeToCoordinateConverter?.let { it(entity.date.toFloat()) } ?: entity.date.toFloat()
          toLineEntry(x, aggregation, entity)
        }

        add(toSetConverter(entries))
      }
    }
  }

  fun asBarChartData(
    aggregation: ChartDataAggregation,
    customData: Any? = null,
    timeToCoordinateConverter: ((Float) -> Float)? = null,
    toSetConverter: (set: List<BarEntry>) -> BarDataSet
  ): List<IBarDataSet>? {
    if (!active || entities.isEmpty()) {
      return null
    }

    return mutableListOf<IBarDataSet>().apply {
      entities.forEach { aggregatedEntities ->
        val entries = aggregatedEntities.map { entity ->
          val x = timeToCoordinateConverter?.let { it(entity.date.toFloat()) } ?: entity.date.toFloat()
          toBarEntry(x, aggregation, entity, customData)
        }

        add(toSetConverter(entries))
      }
    }
  }

  fun asCandleChartData(
    aggregation: ChartDataAggregation,
    timeToCoordinateConverter: ((Float) -> Float)? = null,
    toSetConverter: (set: List<CandleEntry>) -> CandleDataSet
  ): List<ICandleDataSet>? {
    if (!active || entities.isEmpty()) {
      return null
    }

    return mutableListOf<ICandleDataSet>().apply {
      entities.forEach { aggregatedEntities ->
        val entries = aggregatedEntities.map { entity ->
          val x = timeToCoordinateConverter?.let { it(entity.date.toFloat()) } ?: entity.date.toFloat()
          toCandleEntry(x, aggregation, entity)
        }

        add(toSetConverter(entries))
      }
    }
  }

  private fun toLineEntry(x: Float, aggregation: ChartDataAggregation, entity: AggregatedEntity): Entry =
    Entry(x, (entity.value as AggregatedValue.Single).value, chartEntryDetails(aggregation, entity))

  private fun toBarEntry(
    x: Float,
    aggregation: ChartDataAggregation,
    entity: AggregatedEntity,
    customData: Any? = null
  ): BarEntry =
    when (val value = entity.value) {
      is AggregatedValue.Single -> BarEntry(x, value.value, chartEntryDetails(aggregation, entity, customData))
      is AggregatedValue.Multiple -> BarEntry(x, value.values, chartEntryDetails(aggregation, entity, customData))
    }

  private fun toCandleEntry(x: Float, aggregation: ChartDataAggregation, entity: AggregatedEntity): CandleEntry {
    val value = (entity.value as AggregatedValue.Single)
    val min = value.min ?: value.value
    val max = value.max ?: value.value
    val open = value.open ?: value.value
    val close = value.close ?: value.value
    return CandleEntry(x, max, min, open, close, chartEntryDetails(aggregation, entity))
  }

  private fun chartEntryDetails(aggregation: ChartDataAggregation, entity: AggregatedEntity, customData: Any? = null) =
    when (val value = entity.value) {
      is AggregatedValue.Single ->
        ChartEntryDetails(aggregation, type, entity.date, value.min, value.max, value.open, value.close, valueFormatter, customData)

      is AggregatedValue.Multiple ->
        ChartEntryDetails(aggregation, type, entity.date, null, null, null, null, valueFormatter, customData)
    }

  sealed interface Label {
    data class Single(val value: LabelData) : Label
    data class Multiple(val values: List<LabelData>) : Label {
      fun colors(resources: Resources): List<Int> =
        mutableListOf<Int>().apply {
          values.forEach { labelData ->
            if (labelData.useColor) {
              add(ResourcesCompat.getColor(resources, labelData.color, null))
            }
          }
        }
    }
  }

  data class LabelData(
    val iconProvider: BitmapProvider?,
    val value: String,
    @ColorRes val color: Int,
    val presentColor: Boolean = true,
    val useColor: Boolean = true,
    val justColor: Boolean = false,
    @DimenRes val iconSize: Int? = null
  ) {
    @Composable
    fun Icon(context: Context, content: @Composable (Bitmap) -> Unit) {
      iconProvider?.let {
        it(context)?.let { bitmap -> content(bitmap) }
      }
    }

    companion object {
      operator fun invoke(@ColorRes color: Int): LabelData =
        LabelData({ null }, "", color, presentColor = false, justColor = true)
    }
  }
}

fun singleLabel(
  iconProvider: BitmapProvider,
  value: String,
  @ColorRes color: Int
): HistoryDataSet.Label.Single =
  HistoryDataSet.Label.Single(HistoryDataSet.LabelData(iconProvider, value, color))
