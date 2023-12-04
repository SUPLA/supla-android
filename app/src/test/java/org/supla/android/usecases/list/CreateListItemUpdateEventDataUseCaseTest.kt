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
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.db.Channel
import org.supla.android.db.ChannelGroup
import org.supla.android.events.UpdateEventsManager
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.ChannelChild
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToThermostatUpdateEventMapper

@RunWith(MockitoJUnitRunner::class)
class CreateListItemUpdateEventDataUseCaseTest {

  @Mock
  lateinit var eventsManager: UpdateEventsManager

  @Mock
  lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @Mock
  lateinit var channelWithCHildrenToThermostatUpdateEventMapper: ChannelWithChildrenToThermostatUpdateEventMapper

  @InjectMocks
  lateinit var useCase: CreateListItemUpdateEventDataUseCase

  @Test
  fun `should map channel`() {
    // given
    val remoteId = 123
    val itemType = ItemType.CHANNEL

    val channel: Channel = mockk()
    every { channel.remoteId } returns remoteId
    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.channel } returns channel
    every { channelWithChildren.children } returns emptyList()

    val data: SlideableListItemData = mockk()

    whenever(eventsManager.observeChannel(remoteId)).thenReturn(Observable.just(mockk()))
    whenever(readChannelWithChildrenUseCase(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(channelWithCHildrenToThermostatUpdateEventMapper.handle(channelWithChildren)).thenReturn(true)
    whenever(channelWithCHildrenToThermostatUpdateEventMapper.map(channelWithChildren)).thenReturn(data)

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(data)

    verify(eventsManager).observeChannel(remoteId)
    verify(readChannelWithChildrenUseCase, times(2)).invoke(remoteId)
    verify(channelWithCHildrenToThermostatUpdateEventMapper).handle(channelWithChildren)
    verify(channelWithCHildrenToThermostatUpdateEventMapper).map(channelWithChildren)
    verifyNoMoreInteractions(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenUseCase,
      channelWithCHildrenToThermostatUpdateEventMapper
    )
  }

  @Test
  fun `should map channel with main temperature children`() {
    // given
    val remoteId = 123
    val childId = 234
    val itemType = ItemType.CHANNEL

    val child: Channel = mockk()
    every { child.remoteId } returns childId
    val channel: Channel = mockk()
    every { channel.remoteId } returns remoteId
    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.channel } returns channel
    every { channelWithChildren.children } returns listOf(ChannelChild(ChannelRelationType.MAIN_THERMOMETER, child))

    val data: SlideableListItemData = mockk()

    whenever(eventsManager.observeChannel(remoteId)).thenReturn(Observable.empty())
    whenever(eventsManager.observeChannel(childId)).thenReturn(Observable.just(mockk()))
    whenever(readChannelWithChildrenUseCase(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(channelWithCHildrenToThermostatUpdateEventMapper.handle(channelWithChildren)).thenReturn(true)
    whenever(channelWithCHildrenToThermostatUpdateEventMapper.map(channelWithChildren)).thenReturn(data)

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(data)

    verify(eventsManager).observeChannel(remoteId)
    verify(eventsManager).observeChannel(childId)
    verify(readChannelWithChildrenUseCase, times(2)).invoke(remoteId)
    verify(channelWithCHildrenToThermostatUpdateEventMapper).handle(channelWithChildren)
    verify(channelWithCHildrenToThermostatUpdateEventMapper).map(channelWithChildren)
    verifyNoMoreInteractions(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenUseCase,
      channelWithCHildrenToThermostatUpdateEventMapper
    )
  }

  @Test
  fun `should map group`() {
    // given
    val remoteId = 123
    val itemType = ItemType.GROUP

    val channelGroup: ChannelGroup = mockk()
    val data: SlideableListItemData = mockk()

    whenever(eventsManager.observeGroup(remoteId)).thenReturn(Observable.just(mockk()))
    whenever(readChannelGroupByRemoteIdUseCase(remoteId)).thenReturn(Maybe.just(channelGroup))
    whenever(channelWithCHildrenToThermostatUpdateEventMapper.handle(channelGroup)).thenReturn(true)
    whenever(channelWithCHildrenToThermostatUpdateEventMapper.map(channelGroup)).thenReturn(data)

    // when
    val observer = useCase(itemType, remoteId).test()

    // then
    observer.assertComplete()
    observer.assertResult(data)

    verify(eventsManager).observeGroup(remoteId)
    verify(readChannelGroupByRemoteIdUseCase).invoke(remoteId)
    verify(channelWithCHildrenToThermostatUpdateEventMapper).handle(channelGroup)
    verify(channelWithCHildrenToThermostatUpdateEventMapper).map(channelGroup)
    verifyNoMoreInteractions(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenUseCase,
      channelWithCHildrenToThermostatUpdateEventMapper
    )
  }

  @Test
  fun `should throw when no mapper able to handle data`() {
    // given
    val remoteId = 123
    val itemType = ItemType.CHANNEL

    val channel: Channel = mockk()
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
    verify(channelWithCHildrenToThermostatUpdateEventMapper).handle(channelWithChildren)
    verifyNoMoreInteractions(
      eventsManager,
      readChannelGroupByRemoteIdUseCase,
      readChannelWithChildrenUseCase,
      channelWithCHildrenToThermostatUpdateEventMapper
    )
  }
}
