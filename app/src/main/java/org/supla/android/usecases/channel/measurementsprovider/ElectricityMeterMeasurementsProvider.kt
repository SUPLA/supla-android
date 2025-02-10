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
import org.supla.android.Preferences
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterChartType
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.measurementsprovider.electricity.CurrentMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.electricity.ElectricityChartFilters
import org.supla.android.usecases.channel.measurementsprovider.electricity.ElectricityConsumptionProvider
import org.supla.android.usecases.channel.measurementsprovider.electricity.PowerActiveMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.electricity.VoltageMeasurementsProvider
import org.supla.android.usecases.icon.GetChannelIconUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ElectricityMeterMeasurementsProvider @Inject constructor(
  private val electricityConsumptionProvider: ElectricityConsumptionProvider,
  private val voltageMeasurementsProvider: VoltageMeasurementsProvider,
  private val currentMeasurementsProvider: CurrentMeasurementsProvider,
  private val powerActiveMeasurementsProvider: PowerActiveMeasurementsProvider,
  getChannelIconUseCase: GetChannelIconUseCase,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  preferences: Preferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : ChannelMeasurementsProvider(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {

  override fun handle(channelWithChildren: ChannelWithChildren) = channelWithChildren.isOrHasElectricityMeter

  override fun provide(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)?
  ): Single<ChannelChartSets> {
    return when ((spec.customFilters as? ElectricityChartFilters)?.type) {
      ElectricityMeterChartType.VOLTAGE -> voltageMeasurementsProvider(channelWithChildren, spec)
      ElectricityMeterChartType.CURRENT -> currentMeasurementsProvider(channelWithChildren, spec)
      ElectricityMeterChartType.POWER_ACTIVE -> powerActiveMeasurementsProvider(channelWithChildren, spec)
      else -> electricityConsumptionProvider(channelWithChildren, spec)
    }
  }
}
