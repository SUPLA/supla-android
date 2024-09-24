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

import io.reactivex.rxjava3.core.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.RoomUserIconRepository
import org.supla.android.data.source.local.entity.UserIconEntity
import org.supla.android.images.ImageCacheProxy
import org.supla.android.images.ImageId

@RunWith(MockitoJUnitRunner::class)
class LoadUserIconsIntoCacheUseCaseTest {

  @Mock
  private lateinit var userIconRepository: RoomUserIconRepository

  @Mock
  private lateinit var imageCacheProxy: ImageCacheProxy

  @InjectMocks
  private lateinit var useCase: LoadUserIconsIntoCacheUseCase

  @Test
  fun `should add image when available`() {
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
    whenever(imageCacheProxy.sum()).thenReturn(0, 3)
    whenever(imageCacheProxy.size()).thenReturn(3)
    whenever(userIconRepository.loadAllIcons()).thenReturn(Observable.just(listOf(entity)))

    // when
    val testObserver = useCase.invoke().test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(IconsCacheStatistics(3, true))
    verify(imageCacheProxy).addImage(ImageId(iconRemoteId, 1, profileId), firstImage)
    verify(imageCacheProxy).addImage(ImageId(iconRemoteId, 3, profileId), thirdImage)
    verify(imageCacheProxy).addImage(ImageId(iconRemoteId, 1, profileId).setNightMode(true), nightImage)
    verify(imageCacheProxy, times(2)).sum()
    verify(imageCacheProxy).size()
    verify(userIconRepository).loadAllIcons()
    verifyNoMoreInteractions(imageCacheProxy, userIconRepository)
  }
}
