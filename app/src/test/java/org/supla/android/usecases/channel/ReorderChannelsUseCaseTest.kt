package org.supla.android.usecases.channel
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
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
import org.supla.android.data.source.ChannelRepository
import org.supla.android.ui.lists.ListItem

class ReorderChannelsUseCaseTest {

  @MockK
  private lateinit var channelRepository: ChannelRepository

  @InjectMockKs
  private lateinit var useCase: ReorderChannelsUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should do nothing when moved item is not a channel item`() = runTest {
    useCase(listOf(mockLocationItem()), movedItemId = 123)

    coVerify(exactly = 0) { channelRepository.updatePositions(any(), any()) }
    confirmVerified(channelRepository)
  }

  @Test
  fun `should do nothing when location header is missing`() = runTest {
    useCase(listOf(mockChannelItem(11, 1), mockChannelItem(22, 1)), movedItemId = 11)

    coVerify(exactly = 0) { channelRepository.updatePositions(any(), any()) }
    confirmVerified(channelRepository)
  }

  @Test
  fun `should reorder all channels in merged location section`() = runTest {
    val items = listOf(
      mockLocationItem(),
      mockChannelItem(22, 2),
      mockChannelItem(11, 1),
      mockChannelItem(33, 2),
      mockLocationItem(),
      mockChannelItem(44, 3)
    )
    coEvery { channelRepository.updatePositions(listOf(2, 1), listOf(22, 11, 33)) } just Runs

    useCase(items, movedItemId = 11)

    coVerify(exactly = 1) { channelRepository.updatePositions(listOf(2, 1), listOf(22, 11, 33)) }
    confirmVerified(channelRepository)
  }

  @Test
  fun `should not include separate section with the same location caption`() = runTest {
    val items = listOf(
      mockLocationItem("Kitchen"),
      mockChannelItem(11, 1),
      mockLocationItem("Kitchen"),
      mockChannelItem(22, 2)
    )
    coEvery { channelRepository.updatePositions(listOf(1), listOf(11)) } just Runs

    useCase(items, movedItemId = 11)

    coVerify(exactly = 1) { channelRepository.updatePositions(listOf(1), listOf(11)) }
    confirmVerified(channelRepository)
  }

  private fun mockChannelItem(remoteId: Int, locationId: Int): ListItem.DefaultItem = mockk {
    every { this@mockk.remoteId } returns remoteId
    every { this@mockk.locationId } returns locationId
  }

  private fun mockLocationItem(userCaption: String = "Kitchen"): ListItem.LocationItem = mockk {
    every { this@mockk.userCaption } returns userCaption
  }
}
