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
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFunction
import org.supla.android.extensions.date

@RunWith(MockitoJUnitRunner::class)
class LoadChannelMeasurementsDataRangeUseCaseTest {

  @MockK
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @MockK
  private lateinit var thermometerDataRangeProvide: ThermometerDataRangeProvide

  @MockK
  private lateinit var humidityAndTemperatureDataRangeProvide: HumidityAndTemperatureDataRangeProvide

  @MockK
  private lateinit var generalPurposeMeasurementDataRangeProvide: GeneralPurposeMeasurementDataRangeProvide

  @MockK
  private lateinit var generalPurposeMeterDataRangeProvide: GeneralPurposeMeterDataRangeProvide

  @MockK
  private lateinit var electricityMeterDataRangeProvide: ElectricityMeterDataRangeProvide

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
    val channelFunction = SuplaChannelFunction.THERMOMETER
    val channel: ChannelDataEntity = mockk {
      every { function } returns channelFunction
    }
    val minDate = date(2024, 5, 1)
    val maxDate = date(2024, 8, 14)

    every { readChannelByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { thermometerDataRangeProvide.handle(channelFunction.value) } returns true
    every { thermometerDataRangeProvide.minTime(remoteId, profileId) } returns Single.just(minDate.time)
    every { thermometerDataRangeProvide.maxTime(remoteId, profileId) } returns Single.just(maxDate.time)

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(Optional.of(DateRange(minDate, maxDate)))

    verify {
      readChannelByRemoteIdUseCase.invoke(remoteId)
      thermometerDataRangeProvide.handle(channelFunction.value)
      thermometerDataRangeProvide.minTime(remoteId, profileId)
      thermometerDataRangeProvide.maxTime(remoteId, profileId)
    }
    confirmVerified(
      readChannelByRemoteIdUseCase,
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
    val channelFunction = SuplaChannelFunction.THERMOMETER
    val channel: ChannelDataEntity = mockk {
      every { function } returns channelFunction
    }

    every { readChannelByRemoteIdUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { thermometerDataRangeProvide.handle(channelFunction.value) } returns false
    every { humidityAndTemperatureDataRangeProvide.handle(channelFunction.value) } returns false
    every { generalPurposeMeasurementDataRangeProvide.handle(channelFunction.value) } returns false
    every { generalPurposeMeterDataRangeProvide.handle(channelFunction.value) } returns false
    every { electricityMeterDataRangeProvide.handle(channelFunction.value) } returns false

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertError {
      it is IllegalArgumentException && it.message == "Channel function not supported ($channelFunction"
    }
    verify {
      readChannelByRemoteIdUseCase.invoke(remoteId)
      thermometerDataRangeProvide.handle(channelFunction.value)
      humidityAndTemperatureDataRangeProvide.handle(channelFunction.value)
      generalPurposeMeasurementDataRangeProvide.handle(channelFunction.value)
      generalPurposeMeterDataRangeProvide.handle(channelFunction.value)
      electricityMeterDataRangeProvide.handle(channelFunction.value)
    }
    confirmVerified(
      readChannelByRemoteIdUseCase,
      thermometerDataRangeProvide,
      humidityAndTemperatureDataRangeProvide,
      generalPurposeMeasurementDataRangeProvide,
      generalPurposeMeterDataRangeProvide,
      electricityMeterDataRangeProvide
    )
  }
}
