package org.supla.android.features.details.switchdetail
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
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.db.Channel
import org.supla.android.db.ChannelGroup
import org.supla.android.events.UpdateEventsManager
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase

@RunWith(MockitoJUnitRunner::class)
class SwitchDetailViewModelTest : BaseViewModelTest<SwitchDetailViewState, SwitchDetailViewEvent, SwitchDetailViewModel>() {

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  private lateinit var updateEventsManager: UpdateEventsManager

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: SwitchDetailViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should load channel`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_THERMOSTAT
    val channel: Channel = mockk()
    every { channel.visible } returns 1
    every { channel.func } returns function
    val channelData: ChannelDataEntity = mockk {
      every { getLegacyChannel() } returns channel
    }
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL, function)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(SwitchDetailViewState(channel))

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase)
    verifyZeroInteractions(readChannelGroupByRemoteIdUseCase, updateEventsManager)
  }

  @Test
  fun `should close activity when loaded channel is not visible`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_THERMOSTAT
    val channel: Channel = mockk()
    every { channel.visible } returns 0
    every { channel.func } returns function
    val channelData: ChannelDataEntity = mockk {
      every { getLegacyChannel() } returns channel
    }
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL, function)

    // then
    assertThat(events).containsExactly(SwitchDetailViewEvent.Close)
    assertThat(states).isEmpty()

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase)
    verifyZeroInteractions(readChannelGroupByRemoteIdUseCase, updateEventsManager)
  }

  @Test
  fun `should close activity when loaded channel has different function`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_THERMOSTAT
    val channel: Channel = mockk()
    every { channel.visible } returns 1
    every { channel.func } returns function
    val channelData: ChannelDataEntity = mockk {
      every { getLegacyChannel() } returns channel
    }
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL, SUPLA_CHANNELFNC_LIGHTSWITCH)

    // then
    assertThat(events).containsExactly(SwitchDetailViewEvent.Close)
    assertThat(states).isEmpty()

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase)
    verifyZeroInteractions(readChannelGroupByRemoteIdUseCase, updateEventsManager)
  }

  @Test
  fun `should load group`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_DIMMER
    val group: ChannelGroup = mockk()
    every { group.visible } returns 1
    every { group.func } returns function
    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(group))

    // when
    viewModel.loadData(remoteId, ItemType.GROUP, function)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(SwitchDetailViewState(group))

    verify(readChannelGroupByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelGroupByRemoteIdUseCase)
    verifyZeroInteractions(readChannelByRemoteIdUseCase, updateEventsManager)
  }

  @Test
  fun `should reload channel when channels updated`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_DIMMER
    val channel: Channel = mockk()
    every { channel.visible } returns 1
    every { channel.func } returns function
    val channelData: ChannelDataEntity = mockk {
      every { getLegacyChannel() } returns channel
    }
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))
    whenever(updateEventsManager.observeChannelsUpdate()).thenReturn(Observable.just(Any()))

    // when
    viewModel.observeUpdates(remoteId, ItemType.CHANNEL, function)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(SwitchDetailViewState(channel))

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verify(updateEventsManager).observeChannelsUpdate()
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase, updateEventsManager)
    verifyZeroInteractions(readChannelGroupByRemoteIdUseCase)
  }

  @Test
  fun `should reload group when channels updated`() {
    // given
    val remoteId = 123
    val function = SUPLA_CHANNELFNC_DIMMER
    val group: ChannelGroup = mockk()
    every { group.visible } returns 1
    every { group.func } returns function
    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(group))
    whenever(updateEventsManager.observeGroupsUpdate()).thenReturn(Observable.just(Any()))

    // when
    viewModel.observeUpdates(remoteId, ItemType.GROUP, function)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(SwitchDetailViewState(group))

    verify(readChannelGroupByRemoteIdUseCase).invoke(remoteId)
    verify(updateEventsManager).observeGroupsUpdate()
    verifyNoMoreInteractions(readChannelGroupByRemoteIdUseCase, updateEventsManager)
    verifyZeroInteractions(readChannelByRemoteIdUseCase)
  }
}
