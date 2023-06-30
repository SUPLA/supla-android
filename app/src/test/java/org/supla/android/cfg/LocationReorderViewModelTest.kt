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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Location

@RunWith(MockitoJUnitRunner::class)
class LocationReorderViewModelTest {
  @Mock
  private lateinit var channelRepository: ChannelRepository

  @InjectMocks
  private lateinit var viewModel: LocationReorderViewModel

  @Test
  fun `should load locations from repository`() {
    // given
    val location1 = mock(Location::class.java)
    val location2 = mock(Location::class.java)
    whenever(channelRepository.allLocations).thenReturn(listOf(location1, location2))

    // when
    val result = viewModel.getLocations()

    // then
    assertThat(result).containsExactly(location1, location2)
  }

  @Test
  fun `should update locations`() {
    // given
    val location1 = mock(Location::class.java)
    val location2 = mock(Location::class.java)

    // when
    viewModel.onLocationsUpdate(arrayOf(location1, location2))

    // then
    verify(location1).sortOrder = 0
    verify(location2).sortOrder = 1
    verify(channelRepository).updateLocation(location1)
    verify(channelRepository).updateLocation(location2)
    verifyNoMoreInteractions(channelRepository, location1, location2)
  }
}
