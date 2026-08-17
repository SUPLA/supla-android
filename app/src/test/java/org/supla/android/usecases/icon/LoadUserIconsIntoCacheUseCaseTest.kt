package org.supla.android.usecases.icon
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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.RoomUserIconRepository
import org.supla.android.data.source.local.entity.UserIconEntity
import org.supla.android.events.UpdateEventsManager
import org.supla.android.images.ImageCacheProxy
import org.supla.android.images.ImageId
import org.supla.android.widget.WidgetManager

class LoadUserIconsIntoCacheUseCaseTest {

  @MockK
  private lateinit var userIconRepository: RoomUserIconRepository

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @MockK
  private lateinit var imageCacheProxy: ImageCacheProxy

  @MockK
  private lateinit var widgetManager: WidgetManager

  @InjectMockKs
  private lateinit var useCase: LoadUserIconsIntoCacheUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should add image when available and update all widgets`() {
    // given
    val iconRemoteId = 234
    val profileId = 345L
    val firstImage = byteArrayOf(0)
    val thirdImage = byteArrayOf(1, 2)
    val nightImage = byteArrayOf(1, 2)
    val entity = UserIconEntity(
      123L,
      iconRemoteId,
      firstImage,
      byteArrayOf(),
      thirdImage,
      null,
      nightImage,
      null,
      null,
      null,
      profileId
    )
    every { imageCacheProxy.addImage(ImageId(iconRemoteId, 1, profileId), firstImage) } returns true
    every { imageCacheProxy.addImage(ImageId(iconRemoteId, 3, profileId), thirdImage) } returns true
    every { imageCacheProxy.addImage(ImageId(iconRemoteId, 1, profileId).setNightMode(true), nightImage) } returns true
    every { imageCacheProxy.sum() } returnsMany listOf(0, 3)
    every { imageCacheProxy.size() } returns 3
    every { userIconRepository.loadAllIcons() } returns Observable.just(listOf(entity))
    every { widgetManager.updateAllWidgets() } just Runs
    every { updateEventsManager.emitChannelsUpdate() } just Runs
    every { updateEventsManager.emitGroupsUpdate() } just Runs
    every { updateEventsManager.emitScenesUpdate() } just Runs

    // when
    val testObserver = useCase.invoke().test()

    // then
    testObserver.assertComplete()
    verify {
      imageCacheProxy.addImage(ImageId(iconRemoteId, 1, profileId), firstImage)
      imageCacheProxy.addImage(ImageId(iconRemoteId, 3, profileId), thirdImage)
      imageCacheProxy.addImage(ImageId(iconRemoteId, 1, profileId).setNightMode(true), nightImage)
      imageCacheProxy.size()
      userIconRepository.loadAllIcons()
      widgetManager.updateAllWidgets()
      updateEventsManager.emitChannelsUpdate()
      updateEventsManager.emitGroupsUpdate()
      updateEventsManager.emitScenesUpdate()
    }
    verify(exactly = 2) {
      imageCacheProxy.sum()
    }
    confirmVerified(imageCacheProxy, userIconRepository, widgetManager, updateEventsManager)
  }

  @Test
  fun `should not update widgets if no icon loaded`() {
    // given
    every { imageCacheProxy.sum() } returnsMany listOf(2, 2)
    every { imageCacheProxy.size() } returns 3
    every { userIconRepository.loadAllIcons() } returns Observable.just(emptyList())

    // when
    val testObserver = useCase.invoke().test()

    // then
    testObserver.assertComplete()
    verify {
      imageCacheProxy.size()
      userIconRepository.loadAllIcons()
    }
    verify(exactly = 2) {
      imageCacheProxy.sum()
    }
    confirmVerified(imageCacheProxy, userIconRepository, widgetManager, updateEventsManager)
  }
}
