package org.supla.android.usecases.location

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Location

@RunWith(MockitoJUnitRunner::class)
class ToggleLocationUseCaseTest {

  @Mock
  private lateinit var channelRepository: ChannelRepository

  @InjectMocks
  private lateinit var useCase: ToggleLocationUseCase

  @Test
  fun `should close location in channels`() {
    val type = CollapsedFlag.CHANNEL
    testLocationToggle(0, 0 or type.value, type)
  }

  @Test
  fun `should open location in channels`() {
    val type = CollapsedFlag.CHANNEL
    testLocationToggle(0 or type.value, 0, type)
  }

  @Test
  fun `should close location in groups`() {
    val type = CollapsedFlag.GROUP
    testLocationToggle(0, 0 or type.value, type)
  }

  @Test
  fun `should open location in groups`() {
    val type = CollapsedFlag.GROUP
    testLocationToggle(0 or type.value, 0, type)
  }

  @Test
  fun `should close location in scenes`() {
    val type = CollapsedFlag.SCENE
    testLocationToggle(0, 0 or type.value, type)
  }

  @Test
  fun `should open location in scenes`() {
    val type = CollapsedFlag.SCENE
    testLocationToggle(0 or type.value, 0, type)
  }

  private fun testLocationToggle(initialValue: Int, resultValue: Int, type: CollapsedFlag) {
    // given
    val location: Location = mockk()
    every { location.collapsed } returns initialValue
    every { location.collapsed = resultValue } answers { }

    // when
    val testObserver = useCase(location, type).test()

    // then
    testObserver.assertComplete()

    verify(channelRepository).updateLocation(location)
    verifyNoMoreInteractions(channelRepository)

    io.mockk.verify { location.collapsed = resultValue }
  }
}
