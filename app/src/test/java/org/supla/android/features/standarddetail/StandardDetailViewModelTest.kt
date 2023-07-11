package org.supla.android.features.standarddetail
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

import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
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
import org.supla.android.core.BaseViewModelTest
import org.supla.android.db.Channel
import org.supla.android.db.ChannelGroup
import org.supla.android.model.ItemType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase

@RunWith(MockitoJUnitRunner::class)
class StandardDetailViewModelTest : BaseViewModelTest<StandardDetailViewState, StandardDetailViewEvent>() {

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: StandardDetailViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should load channel`() {
    // given
    val remoteId = 123
    val channel: Channel = mockk()
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(StandardDetailViewState(channel))

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase)
    verifyZeroInteractions(readChannelGroupByRemoteIdUseCase)
  }

  @Test
  fun `should load group`() {
    // given
    val remoteId = 123
    val group: ChannelGroup = mockk()
    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(group))

    // when
    viewModel.loadData(remoteId, ItemType.GROUP)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(StandardDetailViewState(group))

    verify(readChannelGroupByRemoteIdUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readChannelGroupByRemoteIdUseCase)
    verifyZeroInteractions(readChannelByRemoteIdUseCase)
  }
}
