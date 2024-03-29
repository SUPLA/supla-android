package org.supla.android.usecases.location

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.local.entity.LocationEntity

@RunWith(MockitoJUnitRunner::class)
class ToggleLocationUseCaseTest {

  @Mock
  private lateinit var locationRepository: LocationRepository

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

  private fun testLocationToggle(initialValue: Int, resultValue: Int, flag: CollapsedFlag) {
    // given
    val location: LocationEntity = mockk {
      every { isCollapsed(flag) } returns (initialValue and flag.value > 0)
      every { collapsed } returns initialValue
    }
    val locationResult: LocationEntity = mockk()
    every { location.copy(collapsed = resultValue) } returns locationResult

    whenever(locationRepository.updateLocation(locationResult)).thenReturn(Completable.complete())

    // when
    val testObserver = useCase(location, flag).test()

    // then
    testObserver.assertComplete()

    verify(locationRepository).updateLocation(locationResult)
    verifyNoMoreInteractions(locationRepository)
  }
}
