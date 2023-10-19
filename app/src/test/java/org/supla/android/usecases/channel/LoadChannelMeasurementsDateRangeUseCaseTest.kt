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

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.db.Channel
import org.supla.android.extensions.date
import org.supla.android.lib.SuplaConst

@RunWith(MockitoJUnitRunner::class)
class LoadChannelMeasurementsDateRangeUseCaseTest {

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var temperatureLogRepository: TemperatureLogRepository

  @Mock
  private lateinit var temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository

  @InjectMocks
  private lateinit var useCase: LoadChannelMeasurementsDateRangeUseCase

  @Test
  fun `should load thermometer date range`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val minDate = date(2022, 8, 1)
    val maxDate = date(2023, 8, 1)
    val channel: Channel = mockk()
    every { channel.remoteId } returns remoteId
    every { channel.func } returns SuplaConst.SUPLA_CHANNELFNC_THERMOMETER

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(temperatureLogRepository.findMinTimestamp(remoteId, profileId)).thenReturn(Single.just(minDate.time))
    whenever(temperatureLogRepository.findMaxTimestamp(remoteId, profileId)).thenReturn(Single.just(maxDate.time))

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(Optional.of(DateRange(minDate, maxDate)))

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verify(temperatureLogRepository).findMinTimestamp(remoteId, profileId)
    verify(temperatureLogRepository).findMaxTimestamp(remoteId, profileId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase, temperatureLogRepository)
    verifyZeroInteractions(temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should load thermometer with humidity date range`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val minDate = date(2022, 8, 1)
    val maxDate = date(2023, 8, 1)
    val channel: Channel = mockk()
    every { channel.remoteId } returns remoteId
    every { channel.func } returns SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId)).thenReturn(Single.just(minDate.time))
    whenever(temperatureAndHumidityLogRepository.findMaxTimestamp(remoteId, profileId)).thenReturn(Single.just(maxDate.time))

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(Optional.of(DateRange(minDate, maxDate)))

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verify(temperatureAndHumidityLogRepository).findMinTimestamp(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).findMaxTimestamp(remoteId, profileId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase, temperatureAndHumidityLogRepository)
    verifyZeroInteractions(temperatureLogRepository)
  }

  @Test
  fun `should throw error when channel not supported`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val channel: Channel = mockk()
    every { channel.remoteId } returns remoteId
    every { channel.func } returns SuplaConst.SUPLA_CHANNELFNC_HUMIDITY

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertError(IllegalArgumentException::class.java)

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase)
    verifyZeroInteractions(temperatureLogRepository, temperatureAndHumidityLogRepository)
  }
}
