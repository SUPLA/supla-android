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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.core.shared.data.SuplaChannelFunction
import org.supla.core.shared.data.source.local.entity.ChannelRelationType

@RunWith(MockitoJUnitRunner::class)
class DeleteChannelMeasurementsUseCaseTest {

  @Mock
  private lateinit var channelRepository: RoomChannelRepository

  @Mock
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @Mock
  private lateinit var temperatureLogRepository: TemperatureLogRepository

  @Mock
  private lateinit var temperatureAndHumidityLogUseCase: TemperatureAndHumidityLogRepository

  @Mock
  private lateinit var generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository

  @Mock
  private lateinit var generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository

  @Mock
  private lateinit var electricityMeterLogRepository: ElectricityMeterLogRepository

  @InjectMocks
  private lateinit var useCase: DeleteChannelMeasurementsUseCase

  @Test
  fun `should delete temperature history`() {
    // given
    val remoteId = 234
    val profileId = 123L
    val channel: ChannelEntity = mockk {
      every { this@mockk.function } returns SuplaChannelFunction.THERMOMETER
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(channelRepository.findByRemoteId(remoteId)).thenReturn(Maybe.just(channel))
    whenever(temperatureLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(channelRepository).findByRemoteId(remoteId)
    verify(temperatureLogRepository).delete(remoteId, profileId)
    verifyNoMoreInteractions(channelRepository, temperatureLogRepository)
    verifyNoInteractions(
      readChannelWithChildrenUseCase,
      temperatureAndHumidityLogUseCase,
      generalPurposeMeterLogRepository,
      generalPurposeMeasurementLogRepository,
      electricityMeterLogRepository
    )
  }

  @Test
  fun `should delete temperature and humidity history`() {
    // given
    val remoteId = 234
    val profileId = 123L
    val channel: ChannelEntity = mockk {
      every { this@mockk.function } returns SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(channelRepository.findByRemoteId(remoteId)).thenReturn(Maybe.just(channel))
    whenever(temperatureAndHumidityLogUseCase.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(channelRepository).findByRemoteId(remoteId)
    verify(temperatureAndHumidityLogUseCase).delete(remoteId, profileId)
    verifyNoMoreInteractions(channelRepository, temperatureAndHumidityLogUseCase)
    verifyNoInteractions(
      readChannelWithChildrenUseCase,
      temperatureLogRepository,
      generalPurposeMeterLogRepository,
      generalPurposeMeasurementLogRepository,
      electricityMeterLogRepository
    )
  }

  @Test
  fun `should delete general purpose measurement history`() {
    // given
    val remoteId = 234
    val profileId = 123L
    val channel: ChannelEntity = mockk {
      every { this@mockk.function } returns SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(channelRepository.findByRemoteId(remoteId)).thenReturn(Maybe.just(channel))
    whenever(generalPurposeMeasurementLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(channelRepository).findByRemoteId(remoteId)
    verify(generalPurposeMeasurementLogRepository).delete(remoteId, profileId)
    verifyNoMoreInteractions(channelRepository, generalPurposeMeasurementLogRepository)
    verifyNoInteractions(
      readChannelWithChildrenUseCase,
      temperatureLogRepository,
      generalPurposeMeterLogRepository,
      temperatureAndHumidityLogUseCase,
      electricityMeterLogRepository
    )
  }

  @Test
  fun `should delete general purpose meter history`() {
    // given
    val remoteId = 234
    val profileId = 123L
    val channel: ChannelEntity = mockk {
      every { this@mockk.function } returns SuplaChannelFunction.GENERAL_PURPOSE_METER
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(channelRepository.findByRemoteId(remoteId)).thenReturn(Maybe.just(channel))
    whenever(generalPurposeMeterLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(channelRepository).findByRemoteId(remoteId)
    verify(generalPurposeMeterLogRepository).delete(remoteId, profileId)
    verifyNoMoreInteractions(channelRepository, generalPurposeMeterLogRepository)
    verifyNoInteractions(
      readChannelWithChildrenUseCase,
      temperatureLogRepository,
      generalPurposeMeasurementLogRepository,
      temperatureAndHumidityLogUseCase,
      electricityMeterLogRepository
    )
  }

  @Test
  fun `should delete electricity meter history`() {
    // given
    val remoteId = 234
    val profileId = 123L
    val channel: ChannelEntity = mockk {
      every { this@mockk.function } returns SuplaChannelFunction.ELECTRICITY_METER
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(channelRepository.findByRemoteId(remoteId)).thenReturn(Maybe.just(channel))
    whenever(electricityMeterLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(channelRepository).findByRemoteId(remoteId)
    verify(electricityMeterLogRepository).delete(remoteId, profileId)
    verifyNoMoreInteractions(channelRepository, electricityMeterLogRepository)
    verifyNoInteractions(
      readChannelWithChildrenUseCase,
      temperatureLogRepository,
      generalPurposeMeasurementLogRepository,
      temperatureAndHumidityLogUseCase,
      generalPurposeMeterLogRepository
    )
  }

  @Test
  fun `should delete thermostat history (channel with children)`() {
    // given
    val remoteId = 234
    val profileId = 123L
    val channel: ChannelEntity = mockk {
      every { this@mockk.function } returns SuplaChannelFunction.HVAC_THERMOSTAT
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }
    val thermometerChild = mockChannelChild(
      function = SuplaChannelFunction.THERMOMETER,
      profileId = profileId,
      remoteId = 111,
      relationType = ChannelRelationType.MAIN_THERMOMETER
    )
    val thermometerAndHumidityChild = mockChannelChild(
      function = SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE,
      profileId = profileId,
      remoteId = 222,
      relationType = ChannelRelationType.AUX_THERMOMETER_FLOOR
    )
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.children } returns listOf(thermometerChild, thermometerAndHumidityChild)
    }

    whenever(channelRepository.findByRemoteId(remoteId)).thenReturn(Maybe.just(channel))
    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(temperatureLogRepository.delete(111, profileId)).thenReturn(Completable.complete())
    whenever(temperatureAndHumidityLogUseCase.delete(222, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(channelRepository).findByRemoteId(remoteId)
    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureLogRepository).delete(111, profileId)
    verify(temperatureAndHumidityLogUseCase).delete(222, profileId)
    verifyNoMoreInteractions(channelRepository, readChannelWithChildrenUseCase, temperatureLogRepository, temperatureAndHumidityLogUseCase)
    verifyNoInteractions(
      generalPurposeMeasurementLogRepository,
      generalPurposeMeterLogRepository,
      electricityMeterLogRepository
    )
  }

  private fun mockChannelChild(
    function: SuplaChannelFunction,
    profileId: Long,
    remoteId: Int,
    relationType: ChannelRelationType
  ): ChannelChildEntity {
    val channelEntity: ChannelEntity = mockk {
      every { this@mockk.function } returns function
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }
    return mockk {
      every { this@mockk.channel } returns channelEntity
      every { this@mockk.relationType } returns relationType
    }
  }
}
