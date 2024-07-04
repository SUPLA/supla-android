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

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToGarageDoorUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToGpmUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToMeasurementUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToProjectScreenUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToShadingSystemUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToThermostatUpdateEventMapper

@RunWith(MockitoJUnitRunner::class)
class CreateListItemUpdateEventDataUseCaseTest {

  @Mock
  private lateinit var eventsManager: UpdateEventsManager

  @Mock
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @Mock
  private lateinit var channelWithChildrenToThermostatUpdateEventMapper: ChannelWithChildrenToThermostatUpdateEventMapper

  @Mock
  private lateinit var channelWithChildrenToMeasurementUpdateEventMapper: ChannelWithChildrenToMeasurementUpdateEventMapper

  @Mock
  private lateinit var channelWithChildrenToGpmUpdateEventMapper: ChannelWithChildrenToGpmUpdateEventMapper

  @Mock
  private lateinit var channelWithChildrenToShadingSystemUpdateEventMapper: ChannelWithChildrenToShadingSystemUpdateEventMapper

  @Mock
  private lateinit var channelWithChildrenToProjectScreenUpdateEventMapper: ChannelWithChildrenToProjectScreenUpdateEventMapper

  @Mock
  private lateinit var channelWithChildrenToGarageDoorUpdateEventMapper: ChannelWithChildrenToGarageDoorUpdateEventMapper

  @InjectMocks
  private lateinit var useCase: CreateListItemUpdateEventDataUseCase

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

    whenever(eventsManager.observeChannel(remoteId)).thenReturn(Observable.just(mockk()))
    whenever(readChannelWithChildrenUseCase(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(channelWithChildrenToThermostatUpdateEventMapper.handle(channelWithChildren)).thenReturn(true)
    whenever(channelWithChildrenToThermostatUpdateEventMapper.map(channelWithChildren)).thenReturn(data)

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(data)

    verify(eventsManager).observeChannel(remoteId)
    verify(readChannelWithChildrenUseCase, times(2)).invoke(remoteId)
    verify(channelWithChildrenToThermostatUpdateEventMapper).handle(channelWithChildren)
    verify(channelWithChildrenToThermostatUpdateEventMapper).map(channelWithChildren)
    verifyNoMoreInteractions(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenUseCase,
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

    whenever(eventsManager.observeChannel(remoteId)).thenReturn(Observable.empty())
    whenever(eventsManager.observeChannel(childId)).thenReturn(Observable.just(mockk()))
    whenever(readChannelWithChildrenUseCase(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(channelWithChildrenToThermostatUpdateEventMapper.handle(channelWithChildren)).thenReturn(true)
    whenever(channelWithChildrenToThermostatUpdateEventMapper.map(channelWithChildren)).thenReturn(data)

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(data)

    verify(eventsManager).observeChannel(remoteId)
    verify(eventsManager).observeChannel(childId)
    verify(readChannelWithChildrenUseCase, times(2)).invoke(remoteId)
    verify(channelWithChildrenToThermostatUpdateEventMapper).handle(channelWithChildren)
    verify(channelWithChildrenToThermostatUpdateEventMapper).map(channelWithChildren)
    verifyNoMoreInteractions(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenUseCase,
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

    whenever(eventsManager.observeGroup(remoteId)).thenReturn(Observable.just(mockk()))
    whenever(readChannelGroupByRemoteIdUseCase(remoteId)).thenReturn(Maybe.just(channelGroup))
    whenever(channelWithChildrenToThermostatUpdateEventMapper.handle(channelGroup)).thenReturn(true)
    whenever(channelWithChildrenToThermostatUpdateEventMapper.map(channelGroup)).thenReturn(data)

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(data)

    verify(eventsManager).observeGroup(remoteId)
    verify(readChannelGroupByRemoteIdUseCase).invoke(remoteId)
    verify(channelWithChildrenToThermostatUpdateEventMapper).handle(channelGroup)
    verify(channelWithChildrenToThermostatUpdateEventMapper).map(channelGroup)
    verifyNoMoreInteractions(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenUseCase,
      channelWithChildrenToThermostatUpdateEventMapper
    )
  }

  @Test
  fun `should throw when no mapper able to handle data`() {
    // given
    val remoteId = 123
    val itemType = ItemType.CHANNEL

    val channel: ChannelDataEntity = mockk()
    every { channel.remoteId } returns remoteId
    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.channel } returns channel
    every { channelWithChildren.children } returns emptyList()

    whenever(eventsManager.observeChannel(remoteId)).thenReturn(Observable.just(mockk()))
    whenever(readChannelWithChildrenUseCase(remoteId)).thenReturn(Maybe.just(channelWithChildren))

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertFailure(IllegalStateException::class.java)

    verify(eventsManager).observeChannel(remoteId)
    verify(readChannelWithChildrenUseCase, times(2)).invoke(remoteId)
    verify(channelWithChildrenToThermostatUpdateEventMapper).handle(channelWithChildren)
    verifyNoMoreInteractions(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenUseCase,
      channelWithChildrenToThermostatUpdateEventMapper
    )
  }
}
