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
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.extensions.date
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.general.SuplaFunction

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
    val channel: ChannelDataEntity = mockk()
    every { channel.channelEntity } returns mockk { every { function } returns SuplaFunction.HUMIDITY }
    every { channel.remoteId } returns remoteId
    every { channel.function } returns SuplaFunction.HUMIDITY

    whenever(readChannelWithChildrenUseCase.invoke(remoteId))
      .thenReturn(Maybe.just(ChannelWithChildren(channel, emptyList())))

    // when
    val testObserver = useCase.invoke(remoteId, profileId).test()

    // then
    testObserver.assertError(IllegalArgumentException::class.java)

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase)
    verifyNoInteractions(temperatureLogRepository, temperatureAndHumidityLogRepository)
  }

  private fun mockChannelWithChildren(): ChannelWithChildren {
    val channel: ChannelDataEntity = mockk()
    every { channel.channelEntity } returns mockk { every { function } returns SuplaFunction.HVAC_THERMOSTAT }
    every { channel.function } returns SuplaFunction.HVAC_THERMOSTAT
    every { channel.remoteId } returns 1
    val child1: ChannelDataEntity = mockk()
    every { child1.channelEntity } returns mockk {
      every { function } returns SuplaFunction.HUMIDITY_AND_TEMPERATURE
      every { remoteId } returns 2
    }
    every { child1.function } returns SuplaFunction.HUMIDITY_AND_TEMPERATURE
    val child2: ChannelDataEntity = mockk()
    every { child2.channelEntity } returns mockk {
      every { function } returns SuplaFunction.THERMOMETER
      every { remoteId } returns 3
    }
    every { child2.function } returns SuplaFunction.THERMOMETER

    val mainRelation: ChannelRelationEntity = mockk {
      every { relationType } returns ChannelRelationType.MAIN_THERMOMETER
    }
    val auxRelation: ChannelRelationEntity = mockk {
      every { relationType } returns ChannelRelationType.MAIN_THERMOMETER
    }

    return ChannelWithChildren(
      channel = channel,
      children = listOf(
        ChannelChildEntity(mainRelation, child1),
        ChannelChildEntity(auxRelation, child2)
      )
    )
  }
}
