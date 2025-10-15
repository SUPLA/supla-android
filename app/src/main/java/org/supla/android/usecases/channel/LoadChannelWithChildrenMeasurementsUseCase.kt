package org.supla.android.usecases.channel
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

import io.reactivex.rxjava3.core.Single
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HumidityChartColors
import org.supla.android.data.model.chart.TemperatureChartColors
import org.supla.android.data.source.local.entity.complex.isHvacThermostat
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.hasMeasurements
import org.supla.android.usecases.channel.measurementsprovider.TemperatureAndHumidityMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.TemperatureMeasurementsProvider
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelWithChildrenMeasurementsUseCase @Inject constructor(
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val temperatureMeasurementsProvider: TemperatureMeasurementsProvider,
  private val temperatureAndHumidityMeasurementsProvider: TemperatureAndHumidityMeasurementsProvider
) {

  operator fun invoke(
    remoteId: Int,
    spec: ChartDataSpec
  ): Single<List<ChannelChartSets>> =
    readChannelWithChildrenUseCase(remoteId)
      .toSingle()
      .flatMap {
        if (it.channel.isHvacThermostat()) {
          buildDataSets(it, spec)
        } else {
          Single.error(IllegalArgumentException("Channel function not supported (${it.channel.channelEntity.function}"))
        }
      }

  private fun buildDataSets(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec
  ): Single<List<ChannelChartSets>> {
    val channelsWithMeasurements = mutableListOf<ChannelWithChildren>().also { list ->
      list.addAll(
        channelWithChildren.children
          .sortedBy { it.relationType.value }
          .filter { it.channel.hasMeasurements() }
          .map { it.withChildren }
      )
      if (channelWithChildren.channel.channelEntity.hasMeasurements()) {
        list.add(channelWithChildren)
      }
    }

    val temperatureColors = TemperatureChartColors()
    val humidityColors = HumidityChartColors()
    val observables = mutableListOf<Single<ChannelChartSets>>().also { list ->
      channelsWithMeasurements.forEach {
        if (it.function == SuplaFunction.THERMOMETER) {
          val color = temperatureColors.nextColor()
          list.add(temperatureMeasurementsProvider.provide(it, spec) { color })
        } else if (it.function == SuplaFunction.HUMIDITY_AND_TEMPERATURE) {
          val firstColor = temperatureColors.nextColor()
          val secondColor = humidityColors.nextColor()
          list.add(
            temperatureAndHumidityMeasurementsProvider.provide(it, spec) { type ->
              if (type == ChartEntryType.HUMIDITY) secondColor else firstColor
            }
          )
        }
      }
    }

    return Single.zip(observables) { items ->
      mutableListOf<ChannelChartSets>().also { list ->
        items.forEach {
          (it as? ChannelChartSets)?.let { channelSets -> list.add(channelSets) }
        }
      }
    }
  }
}
