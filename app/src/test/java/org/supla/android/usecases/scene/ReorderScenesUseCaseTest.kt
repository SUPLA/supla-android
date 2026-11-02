package org.supla.android.usecases.scene
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
import org.supla.android.data.source.SceneRepository
import org.supla.android.ui.lists.ListItem

class ReorderScenesUseCaseTest {

  @MockK
  private lateinit var sceneRepository: SceneRepository

  @InjectMockKs
  private lateinit var useCase: ReorderScenesUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should do nothing when moved item is not a scene item`() = runTest {
    useCase(listOf(mockLocationItem()), movedItemId = 123)

    coVerify(exactly = 0) { sceneRepository.updatePositions(any(), any()) }
    confirmVerified(sceneRepository)
  }

  @Test
  fun `should do nothing when location header is missing`() = runTest {
    useCase(listOf(mockSceneItem(11, 1), mockSceneItem(22, 1)), movedItemId = 11)

    coVerify(exactly = 0) { sceneRepository.updatePositions(any(), any()) }
    confirmVerified(sceneRepository)
  }

  @Test
  fun `should reorder all scenes in merged location section`() = runTest {
    val items = listOf(
      mockLocationItem(),
      mockSceneItem(22, 2),
      mockSceneItem(11, 1),
      mockSceneItem(33, 2),
      mockLocationItem(),
      mockSceneItem(44, 3)
    )
    coEvery { sceneRepository.updatePositions(listOf(2, 1), listOf(22, 11, 33)) } just Runs

    useCase(items, movedItemId = 11)

    coVerify(exactly = 1) { sceneRepository.updatePositions(listOf(2, 1), listOf(22, 11, 33)) }
    confirmVerified(sceneRepository)
  }

  @Test
  fun `should not include separate section with the same location caption`() = runTest {
    val items = listOf(
      mockLocationItem("Kitchen"),
      mockSceneItem(11, 1),
      mockLocationItem("Kitchen"),
      mockSceneItem(22, 2)
    )
    coEvery { sceneRepository.updatePositions(listOf(1), listOf(11)) } just Runs

    useCase(items, movedItemId = 11)

    coVerify(exactly = 1) { sceneRepository.updatePositions(listOf(1), listOf(11)) }
    confirmVerified(sceneRepository)
  }

  private fun mockSceneItem(remoteId: Int, locationId: Int): ListItem.SceneItem = mockk {
    every { this@mockk.remoteId } returns remoteId
    every { this@mockk.locationId } returns locationId
  }

  private fun mockLocationItem(userCaption: String = "Kitchen"): ListItem.LocationItem = mockk {
    every { this@mockk.userCaption } returns userCaption
  }
}
