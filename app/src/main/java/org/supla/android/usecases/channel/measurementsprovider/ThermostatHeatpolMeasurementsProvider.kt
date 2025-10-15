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

import com.google.gson.Gson
import io.reactivex.rxjava3.core.Single
import org.supla.android.R
import org.supla.android.core.shared.provider
import org.supla.android.core.shared.shareable
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.chart.HumidityChartColors
import org.supla.android.data.model.chart.TemperatureChartColors
import org.supla.android.data.source.HomePlusThermostatLogRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.GetChannelValueUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.function.thermostat.HomePlusThermostatValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ThermostatHeatpolMeasurementsProvider @Inject constructor(
  private val homePlusThermostatLogRepository: HomePlusThermostatLogRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getChannelValueUseCase: GetChannelValueUseCase,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  preferences: ApplicationPreferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : ChannelMeasurementsProvider(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {
  override fun handle(channelWithChildren: ChannelWithChildren) =
    channelWithChildren.channel.function == SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS

  override fun provide(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)?
  ): Single<ChannelChartSets> {
    val channel = channelWithChildren.channel

    return homePlusThermostatLogRepository.findMeasurements(channel.remoteId, channel.profileId, spec.startDate, spec.endDate)
      .map { entities ->
        val formatter = getValueFormatter(ChartEntryType.TEMPERATURE, channelWithChildren)
        val value: HomePlusThermostatValue = getChannelValueUseCase(channelWithChildren)
        listOf(
          historyDataSet(
            channelWithChildren = channelWithChildren,
            type = ChartEntryType.TEMPERATURE,
            aggregation = spec.aggregation,
            measurements = aggregatingTemperature(entities, spec.aggregation),
            label = measuredTemperatureLabel(channel, formatter, value.measuredTemperature)
          ),
          historyDataSet(
            channelWithChildren = channelWithChildren,
            type = ChartEntryType.PRESET_TEMPERATURE,
            aggregation = spec.aggregation,
            measurements = aggregatingTemperature(entities, spec.aggregation) { it?.presetTemperature },
            label = presetTemperatureLabel(formatter, value.presetTemperature)
          )
        )
      }
      .map {
        ChannelChartSets(
          channel.remoteId,
          channel.function,
          getCaptionUseCase(channel.shareable).provider(),
          spec.aggregation,
          it
        )
      }
      .firstOrError()
  }

  private fun measuredTemperatureLabel(channel: ChannelDataEntity, formatter: ValueFormatter, value: Float) =
    HistoryDataSet.Label.Single(
      HistoryDataSet.LabelData(
        imageId = getChannelIconUseCase(channel),
        value = formatter.format(value, ValueFormat.WithUnit),
        color = TemperatureChartColors.DEFAULT,
        description = localizedString(R.string.temperature_measured)
      )
    )

  private fun presetTemperatureLabel(formatter: ValueFormatter, value: Float) =
    HistoryDataSet.Label.Single(
      HistoryDataSet.LabelData(
        imageId = null,
        value = formatter.format(value, ValueFormat.WithUnit),
        color = HumidityChartColors.DEFAULT,
        description = localizedString(R.string.temperature_preset)
      )
    )
}
