package org.supla.android.usecases.group
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.custom.LocationSortingType
import org.supla.android.lib.SuplaChannelGroup
import org.supla.core.shared.data.model.general.SuplaFunction

class UpdateChannelGroupUseCaseTest {

  @MockK
  private lateinit var locationRepository: LocationRepository

  @MockK
  private lateinit var channelGroupRepository: ChannelGroupRepository

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @InjectMockKs
  private lateinit var useCase: UpdateChannelGroupUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should insert channel group when not found`() {
    val suplaChannelGroup = suplaChannelGroup(234, 123, "caption", 1, 2, 3, 4)
    val location = locationEntity(remoteId = 123)
    val profile = profileEntity(456)

    coEvery { locationRepository.findByRemoteId(123) } returns Maybe.just(location)
    coEvery { channelGroupRepository.findByRemoteId(234) } returns Maybe.empty()
    coEvery { profileRepository.findActiveProfileKtx() } returns profile
    coEvery { channelGroupRepository.findMaxPositionInLocation(123) } returns null
    coEvery { channelGroupRepository.insert(any()) } returns Unit

    val result = useCase.invoke(suplaChannelGroup)

    assertThat(result).isTrue()

    val groupSlot = slot<ChannelGroupEntity>()
    coVerify {
      locationRepository.findByRemoteId(123)
      channelGroupRepository.findByRemoteId(234)
      profileRepository.findActiveProfileKtx()
      channelGroupRepository.findMaxPositionInLocation(123)
      channelGroupRepository.insert(capture(groupSlot))
    }
    confirmVerified(locationRepository, channelGroupRepository, profileRepository)

    with(groupSlot.captured) {
      assertThat(id).isNull()
      assertThat(remoteId).isEqualTo(234)
      assertThat(caption).isEqualTo("caption")
      assertThat(function).isEqualTo(SuplaFunction.from(1))
      assertThat(online).isEqualTo(0)
      assertThat(visible).isEqualTo(1)
      assertThat(locationId).isEqualTo(123)
      assertThat(altIcon).isEqualTo(3)
      assertThat(userIcon).isEqualTo(4)
      assertThat(flags).isEqualTo(2L)
      assertThat(totalValue).isNull()
      assertThat(position).isEqualTo(0)
      assertThat(profileId).isEqualTo(profile.id)
    }
  }

  @Test
  fun `should insert channel group on last position when positions defined`() {
    val suplaChannelGroup = suplaChannelGroup(234, 123, "caption", 1, 2, 3, 4)
    val location = locationEntity(remoteId = 123)
    val profile = profileEntity(456)

    coEvery { locationRepository.findByRemoteId(123) } returns Maybe.just(location)
    coEvery { channelGroupRepository.findByRemoteId(234) } returns Maybe.empty()
    coEvery { profileRepository.findActiveProfileKtx() } returns profile
    coEvery { channelGroupRepository.findMaxPositionInLocation(123) } returns 12
    coEvery { channelGroupRepository.insert(any()) } returns Unit

    val result = useCase.invoke(suplaChannelGroup)

    assertThat(result).isTrue()

    val groupSlot = slot<ChannelGroupEntity>()
    coVerify {
      channelGroupRepository.findMaxPositionInLocation(123)
      channelGroupRepository.insert(capture(groupSlot))
    }
    assertThat(groupSlot.captured.position).isEqualTo(13)
  }

  @Test
  fun `should update channel group when found`() {
    val suplaChannelGroup = suplaChannelGroup(234, 123, "caption", 1, 2, 3, 4)
    val location = locationEntity(remoteId = 123)
    val existing = channelGroupEntity(
      remoteId = 234,
      locationId = 123,
      caption = "caption",
      function = 1,
      flags = 2,
      altIcon = 3,
      userIcon = 4
    ).copy(visible = 0)

    coEvery { locationRepository.findByRemoteId(123) } returns Maybe.just(location)
    coEvery { channelGroupRepository.findByRemoteId(234) } returns Maybe.just(existing)
    coEvery { channelGroupRepository.updateEntity(any()) } returns Unit

    val result = useCase.invoke(suplaChannelGroup)

    assertThat(result).isTrue()

    val groupSlot = slot<ChannelGroupEntity>()
    coVerify {
      locationRepository.findByRemoteId(123)
      channelGroupRepository.findByRemoteId(234)
      channelGroupRepository.updateEntity(capture(groupSlot))
    }
    confirmVerified(locationRepository, channelGroupRepository, profileRepository)

    with(groupSlot.captured) {
      assertThat(id).isEqualTo(existing.id)
      assertThat(position).isEqualTo(existing.position)
      assertThat(visible).isEqualTo(1)
      assertThat(locationId).isEqualTo(123)
    }
  }

  @Test
  fun `should update channel group and change position when moved to another location`() {
    val suplaChannelGroup = suplaChannelGroup(234, 123, "caption", 1, 2, 3, 4)
    val location = locationEntity(remoteId = 123)
    val existing =
      channelGroupEntity(remoteId = 234, locationId = 456, caption = "caption", function = 1, flags = 2, altIcon = 3, userIcon = 4)

    coEvery { locationRepository.findByRemoteId(123) } returns Maybe.just(location)
    coEvery { channelGroupRepository.findByRemoteId(234) } returns Maybe.just(existing)
    coEvery { channelGroupRepository.findMaxPositionInLocation(123) } returns 0
    coEvery { channelGroupRepository.updateEntity(any()) } returns Unit

    val result = useCase.invoke(suplaChannelGroup)

    assertThat(result).isTrue()

    val groupSlot = slot<ChannelGroupEntity>()
    coVerify {
      channelGroupRepository.findMaxPositionInLocation(123)
      channelGroupRepository.updateEntity(capture(groupSlot))
    }
    assertThat(groupSlot.captured.locationId).isEqualTo(123)
    assertThat(groupSlot.captured.position).isEqualTo(0)
  }

  @Test
  fun `should not change channel group when nothing changed`() {
    val suplaChannelGroup = suplaChannelGroup(234, 123, "caption", 1, 2, 3, 4)
    val location = locationEntity(remoteId = 123)
    val existing =
      channelGroupEntity(remoteId = 234, locationId = 123, caption = "caption", function = 1, flags = 2, altIcon = 3, userIcon = 4)

    coEvery { locationRepository.findByRemoteId(123) } returns Maybe.just(location)
    coEvery { channelGroupRepository.findByRemoteId(234) } returns Maybe.just(existing)

    val result = useCase.invoke(suplaChannelGroup)

    assertThat(result).isFalse()

    coVerify {
      locationRepository.findByRemoteId(123)
      channelGroupRepository.findByRemoteId(234)
    }
    confirmVerified(locationRepository, channelGroupRepository, profileRepository)
  }

  @Test
  fun `should not update channel group when location not found`() {
    val suplaChannelGroup = suplaChannelGroup(234, 123, "caption", 1, 2, 3, 4)

    coEvery { locationRepository.findByRemoteId(123) } returns Maybe.empty()

    val result = useCase.invoke(suplaChannelGroup)

    assertThat(result).isFalse()

    coVerify {
      locationRepository.findByRemoteId(123)
    }
    confirmVerified(locationRepository, channelGroupRepository, profileRepository)
  }

  private fun suplaChannelGroup(
    id: Int,
    locationId: Int,
    caption: String,
    func: Int,
    flags: Int,
    altIcon: Int,
    userIcon: Int
  ) = SuplaChannelGroup().apply {
    Id = id
    LocationID = locationId
    Caption = caption
    Func = func
    Flags = flags.toLong()
    AltIcon = altIcon
    UserIcon = userIcon
  }

  private fun locationEntity(remoteId: Int) = LocationEntity(
    id = 1,
    remoteId = remoteId,
    caption = "Location",
    visible = 1,
    collapsed = 0,
    sorting = LocationSortingType.DEFAULT,
    sortOrder = 0,
    profileId = 456
  )

  private fun profileEntity(profileId: Long) = mockk<ProfileEntity> {
    every { id } returns profileId
  }

  private fun channelGroupEntity(
    remoteId: Int,
    locationId: Int,
    caption: String,
    function: Int,
    flags: Int,
    altIcon: Int,
    userIcon: Int
  ) = ChannelGroupEntity(
    id = 321,
    remoteId = remoteId,
    caption = caption,
    function = SuplaFunction.from(function),
    online = 0,
    visible = 1,
    locationId = locationId,
    altIcon = altIcon,
    userIcon = userIcon,
    flags = flags.toLong(),
    totalValue = null,
    position = 7,
    profileId = 456
  )
}
