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
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.events.DownloadEventsManager.DataType.DEFAULT_TYPE
import org.supla.android.extensions.date
import org.supla.core.shared.data.model.general.SuplaFunction

class LoadChannelMeasurementsDataRangeUseCaseTest {

  @MockK
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @MockK
  private lateinit var thermometerDataRangeProvide: ThermometerDataRangeProvider

  @MockK
  private lateinit var humidityAndTemperatureDataRangeProvide: HumidityAndTemperatureDataRangeProvider

  @MockK
  private lateinit var generalPurposeMeasurementDataRangeProvide: GeneralPurposeMeasurementDataRangeProvider

  @MockK
  private lateinit var generalPurposeMeterDataRangeProvide: GeneralPurposeMeterDataRangeProvider

  @MockK
  private lateinit var electricityMeterDataRangeProvide: ElectricityMeterDataRangeProvider

  @MockK
  private lateinit var humidityDataRangeProvider: HumidityDataRangeProvider

  @MockK
  private lateinit var impulseCounterDataRangeProvider: ImpulseCounterDataRangeProvider

  @MockK
  private lateinit var voltageDataRangeProvider: VoltageDataRangeProvider

  @MockK
  private lateinit var currentDataRangeProvider: CurrentDataRangeProvider

  @MockK
  private lateinit var powerActiveDataRangeProvider: PowerActiveDataRangeProvider

  @MockK
  private lateinit var thermostatHeatpolDataRangeProvider: ThermostatHeatpolDataRangeProvider

  @InjectMockKs
  private lateinit var useCase: LoadChannelMeasurementsDataRangeUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should use provider when it can handle given channel`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val channelWithChildren: ChannelWithChildren = mockk()
    val minDate = date(2024, 5, 1)
    val maxDate = date(2024, 8, 14)

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channelWithChildren)
    every { thermometerDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE) } returns true
    every { thermometerDataRangeProvide.minTime(remoteId, profileId) } returns Single.just(minDate.time)
    every { thermometerDataRangeProvide.maxTime(remoteId, profileId) } returns Single.just(maxDate.time)

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(Optional.of(DateRange(minDate, maxDate)))

    verify {
      readChannelWithChildrenUseCase.invoke(remoteId)
      thermometerDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE)
      thermometerDataRangeProvide.minTime(remoteId, profileId)
      thermometerDataRangeProvide.maxTime(remoteId, profileId)
    }
    confirmVerified(
      readChannelWithChildrenUseCase,
      thermometerDataRangeProvide,
      humidityAndTemperatureDataRangeProvide,
      generalPurposeMeasurementDataRangeProvide,
      generalPurposeMeterDataRangeProvide,
      electricityMeterDataRangeProvide
    )
  }

  @Test
  fun `should throw exception when there is no provider for function`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val channelFunction = SuplaFunction.THERMOMETER
    val channelWithChildren: ChannelWithChildren = mockk {
      every { function } returns channelFunction
    }

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channelWithChildren)
    every { thermometerDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { humidityAndTemperatureDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { generalPurposeMeasurementDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { generalPurposeMeterDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { electricityMeterDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { humidityDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { impulseCounterDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { voltageDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { currentDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { powerActiveDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE) } returns false
    every { thermostatHeatpolDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE) } returns false

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertError {
      it is IllegalArgumentException && it.message == "Channel function not supported ($channelFunction"
    }
    verify {
      readChannelWithChildrenUseCase.invoke(remoteId)
      thermometerDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE)
      humidityAndTemperatureDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE)
      generalPurposeMeasurementDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE)
      generalPurposeMeterDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE)
      electricityMeterDataRangeProvide.handle(channelWithChildren, DEFAULT_TYPE)
      humidityDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE)
      impulseCounterDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE)
      voltageDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE)
      currentDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE)
      powerActiveDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE)
      thermostatHeatpolDataRangeProvider.handle(channelWithChildren, DEFAULT_TYPE)
    }
    confirmVerified(
      readChannelWithChildrenUseCase,
      thermometerDataRangeProvide,
      humidityAndTemperatureDataRangeProvide,
      generalPurposeMeasurementDataRangeProvide,
      generalPurposeMeterDataRangeProvide,
      electricityMeterDataRangeProvide,
      humidityDataRangeProvider,
      impulseCounterDataRangeProvider,
      voltageDataRangeProvider,
      powerActiveDataRangeProvider,
      thermostatHeatpolDataRangeProvider
    )
  }
}
