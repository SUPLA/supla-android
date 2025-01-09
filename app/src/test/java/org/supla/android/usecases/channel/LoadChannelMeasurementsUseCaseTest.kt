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

import android.annotation.SuppressLint
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.usecases.channel.measurementsprovider.ElectricityMeterMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.GeneralPurposeMeasurementMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.GeneralPurposeMeterMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.HumidityMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.ImpulseCounterMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.TemperatureAndHumidityMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.TemperatureMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.ThermostatHeatpolMeasurementsProvider
import org.supla.core.shared.data.model.general.SuplaFunction

class LoadChannelMeasurementsUseCaseTest : BaseLoadMeasurementsUseCaseTest() {

  @MockK
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @MockK
  private lateinit var temperatureMeasurementsProvider: TemperatureMeasurementsProvider

  @MockK
  private lateinit var temperatureAndHumidityMeasurementsProvider: TemperatureAndHumidityMeasurementsProvider

  @MockK
  private lateinit var generalPurposeMeasurementMeasurementsProvider: GeneralPurposeMeasurementMeasurementsProvider

  @MockK
  private lateinit var generalPurposeMeterMeasurementsProvider: GeneralPurposeMeterMeasurementsProvider

  @MockK
  private lateinit var electricityMeterMeasurementsProvider: ElectricityMeterMeasurementsProvider

  @MockK
  private lateinit var humidityMeasurementsProvider: HumidityMeasurementsProvider

  @MockK
  private lateinit var impulseCounterMeasurementsProvider: ImpulseCounterMeasurementsProvider

  @MockK
  private lateinit var thermostatHeatpolMeasurementsProvider: ThermostatHeatpolMeasurementsProvider

  @InjectMockKs
  private lateinit var useCase: LoadChannelMeasurementsUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should provide sets when there is a provider which can handle given function`() {
    // given
    val remoteId = 1
    val channelWithChildren: ChannelWithChildren = mockk {
    }
    val spec: ChartDataSpec = mockk()
    val channelChartSets: ChannelChartSets = mockk()

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channelWithChildren)
    every { temperatureMeasurementsProvider.handle(channelWithChildren) } returns true
    every { temperatureMeasurementsProvider.provide(channelWithChildren, spec) } returns Single.just(channelChartSets)

    // when
    val observer = useCase.invoke(remoteId, spec).test()

    // then
    observer.assertComplete()
    observer.assertResult(channelChartSets)

    verify {
      readChannelWithChildrenUseCase.invoke(remoteId)
      temperatureMeasurementsProvider.handle(channelWithChildren)
      temperatureMeasurementsProvider.provide(channelWithChildren, spec)
    }
    confirmVerified(
      readChannelWithChildrenUseCase,
      temperatureMeasurementsProvider,
      temperatureAndHumidityMeasurementsProvider,
      generalPurposeMeasurementMeasurementsProvider,
      generalPurposeMeterMeasurementsProvider,
      electricityMeterMeasurementsProvider
    )
  }

  @Test
  @SuppressLint("CheckResult")
  fun `should throw exception when there is no provider for given function`() {
    // given
    val remoteId = 1
    val function = SuplaFunction.ALARM
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.function } returns function
    }
    val spec: ChartDataSpec = mockk()

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channelWithChildren)
    every { temperatureMeasurementsProvider.handle(channelWithChildren) } returns false
    every { temperatureAndHumidityMeasurementsProvider.handle(channelWithChildren) } returns false
    every { generalPurposeMeasurementMeasurementsProvider.handle(channelWithChildren) } returns false
    every { generalPurposeMeterMeasurementsProvider.handle(channelWithChildren) } returns false
    every { electricityMeterMeasurementsProvider.handle(channelWithChildren) } returns false
    every { humidityMeasurementsProvider.handle(channelWithChildren) } returns false
    every { impulseCounterMeasurementsProvider.handle(channelWithChildren) } returns false
    every { thermostatHeatpolMeasurementsProvider.handle(channelWithChildren) } returns false

    // when
    val observer = useCase.invoke(remoteId, spec).test()
    observer.assertError(IllegalArgumentException::class.java)

    verify {
      readChannelWithChildrenUseCase.invoke(remoteId)
      temperatureMeasurementsProvider.handle(channelWithChildren)
      temperatureAndHumidityMeasurementsProvider.handle(channelWithChildren)
      generalPurposeMeasurementMeasurementsProvider.handle(channelWithChildren)
      generalPurposeMeterMeasurementsProvider.handle(channelWithChildren)
      electricityMeterMeasurementsProvider.handle(channelWithChildren)
      humidityMeasurementsProvider.handle(channelWithChildren)
      impulseCounterMeasurementsProvider.handle(channelWithChildren)
      thermostatHeatpolMeasurementsProvider.handle(channelWithChildren)
    }
    confirmVerified(
      readChannelWithChildrenUseCase,
      temperatureMeasurementsProvider,
      temperatureAndHumidityMeasurementsProvider,
      generalPurposeMeasurementMeasurementsProvider,
      generalPurposeMeterMeasurementsProvider,
      electricityMeterMeasurementsProvider,
      humidityMeasurementsProvider,
      impulseCounterMeasurementsProvider,
      thermostatHeatpolMeasurementsProvider
    )
  }
}
