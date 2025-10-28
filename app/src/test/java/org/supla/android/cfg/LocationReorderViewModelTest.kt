package org.supla.android.cfg
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
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Location

class LocationReorderViewModelTest {
  @RelaxedMockK
  private lateinit var channelRepository: ChannelRepository

  @InjectMockKs
  private lateinit var viewModel: LocationReorderViewModel

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should load locations from repository`() {
    // given
    val location1: Location = mockk()
    val location2: Location = mockk()
    every { channelRepository.allLocations } returns listOf(location1, location2)

    // when
    val result = viewModel.getLocations()

    // then
    assertThat(result).containsExactly(location1, location2)
  }

  @Test
  fun `should update locations`() {
    // given
    val location1: Location = mockk(relaxed = true)
    val location2: Location = mockk(relaxed = true)

    // when
    viewModel.onLocationsUpdate(arrayOf(location1, location2))

    // then
    verify {
      location1.sortOrder = 0
      location2.sortOrder = 1
      channelRepository.updateLocation(location1)
      channelRepository.updateLocation(location2)
    }
    confirmVerified(channelRepository, location1, location2)
  }
}
