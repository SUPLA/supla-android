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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase

class CreateProfileChannelsListUseCaseTest {

  @MockK
  private lateinit var getChannelChildrenTreeUseCase: GetChannelChildrenTreeUseCase

  @MockK
  private lateinit var channelRelationRepository: ChannelRelationRepository

  @MockK
  private lateinit var channelToListItemMapper: ChannelToListItemMapper

  @MockK
  private lateinit var channelRepository: ChannelRepository

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

    every { preferences.hideUnavailableChannels } returns false
    every { channelRepository.findList() } returns Single.just(listOf(first, second, third, fourth, fifth, sixth))
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(emptyMap())

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

    every { preferences.hideUnavailableChannels } returns false
    every { channelRepository.findList() } returns Single.just(listOf(first, second, third, fourth, fifth, sixth))
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(emptyMap())

    // when
    val testObserver = usecase("caption 10").test()

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

    assertThat((list[1] as ListItem.DefaultItem).remoteId).isEqualTo(101)
    assertThat((list[2] as ListItem.DefaultItem).remoteId).isEqualTo(102)
    assertThat((list[5] as ListItem.DefaultItem).remoteId).isEqualTo(104)

    assertThat((list[0] as ListItem.LocationItem).userCaption).isEqualTo("12")
    assertThat((list[3] as ListItem.LocationItem).userCaption).isEqualTo("32")
    assertThat((list[4] as ListItem.LocationItem).userCaption).isEqualTo("42")
  }

  @Test
  fun `should merge location with same name into one`() {
    // given
    val first = mockListEntity(11, 12)
    val second = mockListEntity(21, 12)
    val third = mockListEntity(31, 32, locationName = "12")
    val fourth = mockListEntity(41, 42)

    every { preferences.hideUnavailableChannels } returns true
    every { channelRepository.findListWithoutUnavailable() } returns Single.just(listOf(first, second, third, fourth))
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(emptyMap())

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
    assertThat((list[2] as ListItem.DefaultItem).remoteId).isEqualTo(21)
    assertThat((list[3] as ListItem.DefaultItem).remoteId).isEqualTo(31)
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
    every { channelRepository.findList() } returns Single.just(listOf(first, second, third))
    val childrenRelation = mockk<ChannelRelationEntity> {
      every { channelId } returns 21
      every { parentId } returns 11
      every { relationType } returns ChannelRelationType.DEFAULT
    }
    val relationMap = mapOf(11 to listOf(childrenRelation))
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(relationMap)
    val childEntity = ChannelChildEntity(childrenRelation, second)
    every {
      getChannelChildrenTreeUseCase.invoke(eq(11), eq(relationMap), any(), any())
    } returns listOf(childEntity)

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
  }

  private fun mockListEntity(
    channelRemoteId: Int,
    locationRemoteId: Int,
    locationName: String = "$locationRemoteId",
    locationCollapsed: Boolean = false,
  ): ChannelDataEntity = mockk {
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
      every { isCollapsed(CollapsedFlag.CHANNEL) } returns locationCollapsed
    }
    every { channelValueEntity } returns mockk {
      every { getValueAsByteArray() } returns byteArrayOf()
    }

    val listItem: ListItem.DefaultItem = mockk {
      every { remoteId } returns channelRemoteId
    }

    every { channelToListItemMapper.invoke(match { it.remoteId == channelRemoteId }) } returns
      listItem
    every { getCaptionUseCase.invoke(match { it.remoteId == channelRemoteId }) } returns
      LocalizedString.Constant("caption $channelRemoteId")
  }
}
