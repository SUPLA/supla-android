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
import io.mockk.verify
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase

class CreateProfileChannelsListUseCaseTest {

  @MockK
  private lateinit var observeChannelsTreeSnapshotUseCase: ObserveChannelsTreeSnapshotUseCase

  @MockK
  private lateinit var channelToListItemMapper: ChannelToListItemMapper

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @MockK
  private lateinit var preferences: ApplicationPreferences

  @MockK
  private lateinit var context: Context

  @InjectMockKs
  private lateinit var usecase: CreateProfileChannelsListUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should create list of channels and locations`() {
    // given
    val first = mockListEntity(11, 12)
    val second = mockListEntity(21, 12)
    val third = mockListEntity(31, 32, locationCollapsed = true)
    val fourth = mockListEntity(41, 42)
    val fifth = mockListEntity(51, 42)
    val sixth = mockListEntity(61, 42)
    val snapshot = channelsTreeSnapshot(listOf(first, second, third, fourth, fifth, sixth))

    every { preferences.hideUnavailableChannels } returns false
    every { observeChannelsTreeSnapshotUseCase() } returns Observable.just(snapshot)

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(8)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[6]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[7]).isInstanceOf(ListItem.DefaultItem::class.java)

    assertThat((list[1] as ListItem.DefaultItem).remoteId).isEqualTo(11)
    assertThat((list[2] as ListItem.DefaultItem).remoteId).isEqualTo(21)
    assertThat((list[5] as ListItem.DefaultItem).remoteId).isEqualTo(41)
    assertThat((list[6] as ListItem.DefaultItem).remoteId).isEqualTo(51)
    assertThat((list[7] as ListItem.DefaultItem).remoteId).isEqualTo(61)

    assertThat((list[0] as ListItem.LocationItem).userCaption).isEqualTo("12")
    assertThat((list[3] as ListItem.LocationItem).userCaption).isEqualTo("32")
    assertThat((list[4] as ListItem.LocationItem).userCaption).isEqualTo("42")
  }

  @Test
  fun `should create list of channels and locations - filtered`() {
    // given
    val first = mockListEntity(101, 12)
    val second = mockListEntity(102, 12)
    val third = mockListEntity(103, 32, locationCollapsed = true)
    val fourth = mockListEntity(104, 42)
    val fifth = mockListEntity(111, 42)
    val sixth = mockListEntity(113, 42)
    val snapshot = channelsTreeSnapshot(listOf(first, second, third, fourth, fifth, sixth))

    every { preferences.hideUnavailableChannels } returns false
    every { observeChannelsTreeSnapshotUseCase() } returns Observable.just(snapshot)

    // when
    val testObserver = usecase("caption 10").test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(7)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[6]).isInstanceOf(ListItem.DefaultItem::class.java)

    assertThat((list[1] as ListItem.DefaultItem).remoteId).isEqualTo(101)
    assertThat((list[2] as ListItem.DefaultItem).remoteId).isEqualTo(102)
    assertThat((list[4] as ListItem.DefaultItem).remoteId).isEqualTo(103)
    assertThat((list[6] as ListItem.DefaultItem).remoteId).isEqualTo(104)

    assertThat((list[0] as ListItem.LocationItem).userCaption).isEqualTo("12")
    assertThat((list[3] as ListItem.LocationItem).userCaption).isEqualTo("32")
    assertThat((list[5] as ListItem.LocationItem).userCaption).isEqualTo("42")
  }

  @Test
  fun `should merge location with same name into one`() {
    // given
    val first = mockListEntity(11, 12, locationSortOrder = 1, position = 1)
    val second = mockListEntity(21, 12, locationSortOrder = 1, position = 3)
    val third = mockListEntity(31, 32, locationName = "12", locationSortOrder = 2, position = 2)
    val fourth = mockListEntity(41, 42, locationSortOrder = 3, position = 4)
    val channels = listOf(first, second, third, fourth)
    val snapshot = channelsTreeSnapshot(channels)

    every { preferences.hideUnavailableChannels } returns true
    every { observeChannelsTreeSnapshotUseCase() } returns Observable.just(snapshot)

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
    assertThat((list[2] as ListItem.DefaultItem).remoteId).isEqualTo(31)
    assertThat((list[3] as ListItem.DefaultItem).remoteId).isEqualTo(21)
    assertThat((list[5] as ListItem.DefaultItem).remoteId).isEqualTo(41)

    assertThat((list[0] as ListItem.LocationItem).userCaption).isEqualTo("12")
    assertThat((list[4] as ListItem.LocationItem).userCaption).isEqualTo("42")
  }

  @Test
  fun `should load children`() {
    // given
    val first = mockListEntity(11, 12)
    val second = mockListEntity(21, 12)
    val third = mockListEntity(31, 12)

    every { preferences.hideUnavailableChannels } returns false
    val childrenRelation = mockk<ChannelRelationEntity> {
      every { channelId } returns 21
      every { parentId } returns 11
      every { relationType } returns ChannelRelationType.DEFAULT
    }
    val parentWithChildren = ChannelWithChildren(first, listOf(ChannelChildEntity(childrenRelation, second)))
    val snapshot = channelsTreeSnapshot(
      channels = listOf(first, second, third),
      childChannelIds = setOf(second.remoteId),
      channelsWithChildren = mapOf(
        first.remoteId to parentWithChildren,
        second.remoteId to ChannelWithChildren(second),
        third.remoteId to ChannelWithChildren(third)
      )
    )
    every { observeChannelsTreeSnapshotUseCase() } returns Observable.just(snapshot)
    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(3)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.DefaultItem::class.java)
    assertThat((list[0] as ListItem.LocationItem).userCaption).isEqualTo("12")
    assertThat((list[1] as ListItem.DefaultItem).remoteId).isEqualTo(11)
    assertThat((list[2] as ListItem.DefaultItem).remoteId).isEqualTo(31)
    verify { channelToListItemMapper(parentWithChildren) }
  }

  @Test
  fun `should hide unavailable channels`() {
    // given
    val unavailable = mockListEntity(
      channelRemoteId = 11,
      locationRemoteId = 12,
      lastOnlineStateValue = SuplaChannelAvailabilityStatus.ONLINE_BUT_NOT_AVAILABLE
    )
    val available = mockListEntity(channelRemoteId = 21, locationRemoteId = 12)
    val snapshot = channelsTreeSnapshot(listOf(unavailable, available))

    every { preferences.hideUnavailableChannels } returns true
    every { observeChannelsTreeSnapshotUseCase() } returns Observable.just(snapshot)

    // when
    val testObserver = usecase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values().first()
    assertThat(list).hasSize(2)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat((list[1] as ListItem.DefaultItem).remoteId).isEqualTo(21)
    verify(exactly = 0) { channelToListItemMapper(snapshot.channelsWithChildren.getValue(11)) }
  }

  private fun channelsTreeSnapshot(
    channels: List<ChannelDataEntity>,
    childChannelIds: Set<Int> = emptySet(),
    channelsWithChildren: Map<Int, ChannelWithChildren> = channels.associate { it.remoteId to ChannelWithChildren(it) }
  ) = ChannelsTreeSnapshot(channels, channelsWithChildren, childChannelIds)

  private fun mockListEntity(
    channelRemoteId: Int,
    locationRemoteId: Int,
    locationName: String = "$locationRemoteId",
    locationCollapsed: Boolean = false,
    locationSortOrder: Int = locationRemoteId,
    position: Int = channelRemoteId,
    lastOnlineStateValue: SuplaChannelAvailabilityStatus? = null,
  ): ChannelDataEntity {
    val channelEntityMock = mockk<ChannelEntity>()
    every { channelEntityMock.position } returns position
    val channelValueEntityMock = mockk<ChannelValueEntity> {
      every { lastOnlineState } returns lastOnlineStateValue
      every { getValueAsByteArray() } returns byteArrayOf()
    }

    return mockk {
      every { remoteId } returns channelRemoteId
      every { caption } returns "caption $channelRemoteId"
      every { function } returns SuplaFunction.NONE
      every { altIcon } returns 0
      every { stateEntity } returns null
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { locationEntity } returns mockk {
        every { profileId } returns 1L
        every { remoteId } returns locationRemoteId
        every { caption } returns locationName
        every { sortOrder } returns locationSortOrder
        every { isCollapsed(CollapsedFlag.CHANNEL) } returns locationCollapsed
      }
      every { channelEntity } returns channelEntityMock
      every { channelValueEntity } returns channelValueEntityMock

      val listItem: ListItem.DefaultItem = mockk {
        every { remoteId } returns channelRemoteId
      }

      every { channelToListItemMapper.invoke(match { it.remoteId == channelRemoteId }) } returns
        listItem
      every { getCaptionUseCase.invoke(match { it.remoteId == channelRemoteId }) } returns
        LocalizedString.Constant("caption $channelRemoteId")
    }
  }
}
