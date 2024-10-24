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
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.usecases.channel.measurementsprovider.ElectricityConsumptionMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.GeneralPurposeMeasurementMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.GeneralPurposeMeterMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.TemperatureAndHumidityMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.TemperatureMeasurementsProvider
import org.supla.core.shared.data.SuplaChannelFunction

class LoadChannelMeasurementsUseCaseTest : BaseLoadMeasurementsUseCaseTest() {

  @MockK
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @MockK
  private lateinit var temperatureMeasurementsProvider: TemperatureMeasurementsProvider

  @MockK
  private lateinit var temperatureAndHumidityMeasurementsProvider: TemperatureAndHumidityMeasurementsProvider

  @MockK
  private lateinit var generalPurposeMeasurementMeasurementsProvider: GeneralPurposeMeasurementMeasurementsProvider

  @MockK
  private lateinit var generalPurposeMeterMeasurementsProvider: GeneralPurposeMeterMeasurementsProvider

  @MockK
  private lateinit var electricityConsumptionMeasurementsProvider: ElectricityConsumptionMeasurementsProvider

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
    val function = SuplaChannelFunction.THERMOMETER
    val channel: ChannelDataEntity = mockk {
      every { this@mockk.function } returns function
    }
    val spec: ChartDataSpec = mockk()
    val channelChartSets: ChannelChartSets = mockk()

    every { readChannelByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { temperatureMeasurementsProvider.handle(function.value) } returns true
    every { temperatureMeasurementsProvider.provide(channel, spec) } returns Single.just(channelChartSets)

    // when
    val observer = useCase.invoke(remoteId, spec).test()

    // then
    observer.assertComplete()
    observer.assertResult(channelChartSets)

    verify {
      readChannelByRemoteIdUseCase.invoke(remoteId)
      temperatureMeasurementsProvider.handle(function.value)
      temperatureMeasurementsProvider.provide(channel, spec)
    }
    confirmVerified(
      readChannelByRemoteIdUseCase,
      temperatureMeasurementsProvider,
      temperatureAndHumidityMeasurementsProvider,
      generalPurposeMeasurementMeasurementsProvider,
      generalPurposeMeterMeasurementsProvider,
      electricityConsumptionMeasurementsProvider
    )
  }

  @Test
  @SuppressLint("CheckResult")
  fun `should throw exception when there is no provider for given function`() {
    // given
    val remoteId = 1
    val function = SuplaChannelFunction.ALARM
    val channel: ChannelDataEntity = mockk {
      every { this@mockk.function } returns function
    }
    val spec: ChartDataSpec = mockk()

    every { readChannelByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { temperatureMeasurementsProvider.handle(function.value) } returns false
    every { temperatureAndHumidityMeasurementsProvider.handle(function.value) } returns false
    every { generalPurposeMeasurementMeasurementsProvider.handle(function.value) } returns false
    every { generalPurposeMeterMeasurementsProvider.handle(function.value) } returns false
    every { electricityConsumptionMeasurementsProvider.handle(function.value) } returns false

    // when
    val observer = useCase.invoke(remoteId, spec).test()
    observer.assertError(IllegalArgumentException::class.java)

    verify {
      readChannelByRemoteIdUseCase.invoke(remoteId)
      temperatureMeasurementsProvider.handle(function.value)
      temperatureAndHumidityMeasurementsProvider.handle(function.value)
      generalPurposeMeasurementMeasurementsProvider.handle(function.value)
      generalPurposeMeterMeasurementsProvider.handle(function.value)
      electricityConsumptionMeasurementsProvider.handle(function.value)
    }
    confirmVerified(
      readChannelByRemoteIdUseCase,
      temperatureMeasurementsProvider,
      temperatureAndHumidityMeasurementsProvider,
      generalPurposeMeasurementMeasurementsProvider,
      generalPurposeMeterMeasurementsProvider,
      electricityConsumptionMeasurementsProvider
    )
  }
}
