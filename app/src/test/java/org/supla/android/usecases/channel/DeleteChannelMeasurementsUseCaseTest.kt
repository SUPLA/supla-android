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
import org.supla.android.data.source.CurrentLogRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HumidityLogRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.PowerActiveLogRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.VoltageLogRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.general.SuplaFunction

@RunWith(MockitoJUnitRunner::class)
class DeleteChannelMeasurementsUseCaseTest {

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

  @Mock
  private lateinit var humidityLogRepository: HumidityLogRepository

  @Mock
  private lateinit var impulseCounterLogRepository: ImpulseCounterLogRepository

  @Mock
  private lateinit var voltageLogRepository: VoltageLogRepository

  @Mock
  private lateinit var currentLogRepository: CurrentLogRepository

  @Mock
  private lateinit var powerActiveLogRepository: PowerActiveLogRepository

  @InjectMocks
  private lateinit var useCase: DeleteChannelMeasurementsUseCase

  @Test
  fun `should delete temperature history`() {
    // given
    val remoteId = 234
    val profileId = 123L
    val channel: ChannelWithChildren = mockk {
      every { this@mockk.function } returns SuplaFunction.THERMOMETER
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(temperatureLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureLogRepository).delete(remoteId, profileId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, temperatureLogRepository)
    verifyNoInteractions(
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
    val channel: ChannelWithChildren = mockk {
      every { this@mockk.function } returns SuplaFunction.HUMIDITY_AND_TEMPERATURE
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(temperatureAndHumidityLogUseCase.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureAndHumidityLogUseCase).delete(remoteId, profileId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, temperatureAndHumidityLogUseCase)
    verifyNoInteractions(
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
    val channel: ChannelWithChildren = mockk {
      every { this@mockk.function } returns SuplaFunction.GENERAL_PURPOSE_MEASUREMENT
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(generalPurposeMeasurementLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(generalPurposeMeasurementLogRepository).delete(remoteId, profileId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, generalPurposeMeasurementLogRepository)
    verifyNoInteractions(
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
    val channel: ChannelWithChildren = mockk {
      every { this@mockk.function } returns SuplaFunction.GENERAL_PURPOSE_METER
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(generalPurposeMeterLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(generalPurposeMeterLogRepository).delete(remoteId, profileId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, generalPurposeMeterLogRepository)
    verifyNoInteractions(
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
    val channel: ChannelWithChildren = mockk {
      every { function } returns SuplaFunction.ELECTRICITY_METER
      every { isOrHasElectricityMeter } returns true
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
    }

    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(voltageLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(currentLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(powerActiveLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(electricityMeterLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(electricityMeterLogRepository).delete(remoteId, profileId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, electricityMeterLogRepository)
    verifyNoInteractions(
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
    val thermometerChild = mockChannelChild(
      function = SuplaFunction.THERMOMETER,
      profileId = profileId,
      remoteId = 111,
      relationType = ChannelRelationType.MAIN_THERMOMETER
    )
    val thermometerAndHumidityChild = mockChannelChild(
      function = SuplaFunction.HUMIDITY_AND_TEMPERATURE,
      profileId = profileId,
      remoteId = 222,
      relationType = ChannelRelationType.AUX_THERMOMETER_FLOOR
    )
    val channelWithChildren: ChannelWithChildren = mockk {
      every { function } returns SuplaFunction.HVAC_THERMOSTAT
      every { this@mockk.profileId } returns profileId
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.children } returns listOf(thermometerChild, thermometerAndHumidityChild)
      every { isOrHasElectricityMeter } returns false
      every { isOrHasImpulseCounter } returns false
      every { channel } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
      }
    }

    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(temperatureLogRepository.delete(111, profileId)).thenReturn(Completable.complete())
    whenever(temperatureAndHumidityLogUseCase.delete(222, profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureLogRepository).delete(111, profileId)
    verify(temperatureAndHumidityLogUseCase).delete(222, profileId)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, temperatureLogRepository, temperatureAndHumidityLogUseCase)
    verifyNoInteractions(
      generalPurposeMeasurementLogRepository,
      generalPurposeMeterLogRepository,
      electricityMeterLogRepository
    )
  }

  private fun mockChannelChild(
    function: SuplaFunction,
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
