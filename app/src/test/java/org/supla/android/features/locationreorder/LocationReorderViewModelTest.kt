package org.supla.android.features.locationreorder
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
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.MainDispatcherRule
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.db.Location
import org.supla.android.tools.SuplaSchedulers

class LocationReorderViewModelTest :
  BaseViewModelTest<LocationReorderViewState, ViewEvent, LocationReorderViewModel>(MockSchedulers.MOCKK) {

  @get:Rule
  override val mainDispatcherRule = MainDispatcherRule()

  @MockK
  private lateinit var locationRepository: LocationRepository

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: LocationReorderViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should load locations from repository`() {
    // given
    val location1: LocationEntity = mockk(relaxed = true)
    val location2: LocationEntity = mockk(relaxed = true)
    coEvery { locationRepository.getAllLocations() } returns listOf(location1, location2)

    // when
    viewModel.onViewCreated()

    // then
    assertThat(states).containsExactly(
      LocationReorderViewState(listOf(location1, location2))
    )
    assertThat(events).isEmpty()

    coVerify {
      locationRepository.getAllLocations()
    }
    confirmVerified(locationRepository)
  }

  @Test
  fun `should reorder locations locally`() {
    // given
    val location1: LocationEntity = mockk(relaxed = true)
    val location2: LocationEntity = mockk(relaxed = true)
    val location3: LocationEntity = mockk(relaxed = true)
    coEvery { locationRepository.getAllLocations() } returns listOf(location1, location2, location3)

    viewModel.onViewCreated()

    // when
    viewModel.onMove(0, 2)

    // then
    assertThat(states.last()).isEqualTo(
      LocationReorderViewState(listOf(location2, location3, location1))
    )
    coVerify {
      locationRepository.getAllLocations()
    }
    confirmVerified(locationRepository)
  }

  @Test
  fun `should persist new locations order when drag finishes`() {
    // given
    val location1: LocationEntity = mockk(relaxed = true)
    val location2: LocationEntity = mockk(relaxed = true)
    val location3: LocationEntity = mockk(relaxed = true)
    coEvery { locationRepository.getAllLocations() } returns listOf(location1, location2, location3)
    every { locationRepository.updateLocation(location2.copy(sortOrder = 0)) } returns Completable.complete()
    every { locationRepository.updateLocation(location3.copy(sortOrder = 1)) } returns Completable.complete()
    every { locationRepository.updateLocation(location1.copy(sortOrder = 2)) } returns Completable.complete()

    viewModel.onViewCreated()
    viewModel.onMove(0, 2)

    // when
    viewModel.onMoveFinished()

    // then
    verify {
      locationRepository.updateLocation(location2.copy(sortOrder = 0))
      locationRepository.updateLocation(location3.copy(sortOrder = 1))
      locationRepository.updateLocation(location1.copy(sortOrder = 2))
    }
    coVerify { locationRepository.getAllLocations() }
    confirmVerified(locationRepository)
  }
}
