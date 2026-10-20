package org.supla.core.shared.usecase.channel.issues
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
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.LocalizedStringId

class DigiglassIssuesProviderTest {

  @InjectMockKs
  private lateinit var provider: DigiglassIssuesProvider

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get empty list if no flag set`() {
    // given
    val channel: Channel = mockk {
      every { function } returns SuplaFunction.DIGIGLASS_HORIZONTAL
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { value } returns byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).isEmpty()
  }

  @Test
  fun `should get issue for each flag set`() {
    // given
    val channel: Channel = mockk {
      every { function } returns SuplaFunction.DIGIGLASS_HORIZONTAL
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { value } returns byteArrayOf(7, 0, 0, 0, 0, 0, 0, 0)
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
    }

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).containsExactly(
      ChannelIssueItem.Warning(LocalizedStringId.DIGIGLASS_PLANNED_REGENERATION),
      ChannelIssueItem.Warning(LocalizedStringId.DIGIGLASS_REGENERATION_AFTER_20H),
      ChannelIssueItem.Error(LocalizedStringId.DIGIGLASS_TO_LONG_OPERATION)
    )
  }
}
