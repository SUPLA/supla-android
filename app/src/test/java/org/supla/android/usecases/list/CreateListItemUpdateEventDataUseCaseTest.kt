package org.supla.android.usecases.list
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
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Test
import org.supla.android.core.shared.shareable
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.images.ImageId
import org.supla.android.testhelpers.extensions.mockShareable
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenTreeUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToGarageDoorUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToGpmUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToIconValueItemUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToProjectScreenUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToShadingSystemUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToSwitchUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToTemperatureHumidityUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToThermostatUpdateEventMapper
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase

class CreateListItemUpdateEventDataUseCaseTest {

  @MockK
  private lateinit var eventsManager: UpdateEventsManager

  @MockK
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @MockK
  private lateinit var readChannelWithChildrenTreeUseCase: ReadChannelWithChildrenTreeUseCase

  @MockK
  private lateinit var channelWithChildrenToThermostatUpdateEventMapper: ChannelWithChildrenToThermostatUpdateEventMapper

  @RelaxedMockK
  private lateinit var channelWithChildrenToIconValueItemUpdateEventMapper: ChannelWithChildrenToIconValueItemUpdateEventMapper

  @RelaxedMockK
  private lateinit var channelWithChildrenToGpmUpdateEventMapper: ChannelWithChildrenToGpmUpdateEventMapper

  @RelaxedMockK
  private lateinit var channelWithChildrenToShadingSystemUpdateEventMapper: ChannelWithChildrenToShadingSystemUpdateEventMapper

  @RelaxedMockK
  private lateinit var channelWithChildrenToProjectScreenUpdateEventMapper: ChannelWithChildrenToProjectScreenUpdateEventMapper

  @RelaxedMockK
  private lateinit var channelWithChildrenToGarageDoorUpdateEventMapper: ChannelWithChildrenToGarageDoorUpdateEventMapper

  @RelaxedMockK
  private lateinit var channelWithChildrenToSwitchUpdateEventMapper: ChannelWithChildrenToSwitchUpdateEventMapper

  @RelaxedMockK
  private lateinit var channelWithChildrenToTemperatureHumidityUpdateEventMapper: ChannelWithChildrenToTemperatureHumidityUpdateEventMapper

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @MockK
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @MockK
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @MockK
  private lateinit var getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase

