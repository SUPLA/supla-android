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

import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.db.Channel
import org.supla.android.db.ChannelGroup
import org.supla.android.tools.SuplaSchedulers

@RunWith(MockitoJUnitRunner::class)
class LegacyDetailViewModelTest : BaseViewModelTest<LegacyDetailViewState, LegacyDetailViewEvent, LegacyDetailViewModel>() {

  @Mock
  private lateinit var channelRepository: ChannelRepository

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
    whenever(channelRepository.getChannel(channelId)).thenReturn(legacyChannel)

    // when
    viewModel.loadData(channelId, itemType)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      LegacyDetailViewEvent.LoadDetailView(legacyChannel)
    )
    verify(channelRepository).getChannel(channelId)
    verifyNoMoreInteractions(channelRepository)
  }

  @Test
  fun `should load group data`() {
    // given
    val groupId = 234
    val itemType = ItemType.GROUP

    val legacyGroup: ChannelGroup = mockk()
    whenever(channelRepository.getChannelGroup(groupId)).thenReturn(legacyGroup)

    // when
    viewModel.loadData(groupId, itemType)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      LegacyDetailViewEvent.LoadDetailView(legacyGroup)
    )
    verify(channelRepository).getChannelGroup(groupId)
    verifyNoMoreInteractions(channelRepository)
  }
}
