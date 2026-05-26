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
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.LocationSortingType
import org.supla.core.shared.data.model.general.SuplaFunction

class ReorderGroupsUseCaseTest {

  @MockK
  private lateinit var channelGroupRepository: ChannelGroupRepository

  private lateinit var useCase: ReorderGroupsUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    useCase = ReorderGroupsUseCase(channelGroupRepository)
  }

  @Test
  fun `should reorder groups and update only target location`() {
    // given
    val locationId = 21
    val location = location(locationId, "Living room")
    val otherLocation = location(22, "Kitchen")

    val first = groupDataEntity(groupEntity(id = 11L, remoteId = 101, locationId = locationId, position = 1), location)
    val second = groupDataEntity(groupEntity(id = 12L, remoteId = 102, locationId = locationId, position = 2), location)
    val third = groupDataEntity(groupEntity(id = 13L, remoteId = 103, locationId = locationId, position = 3), location)
    val otherLocationGroup = groupDataEntity(groupEntity(id = 14L, remoteId = 104, locationId = 22, position = 1), otherLocation)

    every { channelGroupRepository.findList() } returns Single.just(listOf(first, second, third, otherLocationGroup))
    every { channelGroupRepository.update(any()) } returns Completable.complete()

    // when
    useCase(firstItemId = 11L, firstItemLocationId = locationId, secondItemId = 13L)
      .test()
      .assertComplete()

    // then
    val captor = slot<List<ChannelGroupEntity>>()
    io.mockk.verify {
      channelGroupRepository.findList()
      channelGroupRepository.update(capture(captor))
    }

    assertThat(captor.captured)
      .extracting({ it.id }, { it.remoteId }, { it.locationId }, { it.position })
      .containsExactly(
        tuple(12L, 102, locationId, 1),
        tuple(13L, 103, locationId, 2),
        tuple(11L, 101, locationId, 3)
      )
  }

  private fun location(remoteId: Int, caption: String): LocationEntity =
    LocationEntity(
      id = remoteId.toLong(),
      remoteId = remoteId,
      caption = caption,
      visible = 1,
      collapsed = 0,
      sorting = LocationSortingType.DEFAULT,
      sortOrder = 1,
      profileId = 7L
    )

  private fun groupEntity(id: Long, remoteId: Int, locationId: Int, position: Int): ChannelGroupEntity =
    ChannelGroupEntity(
      id = id,
      remoteId = remoteId,
      caption = "Group $remoteId",
      online = 0,
      function = SuplaFunction.UNKNOWN,
      visible = 1,
      locationId = locationId,
      altIcon = 0,
      userIcon = 0,
      flags = 0,
      totalValue = null,
      position = position,
      profileId = 7L
    )

  private fun groupDataEntity(groupEntity: ChannelGroupEntity, locationEntity: LocationEntity): ChannelGroupDataEntity =
    ChannelGroupDataEntity(
      channelGroupEntity = groupEntity,
      locationEntity = locationEntity
    )
}
