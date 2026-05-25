package org.supla.android.usecases.scene
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
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.lib.SuplaScene

class UpdateSceneUseCaseTest {

  @MockK
  private lateinit var sceneRepository: RoomSceneRepository

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @InjectMockKs
  private lateinit var useCase: UpdateSceneUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should insert scene when not found`() {
    val suplaScene = suplaScene(123, 456, "Living room")
    val profile = profileEntity(789)

    coEvery { sceneRepository.findByRemoteIdKtx(suplaScene.id) } returns null
    coEvery { profileRepository.findActiveProfileKtx() } returns profile
    coEvery { sceneRepository.insert(any()) } just Runs

    val result = useCase.invoke(suplaScene)

    assertThat(result).isTrue()

    val sceneSlot = slot<SceneEntity>()
    coVerify {
      sceneRepository.findByRemoteIdKtx(suplaScene.id)
      profileRepository.findActiveProfileKtx()
      sceneRepository.insert(capture(sceneSlot))
    }
    confirmVerified(sceneRepository, profileRepository)

    with(sceneSlot.captured) {
      assertThat(id).isNull()
      assertThat(remoteId).isEqualTo(suplaScene.id)
      assertThat(locationId).isEqualTo(suplaScene.locationId)
      assertThat(altIcon).isEqualTo(suplaScene.altIcon)
      assertThat(userIcon).isEqualTo(suplaScene.userIcon)
      assertThat(caption).isEqualTo(suplaScene.caption)
      assertThat(visible).isEqualTo(1)
      assertThat(sortOrder).isEqualTo(0)
      assertThat(profileId).isEqualTo(profile.id.toString())
    }
  }

  @Test
  fun `should update scene when found and changed`() {
    val suplaScene = suplaScene(123, 456, "Kitchen")
    val existingScene = sceneEntity(
      remoteId = 123,
      locationId = 111,
      caption = "Hallway",
      visible = 0
    )

    coEvery { sceneRepository.findByRemoteIdKtx(suplaScene.id) } returns existingScene
    coEvery { sceneRepository.update(any<SceneEntity>()) } just Runs

    val result = useCase.invoke(suplaScene)

    assertThat(result).isTrue()

    val sceneSlot = slot<SceneEntity>()
    coVerify {
      sceneRepository.findByRemoteIdKtx(suplaScene.id)
      sceneRepository.update(capture(sceneSlot))
    }
    confirmVerified(sceneRepository, profileRepository)

    with(sceneSlot.captured) {
      assertThat(id).isEqualTo(existingScene.id)
      assertThat(remoteId).isEqualTo(suplaScene.id)
      assertThat(locationId).isEqualTo(suplaScene.locationId)
      assertThat(altIcon).isEqualTo(suplaScene.altIcon)
      assertThat(userIcon).isEqualTo(suplaScene.userIcon)
      assertThat(caption).isEqualTo(suplaScene.caption)
      assertThat(visible).isEqualTo(1)
      assertThat(sortOrder).isEqualTo(existingScene.sortOrder)
      assertThat(profileId).isEqualTo(existingScene.profileId)
    }
  }

  @Test
  fun `should not update scene when found and unchanged`() {
    val suplaScene = suplaScene(123, 456, "Kitchen")
    val existingScene = sceneEntity(
      remoteId = suplaScene.id,
      locationId = suplaScene.locationId,
      altIcon = suplaScene.altIcon,
      userIcon = suplaScene.userIcon,
      caption = suplaScene.caption,
      visible = 1
    )

    coEvery { sceneRepository.findByRemoteIdKtx(suplaScene.id) } returns existingScene

    val result = useCase.invoke(suplaScene)

    assertThat(result).isFalse()

    coVerify {
      sceneRepository.findByRemoteIdKtx(suplaScene.id)
    }
    confirmVerified(sceneRepository, profileRepository)
  }

  @Test
  fun `should not insert scene when active profile not found`() {
    val suplaScene = suplaScene(123, 456, "Living room")

    coEvery { sceneRepository.findByRemoteIdKtx(suplaScene.id) } returns null
    coEvery { profileRepository.findActiveProfileKtx() } returns null

    val result = useCase.invoke(suplaScene)

    assertThat(result).isFalse()

    coVerify {
      sceneRepository.findByRemoteIdKtx(suplaScene.id)
      profileRepository.findActiveProfileKtx()
    }
    confirmVerified(sceneRepository, profileRepository)
  }

  private fun suplaScene(id: Int, locationId: Int, caption: String) = SuplaScene(
    id = id,
    locationId = locationId,
    altIcon = 2,
    userIcon = 3,
    caption = caption,
    isEol = false
  )

  private fun profileEntity(profileId: Long) = mockk<ProfileEntity> {
    every { id } returns profileId
  }

  private fun sceneEntity(
    remoteId: Int,
    locationId: Int,
    altIcon: Int = 2,
    userIcon: Int = 3,
    caption: String,
    visible: Int = 1
  ) = SceneEntity(
    id = 321,
    remoteId = remoteId,
    locationId = locationId,
    altIcon = altIcon,
    userIcon = userIcon,
    caption = caption,
    startedAt = null,
    estimatedEndDate = null,
    initiatorId = null,
    initiatorName = null,
    sortOrder = 7,
    visible = visible,
    profileId = "789"
  )
}
