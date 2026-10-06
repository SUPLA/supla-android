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

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaThreading
import org.supla.android.tools.VibrationHelper

class BaseListViewModelTest {

  private val viewModel = TestListViewModel()

  @Test
  fun `should add initial items and mark list as loaded`() {
    // given
    val firstItem = locationItem(remoteId = 1)
    val secondItem = locationItem(remoteId = 2)

    // when
    viewModel.update(listOf(firstItem, secondItem))

    // then
    assertThat(viewModel.list).containsExactly(firstItem, secondItem)
    assertThat(viewModel.listLoaded).isTrue()
  }

  @Test
  fun `should preserve existing item when content did not change`() {
    // given
    val currentItem = locationItem(remoteId = 1)
    val equalItem = currentItem.copy()
    viewModel.update(listOf(currentItem))

    // when
    viewModel.update(listOf(equalItem))

    // then
    assertThat(viewModel.list.single()).isSameAs(currentItem)
  }

  @Test
  fun `should replace existing item when content changed`() {
    // given
    val currentItem = locationItem(remoteId = 1, collapsed = false)
    val changedItem = currentItem.copy(collapsed = true)
    viewModel.update(listOf(currentItem))

    // when
    viewModel.update(listOf(changedItem))

    // then
    assertThat(viewModel.list.single()).isSameAs(changedItem)
  }

  @Test
  fun `should add remove and reorder items`() {
    // given
    val removedItem = locationItem(remoteId = 1)
    val firstMovedItem = locationItem(remoteId = 2)
    val secondMovedItem = locationItem(remoteId = 3)
    val addedItem = locationItem(remoteId = 4)
    viewModel.update(listOf(removedItem, firstMovedItem, secondMovedItem))

    // when
    viewModel.update(listOf(secondMovedItem.copy(), addedItem, firstMovedItem.copy()))

    // then
    assertThat(viewModel.list).containsExactly(secondMovedItem, addedItem, firstMovedItem)
    assertThat(viewModel.list[0]).isSameAs(secondMovedItem)
    assertThat(viewModel.list[2]).isSameAs(firstMovedItem)
  }

  private fun locationItem(remoteId: Int, collapsed: Boolean = false) =
    ListItem.LocationItem(
      remoteId = remoteId,
      profileId = 1,
      userCaption = "Location $remoteId",
      collapsed = collapsed
    )

  private class TestListViewModel : BaseListViewModel<TestViewState, TestViewEvent>(
    vibrationHelper = mockk<VibrationHelper>(relaxed = true),
    dateProvider = mockk<DateProvider>(relaxed = true),
    threading = mockk<SuplaThreading>(relaxed = true),
    defaultState = TestViewState
  ) {
    override fun reloadList() = Unit

    fun update(items: List<ListItem>) = updateItemsAtomically(items)
  }

  private data object TestViewState : ViewState()
  private data object TestViewEvent : ViewEvent
}
