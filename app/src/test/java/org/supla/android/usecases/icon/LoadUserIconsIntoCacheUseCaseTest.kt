package org.supla.android.usecases.icon

import io.reactivex.rxjava3.core.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
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
    val entity = UserIconEntity(
      123L,
      iconRemoteId,
      firstImage,
      byteArrayOf(),
      thirdImage,
      null,
      profileId
    )
    whenever(userIconRepository.loadAllIcons()).thenReturn(Observable.just(listOf(entity)))

    // when
    val testObserver = useCase.invoke().test()

    // then
    testObserver.assertComplete()
    verify(imageCacheProxy).addImage(ImageId(iconRemoteId, 1, profileId, true), firstImage)
    verify(imageCacheProxy).addImage(ImageId(iconRemoteId, 3, profileId, true), thirdImage)
    verify(userIconRepository).loadAllIcons()
    verifyNoMoreInteractions(imageCacheProxy, userIconRepository)
  }
}
