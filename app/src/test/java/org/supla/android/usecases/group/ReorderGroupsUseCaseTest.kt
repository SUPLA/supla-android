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
    useCase(listOf(mockLocationItem()), movedItemId = 123)

    coVerify(exactly = 0) { channelGroupRepository.updatePositions(any(), any()) }
    confirmVerified(channelGroupRepository)
  }

  @Test
  fun `should do nothing when location header is missing`() = runTest {
    useCase(listOf(mockGroupItem(11, 1), mockGroupItem(22, 1)), movedItemId = 11)

    coVerify(exactly = 0) { channelGroupRepository.updatePositions(any(), any()) }
    confirmVerified(channelGroupRepository)
  }

  @Test
  fun `should reorder all groups in merged location section`() = runTest {
    val items = listOf(
      mockLocationItem(),
      mockGroupItem(22, 2),
      mockGroupItem(11, 1),
      mockGroupItem(33, 2),
      mockLocationItem(),
      mockGroupItem(44, 3)
    )
    coEvery { channelGroupRepository.updatePositions(listOf(2, 1), listOf(22, 11, 33)) } just Runs

    useCase(items, movedItemId = 11)

    coVerify(exactly = 1) { channelGroupRepository.updatePositions(listOf(2, 1), listOf(22, 11, 33)) }
    confirmVerified(channelGroupRepository)
  }

  @Test
  fun `should not include separate section with the same location caption`() = runTest {
    val items = listOf(
      mockLocationItem("Kitchen"),
      mockGroupItem(11, 1),
      mockLocationItem("Kitchen"),
      mockGroupItem(22, 2)
    )
    coEvery { channelGroupRepository.updatePositions(listOf(1), listOf(11)) } just Runs

    useCase(items, movedItemId = 11)

    coVerify(exactly = 1) { channelGroupRepository.updatePositions(listOf(1), listOf(11)) }
    confirmVerified(channelGroupRepository)
  }

  private fun mockGroupItem(remoteId: Int, locationId: Int): ListItem.GroupItem = mockk {
    every { this@mockk.remoteId } returns remoteId
    every { this@mockk.locationId } returns locationId
  }

  private fun mockLocationItem(userCaption: String = "Kitchen"): ListItem.LocationItem = mockk {
    every { this@mockk.userCaption } returns userCaption
  }
}
