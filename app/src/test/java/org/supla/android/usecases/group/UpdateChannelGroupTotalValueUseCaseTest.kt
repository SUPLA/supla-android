package org.supla.android.usecases.group
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
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupRelationDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.general.SuplaFunction

@Suppress("SameParameterValue")
@RunWith(MockitoJUnitRunner::class)
class UpdateChannelGroupTotalValueUseCaseTest {

  @Mock
  private lateinit var channelGroupRelationRepository: ChannelGroupRelationRepository

  @Mock
  private lateinit var channelGroupRepository: ChannelGroupRepository

  @InjectMocks
  private lateinit var useCase: UpdateChannelGroupTotalValueUseCase

  @Test
  fun `should build total string`() {
    // given
    val rollerShutterRelationData = mockRollerShutter(123, SuplaChannelAvailabilityStatus.ONLINE, 25, 1)
    val roofWindowRelationData = mockRoofWindow(123, SuplaChannelAvailabilityStatus.ONLINE, 75, 0)
    val roofWindowRelationDataOffline = mockRoofWindow(123)
    val facadeBlindRelationData = mockFacadeBlind(234, SuplaChannelAvailabilityStatus.ONLINE, 40, 50)
    val facadeBlindRelationDataOffline = mockFacadeBlind(234)
    val heatpolData = mockRelationData(345, SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS) {
      every { it.getValueHi() } returns true
      every { it.asHeatpolThermostatValue() } returns mockk {
        every { measuredTemperature } returns 18.4f
        every { presetTemperature } returns 19.5f
      }
    }
    val doorLockRelationDataClosed = mockDoorLock(1)
    val doorLockRelationDataOpened = mockDoorLock(1, open = true)
    val powerSwitchRelationOff = mockPowerSwitch(2)
    val powerSwitchRelationOn = mockPowerSwitch(2, on = true)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(
        listOf(
          rollerShutterRelationData,
          roofWindowRelationData,
          roofWindowRelationDataOffline,
          facadeBlindRelationData,
          facadeBlindRelationDataOffline,
          heatpolData,
          doorLockRelationDataClosed,
          doorLockRelationDataOpened,
          powerSwitchRelationOff,
          powerSwitchRelationOn
        )
      )
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(123, 234, 345, 1, 2))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(
        tuple(123, 66, "25:1|75:0"),
        tuple(234, 50, "40:50"),
        tuple(345, 100, "1:18.4:19.5"),
        tuple(1, 100, "0|1"),
        tuple(2, 100, "0|1")
      )
  }

  @Test
  fun `should build total string - door lock`() {
    // given
    val doorLockRelationDataClosed = mockDoorLock(1)
    val doorLockRelationDataOpened = mockDoorLock(1, open = true)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(listOf(doorLockRelationDataClosed, doorLockRelationDataOpened))
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(1))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(tuple(1, 100, "0|1"))
  }

  @Test
  fun `should build total string - power switch`() {
    // given
    val powerSwitchRelationOff = mockPowerSwitch(2)
    val powerSwitchRelationOn = mockPowerSwitch(2, on = true)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(listOf(powerSwitchRelationOff, powerSwitchRelationOn))
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(2))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(tuple(2, 100, "0|1"))
  }

  @Test
  fun `should build total string - dimmer`() {
    // given
    val dimmerRelationOff = mockDimmer(2)
    val dimmerRelationOn = mockDimmer(2, brightness = 44)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(listOf(dimmerRelationOff, dimmerRelationOn))
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(2))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(tuple(2, 100, "0|44"))
  }

  @Test
  fun `should build total string - rgb`() {
    // given
    val rgbRelationOff = mockRgb(1)
    val rgbRelationOn = mockRgb(2, color = 11, brightness = 44)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(listOf(rgbRelationOff, rgbRelationOn))
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(1, 2))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(
        tuple(1, 100, "0:0"),
        tuple(2, 100, "11:44")
      )
  }

  @Test
  fun `should build total string - dimmer and rgb`() {
    // given
    val rgbRelationOff = mockDimmerAndRgb(2)
    val rgbRelationOn = mockDimmerAndRgb(2, color = 11, brightnessColor = 33, brightness = 44)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(listOf(rgbRelationOff, rgbRelationOn))
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(2))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(tuple(2, 100, "0:0:0|11:33:44"))
  }

  @Test
  fun `should build total string - terrace awning`() {
    // given
    val terraceAwning = mockTerraceAwning(2, status = SuplaChannelAvailabilityStatus.ONLINE, position = 30)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(listOf(terraceAwning))
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(2))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(tuple(2, 100, "30:0"))
  }

  @Test
  fun `should build total string - projector screen`() {
    // given
    val projectorScreen = mockProjectorScreen(2, status = SuplaChannelAvailabilityStatus.ONLINE, position = 45)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(listOf(projectorScreen))
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(2))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(tuple(2, 100, "45"))
  }

  @Test
  fun `should build total string - curtain`() {
    // given
    val curtain = mockCurtain(2, status = SuplaChannelAvailabilityStatus.ONLINE, position = 30)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(listOf(curtain))
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(2))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(tuple(2, 100, "30:0"))
  }

  @Test
  fun `should build total string - vertical blind`() {
    // given
    val curtain = mockVerticalBlind(2, status = SuplaChannelAvailabilityStatus.ONLINE, position = 30)

    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(
      Single.just(listOf(curtain))
    )
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(2))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(tuple(2, 100, "30:0"))
  }

  @Test
  fun `should not crash when function is not supported`() {
    // given
    val relation = mockRelationData(1, SuplaFunction.ALARM) {}
    whenever(channelGroupRelationRepository.findAllVisibleRelations()).thenReturn(Single.just(listOf(relation)))
    whenever(channelGroupRepository.update(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(listOf(1))

    val captor = argumentCaptor<List<ChannelGroupEntity>>()
    verify(channelGroupRepository).update(captor.capture())
    verify(channelGroupRelationRepository).findAllVisibleRelations()
    verifyNoMoreInteractions(channelGroupRepository, channelGroupRelationRepository)

    val groups = captor.firstValue
    assertThat(groups)
      .extracting({ it.remoteId }, { it.online }, { it.totalValue })
      .containsExactly(tuple(1, 0, ""))
  }

  private fun mockDoorLock(groupId: Int, open: Boolean = false) =
    mockRelationData(groupId, SuplaFunction.CONTROLLING_THE_DOOR_LOCK, SuplaChannelAvailabilityStatus.ONLINE) {
      every { it.getSubValueHi() } returns (if (open) 1 else 0)
    }

  private fun mockPowerSwitch(groupId: Int, on: Boolean = false) =
    mockRelationData(groupId, SuplaFunction.POWER_SWITCH, SuplaChannelAvailabilityStatus.ONLINE) {
      every { it.getValueHi() } returns on
    }

  private fun mockRollerShutter(
    groupId: Int,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.OFFLINE,
    position: Int = 0,
    sensor: Int = 0
  ) =
    mockRelationData(groupId, SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER, status) {
      every { it.asRollerShutterValue() } returns mockk {
        every { this@mockk.alwaysValidPosition } returns position
      }
      every { it.getSubValueHi() } returns sensor
    }

  private fun mockRoofWindow(
    groupId: Int,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.OFFLINE,
    position: Int = 0,
    sensor: Int = 0
  ) =
    mockRelationData(groupId, SuplaFunction.CONTROLLING_THE_ROOF_WINDOW, status) {
      every { it.asRollerShutterValue() } returns mockk {
        every { this@mockk.alwaysValidPosition } returns position
      }
      every { it.getSubValueHi() } returns sensor
    }

  private fun mockFacadeBlind(
    groupId: Int,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.OFFLINE,
    position: Int = 0,
    tilt: Int = 0
  ) =
    mockRelationData(groupId, SuplaFunction.CONTROLLING_THE_FACADE_BLIND, status) {
      every { it.asFacadeBlindValue() } returns mockk {
        every { this@mockk.alwaysValidPosition } returns position
        every { this@mockk.alwaysValidTilt } returns tilt
      }
    }

  private fun mockDimmer(groupId: Int, brightness: Int = 0) =
    mockRelationData(groupId, SuplaFunction.DIMMER, SuplaChannelAvailabilityStatus.ONLINE) {
      every { it.asBrightness() } returns brightness
    }

  private fun mockRgb(groupId: Int, color: Int = 0, brightness: Int = 0) =
    mockRelationData(groupId, SuplaFunction.RGB_LIGHTING, SuplaChannelAvailabilityStatus.ONLINE) {
      every { it.asColor() } returns color
      every { it.asBrightnessColor() } returns brightness
    }

  private fun mockDimmerAndRgb(groupId: Int, color: Int = 0, brightnessColor: Int = 0, brightness: Int = 0) =
    mockRelationData(groupId, SuplaFunction.DIMMER_AND_RGB_LIGHTING, SuplaChannelAvailabilityStatus.ONLINE) {
      every { it.asColor() } returns color
      every { it.asBrightnessColor() } returns brightnessColor
      every { it.asBrightness() } returns brightness
    }

  private fun mockTerraceAwning(
    groupId: Int,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.OFFLINE,
    position: Int = 0,
    sensor: Int = 0
  ) =
    mockRelationData(groupId, SuplaFunction.TERRACE_AWNING, status) {
      every { it.asRollerShutterValue() } returns mockk {
        every { this@mockk.alwaysValidPosition } returns position
      }
      every { it.getSubValueHi() } returns sensor
    }

  private fun mockCurtain(
    groupId: Int,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.OFFLINE,
    position: Int = 0,
    sensor: Int = 0
  ) =
    mockRelationData(groupId, SuplaFunction.CURTAIN, status) {
      every { it.asRollerShutterValue() } returns mockk {
        every { this@mockk.alwaysValidPosition } returns position
      }
      every { it.getSubValueHi() } returns sensor
    }

  private fun mockProjectorScreen(
    groupId: Int,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.OFFLINE,
    position: Int = 0
  ) =
    mockRelationData(groupId, SuplaFunction.PROJECTOR_SCREEN, status) {
      every { it.asRollerShutterValue() } returns mockk {
        every { this@mockk.alwaysValidPosition } returns position
      }
    }

  private fun mockVerticalBlind(
    groupId: Int,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.OFFLINE,
    position: Int = 0,
    sensor: Int = 0
  ) =
    mockRelationData(groupId, SuplaFunction.VERTICAL_BLIND, status) {
      every { it.asRollerShutterValue() } returns mockk {
        every { this@mockk.alwaysValidPosition } returns position
      }
      every { it.getSubValueHi() } returns sensor
    }

  private fun mockRelationData(
    groupId: Int,
    function: SuplaFunction,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.ONLINE,
    extraValueMock: (ChannelValueEntity) -> Unit
  ): ChannelGroupRelationDataEntity {
    val channelGroup = ChannelGroupEntity(
      id = 1,
      remoteId = groupId,
      caption = "",
      function = function,
      online = 0,
      visible = 1,
      locationId = 0,
      altIcon = 0,
      userIcon = 0,
      flags = 0L,
      totalValue = null,
      position = 0,
      profileId = 0L
    )

    val value: ChannelValueEntity = mockk {
      every { this@mockk.status } returns status
    }
    extraValueMock(value)

    return mockk {
      every { channelGroupEntity } returns channelGroup
      every { channelValueEntity } returns value
    }
  }
}
