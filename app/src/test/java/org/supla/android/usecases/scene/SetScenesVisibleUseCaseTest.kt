package org.supla.android.usecases.scene

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.usecases.channel.VisibilityChange

class SetScenesVisibleUseCaseTest {

  @MockK
  private lateinit var sceneRepository: RoomSceneRepository

  @InjectMockKs
  private lateinit var useCase: SetScenesVisibleUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should set scenes visible`() {
    coEvery { sceneRepository.setScenesVisible(VisibilityChange.VISIBLE_TO_PROCESSING) } returns true

    val result = useCase(VisibilityChange.VISIBLE_TO_PROCESSING)

    assertThat(result).isTrue()
    coVerify { sceneRepository.setScenesVisible(VisibilityChange.VISIBLE_TO_PROCESSING) }
  }

  @Test
  fun `should return false when repository update fails`() {
    coEvery { sceneRepository.setScenesVisible(VisibilityChange.PROCESSING_TO_HIDE) } returns false

    val result = useCase(VisibilityChange.PROCESSING_TO_HIDE)

    assertThat(result).isFalse()
    coVerify { sceneRepository.setScenesVisible(VisibilityChange.PROCESSING_TO_HIDE) }
  }
}
