package org.supla.android.features.standarddetail.switchdetail
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
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.db.Channel
import org.supla.android.db.ChannelGroup
import org.supla.android.db.ChannelValue
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.model.ItemType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase

@RunWith(MockitoJUnitRunner::class)
class SwitchDetailViewModelTest : BaseViewModelTest<SwitchDetailViewState, SwitchDetailViewEvent>() {

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

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
    val imageId = ImageId(0)

    val channelValue: ChannelValue = mockk()
    every { channelValue.hiValue() } returns true

    val channel: Channel = mockk()
    every { channel.imageIdx } returns imageId
    every { channel.value } returns channelValue
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    Assertions.assertThat(events).isEmpty()
    Assertions.assertThat(states).containsExactly(SwitchDetailViewState(imageId, true))

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase)
    verifyZeroInteractions(readChannelGroupByRemoteIdUseCase)
  }

  @Test
  fun `should load group`() {
    // given
    val remoteId = 123
    val imageId = ImageId(0)
    val group: ChannelGroup = mockk()
    every { group.imageIdx } returns imageId
    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(group))

    // when
    viewModel.loadData(remoteId, ItemType.GROUP)

    // then
    Assertions.assertThat(events).isEmpty()
    Assertions.assertThat(states).containsExactly(SwitchDetailViewState(imageId, false))

    verify(readChannelGroupByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelGroupByRemoteIdUseCase)
    verifyZeroInteractions(readChannelByRemoteIdUseCase)
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
    verifyZeroInteractions(readChannelByRemoteIdUseCase, readChannelGroupByRemoteIdUseCase)
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
    verifyZeroInteractions(readChannelByRemoteIdUseCase, readChannelGroupByRemoteIdUseCase)
  }
}
