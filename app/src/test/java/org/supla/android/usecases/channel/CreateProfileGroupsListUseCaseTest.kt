package org.supla.android.usecases.channel
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

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.group.CreateProfileGroupsListUseCase
import org.supla.android.usecases.group.GroupToListItemMapper
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase

class CreateProfileGroupsListUseCaseTest {
  @MockK
  private lateinit var channelGroupRepository: ChannelGroupRepository

  @MockK
  private lateinit var groupToListItemMapper: GroupToListItemMapper

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @MockK
  private lateinit var context: Context

  @InjectMockKs
  private lateinit var usecase: CreateProfileGroupsListUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should create list of channels and locations`() {
    // given
    val firstLocationId = 1
    val collapsedLocationId = 2
    val thirdLocationId = 3

    val firstGroup = mockGroupData(11, firstLocationId, "Location")
    val secondGroup = mockGroupData(22, firstLocationId, "Location")
    val thirdGroup = mockGroupData(33, collapsedLocationId, "Collapsed location", true)
    val fourthGroup = mockGroupData(44, thirdLocationId)

    every { channelGroupRepository.findList() } returns Single.just(listOf(firstGroup, secondGroup, thirdGroup, fourthGroup))
    every { groupToListItemMapper(firstGroup) } returns mockGroupItem(11)
    every { groupToListItemMapper(secondGroup) } returns mockGroupItem(22)
    every { groupToListItemMapper(thirdGroup) } returns mockGroupItem(33)
    every { groupToListItemMapper(fourthGroup) } returns mockGroupItem(44)

    // when
    val testObserver = usecase.invoke().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(6)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.DefaultItem::class.java)

    assertThat((list[1] as ListItem.DefaultItem).remoteId).isEqualTo(11)
    assertThat((list[2] as ListItem.DefaultItem).remoteId).isEqualTo(22)
    assertThat((list[5] as ListItem.DefaultItem).remoteId).isEqualTo(44)

    assertThat((list[0] as ListItem.LocationItem).remoteId).isEqualTo(firstLocationId)
    assertThat((list[3] as ListItem.LocationItem).remoteId).isEqualTo(collapsedLocationId)
    assertThat((list[4] as ListItem.LocationItem).remoteId).isEqualTo(thirdLocationId)
  }

  @Test
  fun `should merge location with same name into one`() {
    // given
    val firstLocationId = 1
    val secondLocationId = 2
    val thirdLocationId = 3

    val firstGroup = mockGroupData(11, firstLocationId, "Location")
    val secondGroup = mockGroupData(22, firstLocationId, "Location")
    val thirdGroup = mockGroupData(33, secondLocationId, "Location")
    val fourthGroup = mockGroupData(44, thirdLocationId)

    every { channelGroupRepository.findList() } returns Single.just(listOf(firstGroup, secondGroup, thirdGroup, fourthGroup))
    every { groupToListItemMapper(firstGroup) } returns mockGroupItem(11)
    every { groupToListItemMapper(secondGroup) } returns mockGroupItem(22)
    every { groupToListItemMapper(thirdGroup) } returns mockGroupItem(33)
    every { groupToListItemMapper(fourthGroup) } returns mockGroupItem(44)

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(6)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.DefaultItem::class.java)

    assertThat((list[1] as ListItem.DefaultItem).remoteId).isEqualTo(11)
    assertThat((list[2] as ListItem.DefaultItem).remoteId).isEqualTo(22)
    assertThat((list[3] as ListItem.DefaultItem).remoteId).isEqualTo(33)
    assertThat((list[5] as ListItem.DefaultItem).remoteId).isEqualTo(44)

    assertThat((list[0] as ListItem.LocationItem).remoteId).isEqualTo(firstLocationId)
    assertThat((list[4] as ListItem.LocationItem).remoteId).isEqualTo(thirdLocationId)
  }

  @Test
  fun `should filter groups by caption`() {
    // given
    val firstGroup = mockGroupData(11, 1, "Location")
    val secondGroup = mockGroupData(22, 1, "Location")
    every { channelGroupRepository.findList() } returns Single.just(listOf(firstGroup, secondGroup))
    every { groupToListItemMapper(firstGroup) } returns mockGroupItem(11)

    // when
    val testObserver = usecase("caption 1").test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(2)
    assertThat((list[0] as ListItem.LocationItem).remoteId).isEqualTo(1)
    assertThat((list[1] as ListItem.DefaultItem).remoteId).isEqualTo(11)
  }

  private fun mockGroupData(
    groupRemoteId: Int,
    locationRemoteId: Int,
    locationCaption: String = "",
    locationCollapsed: Boolean = false
  ): ChannelGroupDataEntity {
    val location: LocationEntity = mockk {
      every { profileId } returns 1L
      every { remoteId } returns locationRemoteId
      every { caption } returns locationCaption
      every { isCollapsed(CollapsedFlag.GROUP) } returns locationCollapsed
    }

    every { getCaptionUseCase.invoke(match { it.remoteId == groupRemoteId }) } returns
      LocalizedString.Constant("caption $groupRemoteId")

    return mockk {
      every { remoteId } returns groupRemoteId
      every { function } returns SuplaFunction.NONE
      every { locationEntity } returns location
      every { getLegacyGroup() } returns mockk()
      every { locationId } returns locationRemoteId
      every { caption } returns "caption $groupRemoteId"
    }
  }

  private fun mockGroupItem(remoteId: Int): ListItem.DefaultItem = mockk {
    every { this@mockk.remoteId } returns remoteId
  }
}
