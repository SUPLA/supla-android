package org.supla.android.ui.lists

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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.images.ImageId
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString

class ListItemTest {

  @Test
  fun `should be different when slideable data changes`() {
    // given
    val channel = mockChannel()
    val first = createItem(channel, ImageId(1))
    val second = createItem(channel, ImageId(2))

    // when
    val different = first.isDifferentFrom(second)

    // then
    assertThat(different).isTrue()
  }

  private fun createItem(channel: ChannelDataEntity, icon: ImageId) =
    ListItem.IconWithRightButtonItem(
      channel = channel,
      locationCaption = "Location",
      online = ListOnlineState.ONLINE,
      captionProvider = LocalizedString.Constant("Gate"),
      icon = icon,
      value = null,
      estimatedTimerEndDate = null,
      issues = ListItemIssues.empty
    )

  private fun mockChannel(): ChannelDataEntity =
    mockk {
      every { remoteId } returns 1
      every { function } returns SuplaFunction.CONTROLLING_THE_GATE
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { caption } returns "Gate"
      every { showInfo } returns false
    }
}
