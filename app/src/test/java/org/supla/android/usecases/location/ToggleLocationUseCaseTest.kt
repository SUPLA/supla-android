package org.supla.android.usecases.location

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.local.entity.LocationEntity

class ToggleLocationUseCaseTest {

  @MockK
  private lateinit var locationRepository: LocationRepository

  @InjectMockKs
  private lateinit var useCase: ToggleLocationUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

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

    every { locationRepository.updateLocation(locationResult) } returns Completable.complete()

    // when
    val testObserver = useCase(location, flag).test()

    // then
    testObserver.assertComplete()

    verify {
      locationRepository.updateLocation(locationResult)
    }
    confirmVerified(locationRepository)
  }
}
