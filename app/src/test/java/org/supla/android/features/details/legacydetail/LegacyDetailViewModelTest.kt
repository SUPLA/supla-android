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

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.db.Channel
import org.supla.android.db.ChannelGroup
import org.supla.android.tools.SuplaSchedulers

class LegacyDetailViewModelTest : BaseViewModelTest<LegacyDetailViewState, LegacyDetailViewEvent, LegacyDetailViewModel>(
  MockSchedulers.MOCKK
) {

  @MockK
  private lateinit var channelRepository: ChannelRepository

  @MockK
  lateinit var channelGroupRepository: ChannelGroupRepository

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: LegacyDetailViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
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
    every { channelRepository.findChannelDataEntity(channelId) } returns Observable.just(channel)

    // when
    viewModel.loadData(channelId, itemType)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      LegacyDetailViewEvent.LoadDetailView(legacyChannel)
    )
    verify { channelRepository.findChannelDataEntity(channelId) }
    confirmVerified(channelRepository, channelGroupRepository)
  }

  @Test
  fun `should load group data`() {
    // given
    val groupId = 234
    val itemType = ItemType.GROUP

    val legacyGroup: ChannelGroup = mockk()

    val group: ChannelGroupDataEntity = mockk {
      every { getLegacyGroup() } returns legacyGroup
    }
    every { channelGroupRepository.findGroupDataEntity(groupId) } returns Observable.just(group)

    // when
    viewModel.loadData(groupId, itemType)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(
      LegacyDetailViewEvent.LoadDetailView(legacyGroup)
    )
    verify { channelGroupRepository.findGroupDataEntity(groupId) }
    confirmVerified(channelRepository, channelGroupRepository)
  }
}
