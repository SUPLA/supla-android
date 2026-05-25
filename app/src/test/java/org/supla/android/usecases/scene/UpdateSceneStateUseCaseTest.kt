package org.supla.android.usecases.scene

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.lib.SuplaSceneState
import java.util.Date

class UpdateSceneStateUseCaseTest {

  @MockK
  private lateinit var sceneRepository: RoomSceneRepository

  @InjectMockKs
  private lateinit var useCase: UpdateSceneStateUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should update scene state when found and changed`() {
    val state = suplaSceneState(sceneId = 123, startedAt = 1000, estimatedEndDate = 2000)
    val existingScene = sceneEntity(
      remoteId = 123,
      startedAt = null,
      estimatedEndDate = null,
      initiatorId = null,
      initiatorName = null
    )

    coEvery { sceneRepository.findByRemoteIdKtx(state.sceneId) } returns existingScene
    coEvery { sceneRepository.update(any<SceneEntity>()) } just Runs

    val result = useCase.invoke(state)

    assertThat(result).isTrue()

    val sceneSlot = slot<SceneEntity>()
    coVerify {
      sceneRepository.findByRemoteIdKtx(state.sceneId)
      sceneRepository.update(capture(sceneSlot))
    }
    confirmVerified(sceneRepository)

    with(sceneSlot.captured) {
      assertThat(id).isEqualTo(existingScene.id)
      assertThat(remoteId).isEqualTo(existingScene.remoteId)
      assertThat(startedAt).isEqualTo(readStartedAt(state))
      assertThat(estimatedEndDate).isEqualTo(readEstimatedEndDate(state))
      assertThat(initiatorId).isEqualTo(7)
      assertThat(initiatorName).isEqualTo("operator")
      assertThat(visible).isEqualTo(existingScene.visible)
      assertThat(sortOrder).isEqualTo(existingScene.sortOrder)
      assertThat(profileId).isEqualTo(existingScene.profileId)
    }
  }

  @Test
  fun `should not update scene state when found and unchanged`() {
    val state = suplaSceneState(sceneId = 123, startedAt = 1000, estimatedEndDate = 2000)
    val existingScene = sceneEntity(
      remoteId = state.sceneId,
      startedAt = readStartedAt(state),
      estimatedEndDate = readEstimatedEndDate(state),
      initiatorId = 7,
      initiatorName = "operator"
    )

    coEvery { sceneRepository.findByRemoteIdKtx(state.sceneId) } returns existingScene

    val result = useCase.invoke(state)

    assertThat(result).isFalse()

    coVerify {
      sceneRepository.findByRemoteIdKtx(state.sceneId)
    }
    confirmVerified(sceneRepository)
  }

  @Test
  fun `should not update scene state when not found`() {
    val state = suplaSceneState(sceneId = 123, startedAt = 1000, estimatedEndDate = 2000)

    coEvery { sceneRepository.findByRemoteIdKtx(state.sceneId) } returns null

    val result = useCase.invoke(state)

    assertThat(result).isFalse()

    coVerify {
      sceneRepository.findByRemoteIdKtx(state.sceneId)
    }
    confirmVerified(sceneRepository)
  }

  private fun suplaSceneState(
    sceneId: Int,
    startedAt: Long,
    estimatedEndDate: Long
  ) = SuplaSceneState(
    sceneId = sceneId,
    millisecondsFromStart = startedAt,
    millisecondsLeft = estimatedEndDate,
    initiatorId = 7,
    initiatorName = "operator",
    isEol = false
  )

  private fun sceneEntity(
    remoteId: Int,
    startedAt: Date?,
    estimatedEndDate: Date?,
    initiatorId: Int?,
    initiatorName: String?
  ) = SceneEntity(
    id = 321,
    remoteId = remoteId,
    locationId = 10,
    altIcon = 2,
    userIcon = 3,
    caption = "Scene",
    startedAt = startedAt,
    estimatedEndDate = estimatedEndDate,
    initiatorId = initiatorId,
    initiatorName = initiatorName,
    sortOrder = 7,
    visible = 1,
    profileId = "789"
  )

  private fun readStartedAt(state: SuplaSceneState): Date? = readPrivateField(state, "startedAt")

  private fun readEstimatedEndDate(state: SuplaSceneState): Date? =
    readPrivateField(state, "estimatedEndDate")

  @Suppress("UNCHECKED_CAST")
  private fun <T> readPrivateField(target: Any, name: String): T? {
    val field = target.javaClass.getDeclaredField(name)
    field.isAccessible = true
    return field.get(target) as T?
  }
}
