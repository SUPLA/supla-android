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
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.db.Location
import org.supla.android.lib.SuplaLocation

class UpdateLocationUseCaseTest {

  @MockK
  private lateinit var locationRepository: LocationRepository

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @InjectMockKs
  private lateinit var useCase: UpdateLocationUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should insert location when not found`() {
    val suplaLocation = suplaLocation(123, "Living room")
    val profile = profileEntity(456)

    every { locationRepository.findByRemoteId(suplaLocation.Id) } returns Maybe.empty()
    coEvery { locationRepository.insert(any()) } just Runs
    coEvery { profileRepository.findActiveProfileKtx() } returns profile

    val result = useCase.invoke(suplaLocation)

    assertThat(result).isTrue()

    val locationSlot = slot<LocationEntity>()
    verify {
      locationRepository.findByRemoteId(suplaLocation.Id)
    }
    coVerify {
      profileRepository.findActiveProfileKtx()
      locationRepository.insert(capture(locationSlot))
    }
    confirmVerified(locationRepository, profileRepository)

    with(locationSlot.captured) {
      assertThat(id).isNull()
      assertThat(remoteId).isEqualTo(suplaLocation.Id)
      assertThat(caption).isEqualTo(suplaLocation.Caption)
      assertThat(visible).isEqualTo(1)
      assertThat(collapsed).isEqualTo(0)
      assertThat(sorting).isEqualTo(Location.SortingType.DEFAULT)
      assertThat(sortOrder).isEqualTo(0)
      assertThat(profileId).isEqualTo(profile.id)
    }
  }

  @Test
  fun `should update location when found and changed`() {
    val suplaLocation = suplaLocation(123, "Kitchen")
    val existingLocation = locationEntity(remoteId = 123, caption = "Hallway", visible = 0)

    every { locationRepository.findByRemoteId(suplaLocation.Id) } returns Maybe.just(existingLocation)
    every { locationRepository.updateLocation(any()) } returns Completable.complete()

    val result = useCase.invoke(suplaLocation)

    assertThat(result).isTrue()

    val locationSlot = slot<LocationEntity>()
    verify {
      locationRepository.findByRemoteId(suplaLocation.Id)
      locationRepository.updateLocation(capture(locationSlot))
    }
    confirmVerified(locationRepository, profileRepository)

    with(locationSlot.captured) {
      assertThat(id).isEqualTo(existingLocation.id)
      assertThat(remoteId).isEqualTo(suplaLocation.Id)
      assertThat(caption).isEqualTo(suplaLocation.Caption)
      assertThat(visible).isEqualTo(1)
      assertThat(collapsed).isEqualTo(existingLocation.collapsed)
      assertThat(sorting).isEqualTo(existingLocation.sorting)
      assertThat(sortOrder).isEqualTo(existingLocation.sortOrder)
      assertThat(profileId).isEqualTo(existingLocation.profileId)
    }
  }

  @Test
  fun `should not update location when found and unchanged`() {
    val suplaLocation = suplaLocation(123, "Kitchen")
    val existingLocation = locationEntity(remoteId = 123, caption = "Kitchen")

    every { locationRepository.findByRemoteId(suplaLocation.Id) } returns Maybe.just(existingLocation)

    val result = useCase.invoke(suplaLocation)

    assertThat(result).isFalse()

    verify {
      locationRepository.findByRemoteId(suplaLocation.Id)
    }
    confirmVerified(locationRepository, profileRepository)
  }

  @Test
  fun `should not insert location when active profile not found`() {
    val suplaLocation = suplaLocation(123, "Living room")

    every { locationRepository.findByRemoteId(suplaLocation.Id) } returns Maybe.empty()
    coEvery { profileRepository.findActiveProfileKtx() } returns null

    val result = useCase.invoke(suplaLocation)

    assertThat(result).isFalse()

    verify {
      locationRepository.findByRemoteId(suplaLocation.Id)
    }
    coVerify {
      profileRepository.findActiveProfileKtx()
    }
    confirmVerified(locationRepository, profileRepository)
  }

  private fun suplaLocation(id: Int, caption: String) = SuplaLocation().apply {
    Id = id
    Caption = caption
  }

  private fun profileEntity(profileId: Long) = mockk<ProfileEntity> {
    every { id } returns profileId
  }

  private fun locationEntity(
    remoteId: Int,
    caption: String,
    visible: Int = 1
  ) = LocationEntity(
    id = 321,
    remoteId = remoteId,
    caption = caption,
    visible = visible,
    collapsed = 2,
    sorting = Location.SortingType.USER_DEFINED,
    sortOrder = 7,
    profileId = 456
  )
}
