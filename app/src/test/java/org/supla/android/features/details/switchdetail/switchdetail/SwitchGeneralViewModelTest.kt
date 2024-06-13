package org.supla.android.features.details.switchdetail.switchdetail
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
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.features.details.switchdetail.general.SwitchGeneralViewEvent
import org.supla.android.features.details.switchdetail.general.SwitchGeneralViewModel
import org.supla.android.features.details.switchdetail.general.SwitchGeneralViewState
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class SwitchGeneralViewModelTest : BaseViewModelTest<SwitchGeneralViewState, SwitchGeneralViewEvent, SwitchGeneralViewModel>() {

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  private lateinit var getChannelStateUseCase: GetChannelStateUseCase

  @Mock
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @Mock
  private lateinit var dateProvider: DateProvider

  @InjectMocks
  override lateinit var viewModel: SwitchGeneralViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should load channel`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_POWERSWITCH
    val channelData: ChannelDataEntity = mockChannelData(function)

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))
    whenever(getChannelStateUseCase.invoke(channelData)).thenReturn(mockk { every { isActive() } returns true })

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    Assertions.assertThat(events).isEmpty()
    Assertions.assertThat(states).containsExactly(SwitchGeneralViewState(channelData, true))

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase)
    verifyNoInteractions(readChannelGroupByRemoteIdUseCase)
  }

  @Test
  fun `should load group`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_POWERSWITCH
    val group: ChannelGroupDataEntity = mockk { every { this@mockk.function } returns function }

    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(group))
    whenever(getChannelStateUseCase.invoke(group)).thenReturn(mockk { every { isActive() } returns false })

    // when
    viewModel.loadData(remoteId, ItemType.GROUP)

    // then
    Assertions.assertThat(events).isEmpty()
    Assertions.assertThat(states).containsExactly(SwitchGeneralViewState(group, false))

    verify(readChannelGroupByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelGroupByRemoteIdUseCase)
    verifyNoInteractions(readChannelByRemoteIdUseCase)
  }

  @Test
  fun `should turn on channel`() {
    // given
    val remoteId = 123
    val itemType = ItemType.CHANNEL
    whenever(executeSimpleActionUseCase(ActionId.TURN_ON, itemType.subjectType, remoteId)).thenReturn(Completable.complete())

    // when
    viewModel.turnOn(remoteId, itemType)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.TURN_ON, SubjectType.CHANNEL, remoteId)
    verifyNoMoreInteractions(executeSimpleActionUseCase)
    verifyNoInteractions(readChannelByRemoteIdUseCase, readChannelGroupByRemoteIdUseCase)
  }

  @Test
  fun `should turn off group`() {
    // given
    val remoteId = 123
    val itemType = ItemType.GROUP
    whenever(executeSimpleActionUseCase(ActionId.TURN_OFF, itemType.subjectType, remoteId)).thenReturn(Completable.complete())

    // when
    viewModel.turnOff(remoteId, itemType)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.TURN_OFF, SubjectType.GROUP, remoteId)
    verifyNoMoreInteractions(executeSimpleActionUseCase)
    verifyNoInteractions(readChannelByRemoteIdUseCase, readChannelGroupByRemoteIdUseCase)
  }

  @Test
  fun `should load estimated count down end time`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_LIGHTSWITCH

    val estimatedEndDate = Date(1000)
    whenever(dateProvider.currentDate()).thenReturn(Date(100))

    val channelData: ChannelDataEntity = mockChannelData(function, estimatedEndDate)
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))
    whenever(getChannelStateUseCase.invoke(channelData)).thenReturn(mockk { every { isActive() } returns true })

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    Assertions.assertThat(events).isEmpty()
    Assertions.assertThat(states).containsExactly(SwitchGeneralViewState(channelData, true, estimatedEndDate))

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase)
    verifyNoInteractions(readChannelGroupByRemoteIdUseCase)
  }

  @Test
  fun `shouldn't load estimated countdown end time when time elapsed`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_LIGHTSWITCH

    val estimatedEndDate = Date(1000)
    whenever(dateProvider.currentDate()).thenReturn(Date(1003))

    val channelData: ChannelDataEntity = mockChannelData(function, estimatedEndDate)
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))
    whenever(getChannelStateUseCase.invoke(channelData)).thenReturn(mockk { every { isActive() } returns true })

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    Assertions.assertThat(events).isEmpty()
    Assertions.assertThat(states).containsExactly(SwitchGeneralViewState(channelData, true))

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase)
    verifyNoInteractions(readChannelGroupByRemoteIdUseCase)
  }

  private fun mockTimerState(date: Date): ChannelExtendedValueEntity {
    val suplaExtendedValue: SuplaChannelExtendedValue = mockk()
    every { suplaExtendedValue.timerEstimatedEndDate } returns date

    val extendedValue: ChannelExtendedValueEntity = mockk()
    every { extendedValue.getSuplaValue() } returns suplaExtendedValue

    return extendedValue
  }

  private fun mockChannelData(function: Int, estimatedEndDate: Date? = null): ChannelDataEntity {
    return mockk {
      every { this@mockk.function } returns function
      every { channelExtendedValueEntity } returns estimatedEndDate?.let { mockTimerState(estimatedEndDate) }
    }
  }
}
