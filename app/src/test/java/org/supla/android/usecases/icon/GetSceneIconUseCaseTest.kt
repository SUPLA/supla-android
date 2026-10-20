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
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.R
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.images.ImageCacheProxy
import org.supla.android.images.ImageId

class GetSceneIconUseCaseTest {

  @MockK
  private lateinit var imageCacheProxy: ImageCacheProxy

  @InjectMockKs
  private lateinit var useCase: GetSceneIconUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get alt icon`() {
    // given
    val scene: SceneEntity = mockk {
      every { userIcon } returns 0
      every { altIcon } returns 4
      every { profileId } returns null
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
      every { profileId } returns null
    }

    // when
    val imageId = useCase.invoke(scene)

    // then
    assertThat(imageId.id).isEqualTo(R.drawable.scene0)
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

    every { imageCacheProxy.bitmapExists(eq(ImageId(userIconId, 1, 123L))) } returns true

    // when
    val imageId = useCase.invoke(scene)

    // then
    assertThat(imageId.id).isEqualTo(userIconId)
    assertThat(imageId.subId).isEqualTo(1)
    assertThat(imageId.profileId).isEqualTo(123L)
  }
}
