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

import androidx.room.rxjava3.EmptyResultSetException
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
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
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.db.Channel
import org.supla.android.extensions.date
import org.supla.android.lib.SuplaConst

@RunWith(MockitoJUnitRunner::class)
class LoadChannelWithChildrenMeasurementsDateRangeUseCaseTest {

  @Mock
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @Mock
  private lateinit var temperatureLogRepository: TemperatureLogRepository

  @Mock
  private lateinit var temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository

  @InjectMocks
  private lateinit var useCase: LoadChannelWithChildrenMeasurementsDateRangeUseCase

  @Test
  fun `should load measurements range`() {
    // given
    val remoteId = 1
    val profileId = 321L
    val minDate = date(2023, 10, 1)
    val maxDate = date(2023, 10, 10)

    val channelWithChildren = mockChannelWithChildren()
    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(temperatureAndHumidityLogRepository.findMinTimestamp(2, profileId))
      .thenReturn(Single.just(minDate.time))
    whenever(temperatureLogRepository.findMinTimestamp(3, profileId))
      .thenReturn(Single.just(date(2023, 10, 2).time))
    whenever(temperatureAndHumidityLogRepository.findMaxTimestamp(2, profileId))
      .thenReturn(Single.just(date(2023, 10, 3).time))
    whenever(temperatureLogRepository.findMaxTimestamp(3, profileId))
      .thenReturn(Single.just(maxDate.time))

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    assertThat(testObserver.values())
      .containsExactly(Optional.of(DateRange(minDate, maxDate)))

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureAndHumidityLogRepository).findMinTimestamp(2, profileId)
    verify(temperatureAndHumidityLogRepository).findMaxTimestamp(2, profileId)
    verify(temperatureLogRepository).findMinTimestamp(3, profileId)
    verify(temperatureLogRepository).findMaxTimestamp(3, profileId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, temperatureAndHumidityLogRepository, temperatureLogRepository)
  }

  @Test
  fun `should load measurements range when one of children has no measurements`() {
    // given
    val remoteId = 1
    val profileId = 321L
    val minDate = date(2023, 10, 1)
    val maxDate = date(2023, 10, 3)

    val channelWithChildren = mockChannelWithChildren()
    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(temperatureAndHumidityLogRepository.findMinTimestamp(2, profileId))
      .thenReturn(Single.just(minDate.time))
    whenever(temperatureLogRepository.findMinTimestamp(3, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))
    whenever(temperatureAndHumidityLogRepository.findMaxTimestamp(2, profileId))
      .thenReturn(Single.just(maxDate.time))
    whenever(temperatureLogRepository.findMaxTimestamp(3, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    assertThat(testObserver.values())
      .containsExactly(Optional.of(DateRange(minDate, maxDate)))

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureAndHumidityLogRepository).findMinTimestamp(2, profileId)
    verify(temperatureAndHumidityLogRepository).findMaxTimestamp(2, profileId)
    verify(temperatureLogRepository).findMinTimestamp(3, profileId)
    verify(temperatureLogRepository).findMaxTimestamp(3, profileId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, temperatureAndHumidityLogRepository, temperatureLogRepository)
  }

  @Test
  fun `should load measurements range when all children have no measurements`() {
    // given
    val remoteId = 1
    val profileId = 321L

    val channelWithChildren = mockChannelWithChildren()
    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(temperatureAndHumidityLogRepository.findMinTimestamp(2, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))
    whenever(temperatureLogRepository.findMinTimestamp(3, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))
    whenever(temperatureAndHumidityLogRepository.findMaxTimestamp(2, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))
    whenever(temperatureLogRepository.findMaxTimestamp(3, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    assertThat(testObserver.values()).containsExactly(Optional.empty())

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureAndHumidityLogRepository).findMinTimestamp(2, profileId)
    verify(temperatureAndHumidityLogRepository).findMaxTimestamp(2, profileId)
    verify(temperatureLogRepository).findMinTimestamp(3, profileId)
    verify(temperatureLogRepository).findMaxTimestamp(3, profileId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, temperatureAndHumidityLogRepository, temperatureLogRepository)
  }

  @Test
  fun `should throw error when channel not supported`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val channel: Channel = mockk()
    every { channel.remoteId } returns remoteId
    every { channel.func } returns SuplaConst.SUPLA_CHANNELFNC_HUMIDITY

    whenever(readChannelWithChildrenUseCase.invoke(remoteId))
      .thenReturn(Maybe.just(ChannelWithChildren(channel, emptyList())))

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertError(IllegalArgumentException::class.java)

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase)
    verifyZeroInteractions(temperatureLogRepository, temperatureAndHumidityLogRepository)
  }

  private fun mockChannelWithChildren(): ChannelWithChildren {
    val channel: Channel = mockk()
    every { channel.func } returns SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    every { channel.remoteId } returns 1
    val child1: Channel = mockk()
    every { child1.func } returns SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
    every { child1.remoteId } returns 2
    val child2: Channel = mockk()
    every { child2.func } returns SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    every { child2.remoteId } returns 3

    return ChannelWithChildren(
      channel = channel,
      children = listOf(
        ChannelChild(ChannelRelationType.MAIN_THERMOMETER, child1),
        ChannelChild(ChannelRelationType.AUX_THERMOMETER_FLOOR, child2)
      )
    )
  }
}
