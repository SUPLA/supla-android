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

import io.mockk.*
import io.mockk.Called
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.CurrentLogRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HomePlusThermostatLogRepository
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

class DeleteChannelMeasurementsUseCaseTest {

  @MockK
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @MockK
  private lateinit var temperatureLogRepository: TemperatureLogRepository

  @MockK
  private lateinit var temperatureAndHumidityLogUseCase: TemperatureAndHumidityLogRepository

  @MockK
  private lateinit var generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository

  @MockK
  private lateinit var generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository

  @MockK
  private lateinit var electricityMeterLogRepository: ElectricityMeterLogRepository

  @MockK
  private lateinit var humidityLogRepository: HumidityLogRepository

  @MockK
  private lateinit var impulseCounterLogRepository: ImpulseCounterLogRepository

  @MockK
  private lateinit var voltageLogRepository: VoltageLogRepository

  @MockK
  private lateinit var currentLogRepository: CurrentLogRepository

  @MockK
  private lateinit var powerActiveLogRepository: PowerActiveLogRepository

  @MockK
  private lateinit var homePlusThermostatLogRepository: HomePlusThermostatLogRepository

  @InjectMockKs
  private lateinit var useCase: DeleteChannelMeasurementsUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

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

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { temperatureLogRepository.delete(remoteId, profileId) } returns Completable.complete()

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify { readChannelWithChildrenUseCase.invoke(remoteId) }
    verify { temperatureLogRepository.delete(remoteId, profileId) }
    confirmVerified(readChannelWithChildrenUseCase, temperatureLogRepository)
    verify {
      temperatureAndHumidityLogUseCase wasNot Called
      generalPurposeMeterLogRepository wasNot Called
      generalPurposeMeasurementLogRepository wasNot Called
      electricityMeterLogRepository wasNot Called
    }
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

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { temperatureAndHumidityLogUseCase.delete(remoteId, profileId) } returns Completable.complete()

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify { readChannelWithChildrenUseCase.invoke(remoteId) }
    verify { temperatureAndHumidityLogUseCase.delete(remoteId, profileId) }
    confirmVerified(readChannelWithChildrenUseCase, temperatureAndHumidityLogUseCase)
    verify {
      temperatureLogRepository wasNot Called
      generalPurposeMeterLogRepository wasNot Called
      generalPurposeMeasurementLogRepository wasNot Called
      electricityMeterLogRepository wasNot Called
    }
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

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { generalPurposeMeasurementLogRepository.delete(remoteId, profileId) } returns Completable.complete()

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify { readChannelWithChildrenUseCase.invoke(remoteId) }
    verify { generalPurposeMeasurementLogRepository.delete(remoteId, profileId) }
    confirmVerified(readChannelWithChildrenUseCase, generalPurposeMeasurementLogRepository)
    verify {
      temperatureLogRepository wasNot Called
      generalPurposeMeterLogRepository wasNot Called
      temperatureAndHumidityLogUseCase wasNot Called
      electricityMeterLogRepository wasNot Called
    }
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

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { generalPurposeMeterLogRepository.delete(remoteId, profileId) } returns Completable.complete()

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify { readChannelWithChildrenUseCase.invoke(remoteId) }
    verify { generalPurposeMeterLogRepository.delete(remoteId, profileId) }
    confirmVerified(readChannelWithChildrenUseCase, generalPurposeMeterLogRepository)
    verify {
      temperatureLogRepository wasNot Called
      generalPurposeMeasurementLogRepository wasNot Called
      temperatureAndHumidityLogUseCase wasNot Called
      electricityMeterLogRepository wasNot Called
    }
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

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channel)
    every { voltageLogRepository.delete(remoteId, profileId) } returns Completable.complete()
    every { currentLogRepository.delete(remoteId, profileId) } returns Completable.complete()
    every { powerActiveLogRepository.delete(remoteId, profileId) } returns Completable.complete()
    every { electricityMeterLogRepository.delete(remoteId, profileId) } returns Completable.complete()

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify { readChannelWithChildrenUseCase.invoke(remoteId) }
    verify { electricityMeterLogRepository.delete(remoteId, profileId) }
    confirmVerified(readChannelWithChildrenUseCase, electricityMeterLogRepository)
    verify {
      temperatureLogRepository wasNot Called
      generalPurposeMeasurementLogRepository wasNot Called
      temperatureAndHumidityLogUseCase wasNot Called
      generalPurposeMeterLogRepository wasNot Called
    }
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

    every { readChannelWithChildrenUseCase.invoke(remoteId) } returns Maybe.just(channelWithChildren)
    every { temperatureLogRepository.delete(111, profileId) } returns Completable.complete()
    every { temperatureAndHumidityLogUseCase.delete(222, profileId) } returns Completable.complete()

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()

    verify { readChannelWithChildrenUseCase.invoke(remoteId) }
    verify { temperatureLogRepository.delete(111, profileId) }
    verify { temperatureAndHumidityLogUseCase.delete(222, profileId) }
    confirmVerified(readChannelWithChildrenUseCase, temperatureLogRepository, temperatureAndHumidityLogUseCase)
    verify {
      generalPurposeMeasurementLogRepository wasNot Called
      generalPurposeMeterLogRepository wasNot Called
      electricityMeterLogRepository wasNot Called
    }
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
