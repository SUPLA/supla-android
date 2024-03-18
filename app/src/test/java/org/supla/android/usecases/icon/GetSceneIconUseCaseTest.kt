package org.supla.android.usecases.icon

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.supla.android.R
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.images.ImageCacheProxy
import org.supla.android.images.ImageId

@RunWith(MockitoJUnitRunner::class)
class GetSceneIconUseCaseTest {

  @Mock
  private lateinit var imageCacheProxy: ImageCacheProxy

  @InjectMocks
  private lateinit var useCase: GetSceneIconUseCase

  @Test
  fun `should get alt icon`() {
    // given
    val scene: SceneEntity = mockk {
      every { userIcon } returns 0
      every { altIcon } returns 4
    }

    // when
    val imageId = useCase.invoke(scene)

    // then
    assertThat(imageId.id).isEqualTo(R.drawable.scene4)
  }

  @Test
  fun `should get default icon when alt icon is wrong`() {
    // given
    val scene: SceneEntity = mockk {
      every { userIcon } returns 0
      every { altIcon } returns 37
    }

    // when
    val imageId = useCase.invoke(scene)

    // then
    assertThat(imageId.id).isEqualTo(R.drawable.scene0)
  }

  @Test
  fun `should get night icon`() {
    // given
    val scene: SceneEntity = mockk {
      every { userIcon } returns 0
      every { altIcon } returns 5
    }

    // when
    val imageId = useCase.invoke(scene, true)

    // then
    assertThat(imageId.id).isEqualTo(R.drawable.scene5_nightmode)
  }

  @Test
  fun `should get user icon`() {
    // given
    val userIconId = 3
    val profileId = "123"
    val scene: SceneEntity = mockk {
      every { userIcon } returns userIconId
      every { altIcon } returns 5
      every { this@mockk.profileId } returns profileId
    }

    whenever(imageCacheProxy.bitmapExists(eq(ImageId(userIconId, 1, 123L)))).thenReturn(true)

    // when
    val imageId = useCase.invoke(scene, true)

    // then
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.subId).isEqualTo(1)
    assertThat(imageId.profileId).isEqualTo(123L)
  }
}
