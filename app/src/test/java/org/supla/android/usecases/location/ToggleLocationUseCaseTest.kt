package org.supla.android.usecases.location
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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
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
    val locationId = 123
    val location: LocationEntity = mockk {
      every { isCollapsed(flag) } returns (initialValue and flag.value > 0)
      every { collapsed } returns initialValue
    }
    val locationResult: LocationEntity = mockk()
    every { location.copy(collapsed = resultValue) } returns locationResult

    every { locationRepository.findByRemoteId(locationId) } returns Maybe.just(location)
    every { locationRepository.updateLocation(locationResult) } returns Completable.complete()

    // when
    val testObserver = useCase(locationId, flag).test()

    // then
    testObserver.assertComplete()

    verify {
      locationRepository.findByRemoteId(locationId)
      locationRepository.updateLocation(locationResult)
    }
    confirmVerified(locationRepository)
  }
}
