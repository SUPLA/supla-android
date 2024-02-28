package org.supla.android.features.details.legacydetail
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
import org.assertj.core.api.Assertions
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.db.Channel
import org.supla.android.db.ChannelGroup
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase

@RunWith(MockitoJUnitRunner::class)
class LegacyDetailViewModelTest : BaseViewModelTest<LegacyDetailViewState, LegacyDetailViewEvent, LegacyDetailViewModel>() {

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: LegacyDetailViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should load channel data`() {
    // given
    val channelId = 123
    val itemType = ItemType.CHANNEL

    val legacyChannel: Channel = mockk()
    val channel: ChannelDataEntity = mockk {
      every { getLegacyChannel() } returns legacyChannel
    }
    whenever(readChannelByRemoteIdUseCase(channelId)).thenReturn(Maybe.just(channel))

    // when
    viewModel.loadData(channelId, itemType)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      LegacyDetailViewEvent.LoadDetailView(legacyChannel)
    )
    verifyZeroInteractions(readChannelGroupByRemoteIdUseCase)
  }

  @Test
  fun `should load group data`() {
    // given
    val groupId = 234
    val itemType = ItemType.GROUP

    val group: ChannelGroup = mockk()
    whenever(readChannelGroupByRemoteIdUseCase(groupId)).thenReturn(Maybe.just(group))

    // when
    viewModel.loadData(groupId, itemType)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      LegacyDetailViewEvent.LoadDetailView(group)
    )
    verifyZeroInteractions(readChannelByRemoteIdUseCase)
  }
}
