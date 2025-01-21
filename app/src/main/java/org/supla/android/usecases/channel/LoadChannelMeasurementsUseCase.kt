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
import org.supla.android.usecases.channel.measurementsprovider.ChannelMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.ElectricityMeterMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.GeneralPurposeMeasurementMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.GeneralPurposeMeterMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.HumidityMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.ImpulseCounterMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.TemperatureAndHumidityMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.TemperatureMeasurementsProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelMeasurementsUseCase @Inject constructor(
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  temperatureMeasurementsProvider: TemperatureMeasurementsProvider,
  temperatureAndHumidityMeasurementsProvider: TemperatureAndHumidityMeasurementsProvider,
  generalPurposeMeasurementMeasurementsProvider: GeneralPurposeMeasurementMeasurementsProvider,
  generalPurposeMeterMeasurementsProvider: GeneralPurposeMeterMeasurementsProvider,
  electricityMeterMeasurementsProvider: ElectricityMeterMeasurementsProvider,
  humidityMeasurementsProvider: HumidityMeasurementsProvider,
  impulseCounterMeasurementsProvider: ImpulseCounterMeasurementsProvider
) {

  private val providers: List<ChannelMeasurementsProvider> = listOf(
    temperatureMeasurementsProvider,
    temperatureAndHumidityMeasurementsProvider,
    generalPurposeMeasurementMeasurementsProvider,
    generalPurposeMeterMeasurementsProvider,
    electricityMeterMeasurementsProvider,
    humidityMeasurementsProvider,
    impulseCounterMeasurementsProvider
  )

  operator fun invoke(remoteId: Int, spec: ChartDataSpec): Single<ChannelChartSets> =
    readChannelWithChildrenUseCase(remoteId)
      .toSingle()
      .flatMap {
        providers.forEach { provider ->
          if (provider.handle(it)) {
            return@flatMap provider.provide(it, spec)
          }
        }
        Single.error(IllegalArgumentException("Channel function not supported (${it.function}"))
      }
}
