package org.supla.android.usecases.channel

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.RoomChannelRepository

class SetChannelsVisibleUseCaseTest {

  @MockK
  private lateinit var channelRepository: RoomChannelRepository

  @InjectMockKs
  private lateinit var useCase: SetChannelsVisibleUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should set channels visible`() {
    coEvery { channelRepository.setChannelsVisible(VisibilityChange.VISIBLE_TO_PROCESSING) } returns true

    val result = useCase(VisibilityChange.VISIBLE_TO_PROCESSING)

    assertThat(result).isTrue()
    coVerify { channelRepository.setChannelsVisible(VisibilityChange.VISIBLE_TO_PROCESSING) }
  }

  @Test
  fun `should return false when repository update fails`() {
    coEvery { channelRepository.setChannelsVisible(VisibilityChange.PROCESSING_TO_HIDE) } returns false

    val result = useCase(VisibilityChange.PROCESSING_TO_HIDE)

    assertThat(result).isFalse()
    coVerify { channelRepository.setChannelsVisible(VisibilityChange.PROCESSING_TO_HIDE) }
  }
}
