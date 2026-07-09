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

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.ui.lists.ListItem

class ReorderGroupsUseCaseTest {

  @MockK
  private lateinit var channelGroupRepository: ChannelGroupRepository

  @InjectMockKs
  private lateinit var useCase: ReorderGroupsUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should do nothing when moved item is not a group item`() = runTest {
    // given
    val items = listOf(
      mockLocationItem(remoteId = 10, userCaption = "Kitchen"),
      mockLocationItem(remoteId = 11, userCaption = "Hall")
    )

    // when
    useCase(items, movedItemId = 123)

    // then
    coVerify(exactly = 0) { channelGroupRepository.updatePosition(any(), any()) }
    confirmVerified(channelGroupRepository)
  }

  @Test
  fun `should do nothing when matching location caption is missing`() = runTest {
    // given
    val movedItem = mockGroupItem(remoteId = 11, locationId = 1, locationCaption = "Kitchen")
    val otherItem = mockGroupItem(remoteId = 22, locationId = 1, locationCaption = "Kitchen")
    val items = listOf(
      mockLocationItem(remoteId = 2, userCaption = "Hall"),
      movedItem,
      otherItem
    )

    // when
    useCase(items, movedItemId = 11)

    // then
    coVerify(exactly = 0) { channelGroupRepository.updatePosition(any(), any()) }
    confirmVerified(channelGroupRepository)
  }

  @Test
  fun `should reorder groups in moved item location`() = runTest {
    // given
    val movedItem = mockGroupItem(remoteId = 11, locationId = 1, locationCaption = "Kitchen")
    val secondItem = mockGroupItem(remoteId = 22, locationId = 1, locationCaption = "Kitchen")
    val otherLocationItem = mockGroupItem(remoteId = 33, locationId = 2, locationCaption = "Hall")
    val items = listOf(
      mockLocationItem(remoteId = 1, userCaption = "Kitchen"),
      movedItem,
      secondItem,
      mockLocationItem(remoteId = 2, userCaption = "Hall"),
      otherLocationItem
    )
    coEvery { channelGroupRepository.updatePosition(11, 1) } just Runs
    coEvery { channelGroupRepository.updatePosition(22, 2) } just Runs

    // when
    useCase(items, movedItemId = 11)

    // then
    coVerify {
      channelGroupRepository.updatePosition(11, 1)
      channelGroupRepository.updatePosition(22, 2)
    }
    coVerify(exactly = 0) { channelGroupRepository.updatePosition(33, any()) }
    confirmVerified(channelGroupRepository)
  }

  @Test
  fun `should reorder groups by caption when there are duplicated location captions`() = runTest {
    // given
    val firstKitchen = mockLocationItem(remoteId = 1, userCaption = "Kitchen")
    val secondKitchen = mockLocationItem(remoteId = 2, userCaption = "Kitchen")
    val movedItem = mockGroupItem(remoteId = 11, locationId = 1, locationCaption = "Kitchen")
    val sameCaptionDifferentLocation = mockGroupItem(remoteId = 22, locationId = 2, locationCaption = "Kitchen")
    val hallItem = mockGroupItem(remoteId = 33, locationId = 3, locationCaption = "Hall")
    val items = listOf(
      firstKitchen,
      movedItem,
      secondKitchen,
      sameCaptionDifferentLocation,
      mockLocationItem(remoteId = 3, userCaption = "Hall"),
      hallItem
    )
    coEvery { channelGroupRepository.updatePosition(11, 1) } just Runs
    coEvery { channelGroupRepository.updatePosition(22, 2) } just Runs

    // when
    useCase(items, movedItemId = 11)

    // then
    coVerify {
      channelGroupRepository.updatePosition(11, 1)
      channelGroupRepository.updatePosition(22, 2)
    }
    coVerify(exactly = 0) { channelGroupRepository.updatePosition(33, any()) }
    confirmVerified(channelGroupRepository)
  }

  private fun mockGroupItem(remoteId: Int, locationId: Int, locationCaption: String): ListItem.GroupItem =
    mockk {
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.locationId } returns locationId
      every { this@mockk.locationCaption } returns locationCaption
    }

  private fun mockLocationItem(remoteId: Int, userCaption: String): ListItem.LocationItem =
    mockk {
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.userCaption } returns userCaption
    }
}