  @InjectMockKs
  private lateinit var useCase: CreateListItemUpdateEventDataUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should map channel`() {
    // given
    val remoteId = 123
    val itemType = ItemType.CHANNEL

    val channel: ChannelDataEntity = mockk()
    every { channel.remoteId } returns remoteId
    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.channel } returns channel
    every { channelWithChildren.children } returns emptyList()

    val data: SlideableListItemData = mockk()

    every { eventsManager.observeChannelEvents(remoteId) } returns Observable.just(mockk())
    every { readChannelWithChildrenTreeUseCase(remoteId) } returns Observable.just(channelWithChildren)
    every { channelWithChildrenToThermostatUpdateEventMapper.handle(channelWithChildren) } returns true
    every { channelWithChildrenToThermostatUpdateEventMapper.map(channelWithChildren) } returns data

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(data)

    verify {
      eventsManager.observeChannelEvents(remoteId)
      channelWithChildrenToThermostatUpdateEventMapper.handle(channelWithChildren)
      channelWithChildrenToThermostatUpdateEventMapper.map(channelWithChildren)
    }
    verify(exactly = 2) { readChannelWithChildrenTreeUseCase.invoke(remoteId) }
    confirmVerified(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenTreeUseCase,
      channelWithChildrenToThermostatUpdateEventMapper
    )
  }

  @Test
  fun `should map channel with main temperature children`() {
    // given
    val remoteId = 123
    val childId = 234
    val itemType = ItemType.CHANNEL

    val child: ChannelDataEntity = mockk()
    every { child.remoteId } returns childId
    val channel: ChannelDataEntity = mockk()
    every { channel.remoteId } returns remoteId
    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.channel } returns channel
    val relation: ChannelRelationEntity = mockk { every { relationType } returns ChannelRelationType.MAIN_THERMOMETER }
    every { channelWithChildren.children } returns listOf(ChannelChildEntity(relation, child))

    val data: SlideableListItemData = mockk()

    every { eventsManager.observeChannelEvents(remoteId) } returns Observable.empty()
    every { eventsManager.observeChannelEvents(childId) } returns Observable.just(mockk())
    every { readChannelWithChildrenTreeUseCase(remoteId) } returns Observable.just(channelWithChildren)
    every { channelWithChildrenToThermostatUpdateEventMapper.handle(channelWithChildren) } returns true
    every { channelWithChildrenToThermostatUpdateEventMapper.map(channelWithChildren) } returns data

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(data)

    verify {
      eventsManager.observeChannelEvents(remoteId)
      eventsManager.observeChannelEvents(childId)
      channelWithChildrenToThermostatUpdateEventMapper.handle(channelWithChildren)
      channelWithChildrenToThermostatUpdateEventMapper.map(channelWithChildren)
    }
    verify(exactly = 2) { readChannelWithChildrenTreeUseCase.invoke(remoteId) }
    confirmVerified(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenTreeUseCase,
      channelWithChildrenToThermostatUpdateEventMapper
    )
  }

  @Test
  fun `should map group`() {
    // given
    val remoteId = 123
    val itemType = ItemType.GROUP

    val channelGroup: ChannelGroupDataEntity = mockk()
    val data: SlideableListItemData = mockk()

    every { eventsManager.observeGroupEvents(remoteId) } returns Observable.just(mockk())
    every { readChannelGroupByRemoteIdUseCase(remoteId) } returns Maybe.just(channelGroup)
    every { channelWithChildrenToThermostatUpdateEventMapper.handle(channelGroup) } returns true
    every { channelWithChildrenToThermostatUpdateEventMapper.map(channelGroup) } returns data

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(data)

    verify {
      eventsManager.observeGroupEvents(remoteId)
      readChannelGroupByRemoteIdUseCase.invoke(remoteId)
      channelWithChildrenToThermostatUpdateEventMapper.handle(channelGroup)
      channelWithChildrenToThermostatUpdateEventMapper.map(channelGroup)
    }
    confirmVerified(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenTreeUseCase,
      channelWithChildrenToThermostatUpdateEventMapper
    )
  }

  @Test
  fun `should handle channel even if there is no mapper`() {
    // given
    val remoteId = 123
    val itemType = ItemType.CHANNEL
    val caption: LocalizedString = LocalizedString.Constant("caption")
    val imageId: ImageId = mockk()
    val value = "---"

    val channel: ChannelDataEntity = mockk {
      every { function } returns SuplaFunction.POWER_SWITCH
    }
    channel.mockShareable(remoteId)
    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.channel } returns channel
    every { channelWithChildren.children } returns emptyList()
    val channelShareable = channel.shareable
    val channelWithChildrenShareable = channelWithChildren.shareable

    every { channelWithChildrenToThermostatUpdateEventMapper.handle(channelWithChildren) } returns false
    every { eventsManager.observeChannelEvents(remoteId) } returns Observable.just(mockk())
    every { readChannelWithChildrenTreeUseCase(remoteId) } returns Observable.just(channelWithChildren)
    every { getCaptionUseCase(channelShareable) } returns caption
    every { getChannelIconUseCase(channel) } returns imageId
    every { getChannelValueStringUseCase(channelWithChildren) } returns value
    every { getChannelIssuesForListUseCase.invoke(channelWithChildrenShareable) } returns ListItemIssues.empty

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(
      SlideableListItemData.Default(
        onlineState = ListOnlineState.ONLINE,
        title = caption,
        icon = imageId,
        issues = ListItemIssues.empty,
        infoSupported = false,
        value = value
      )
    )

    verify {
      eventsManager.observeChannelEvents(remoteId)
      channelWithChildrenToThermostatUpdateEventMapper.handle(channelWithChildren)
      getCaptionUseCase.invoke(channelShareable)
      getChannelIconUseCase.invoke(channel)
      getChannelValueStringUseCase.invoke(channelWithChildren)
    }
    verify(exactly = 2) { readChannelWithChildrenTreeUseCase.invoke(remoteId) }
    confirmVerified(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenTreeUseCase,
      channelWithChildrenToThermostatUpdateEventMapper,
      getCaptionUseCase,
      getChannelIconUseCase,
      getChannelValueStringUseCase
    )
  }

  @Test
  fun `should handle group even if there is no mapper`() {
    // given
    val remoteId = 123
    val itemType = ItemType.GROUP
    val caption: LocalizedString = LocalizedString.Constant("caption")
    val imageId: ImageId = mockk()

    val channelGroup: ChannelGroupDataEntity = mockk {
      every { isOnline() } returns true
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.function } returns SuplaFunction.POWER_SWITCH
      every { this@mockk.caption } returns ""
    }
    val shareable = channelGroup.shareable

    every { eventsManager.observeGroupEvents(remoteId) } returns Observable.just(mockk())
    every { readChannelGroupByRemoteIdUseCase(remoteId) } returns Maybe.just(channelGroup)
    every { channelWithChildrenToThermostatUpdateEventMapper.handle(channelGroup) } returns false
    every { getCaptionUseCase.invoke(shareable) } returns caption
    every { getChannelIconUseCase(channelGroup) } returns imageId

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(
      SlideableListItemData.Default(
        onlineState = ListOnlineState.ONLINE,
        title = caption,
        icon = imageId,
        issues = ListItemIssues.empty,
        infoSupported = false,
        value = null
      )
    )

    verify {
      eventsManager.observeGroupEvents(remoteId)
      readChannelGroupByRemoteIdUseCase.invoke(remoteId)
      channelWithChildrenToThermostatUpdateEventMapper.handle(channelGroup)
      getCaptionUseCase.invoke(shareable)
      getChannelIconUseCase.invoke(channelGroup)
    }
    confirmVerified(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenTreeUseCase,
      channelWithChildrenToThermostatUpdateEventMapper,
      getCaptionUseCase,
      getChannelIconUseCase
    )
  }
}
