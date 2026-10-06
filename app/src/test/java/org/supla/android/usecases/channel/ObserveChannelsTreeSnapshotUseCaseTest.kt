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

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren

class ObserveChannelsTreeSnapshotUseCaseTest {

  @MockK
  private lateinit var channelRepository: ChannelRepository

  @MockK
  private lateinit var channelRelationRepository: ChannelRelationRepository

  @MockK
  private lateinit var channelsTreeSnapshotFactory: ChannelsTreeSnapshotFactory

  private lateinit var channelsSubject: PublishSubject<List<ChannelDataEntity>>
  private lateinit var relationsSubject: PublishSubject<Map<Int, List<ChannelRelationEntity>>>
  private lateinit var useCase: ObserveChannelsTreeSnapshotUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    channelsSubject = PublishSubject.create()
    relationsSubject = PublishSubject.create()
    every { channelRepository.findObservableList() } returns channelsSubject
    every { channelRelationRepository.findChildrenToParentsRelations() } returns relationsSubject
    useCase = ObserveChannelsTreeSnapshotUseCase(
      channelRepository,
      channelRelationRepository,
      channelsTreeSnapshotFactory
    )
  }

  @Test
  fun `should share source and replay latest snapshot`() {
    // given
    val channel = mockk<ChannelDataEntity>()
    val channels = listOf(channel)
    val relations = emptyMap<Int, List<ChannelRelationEntity>>()
    val snapshot = ChannelsTreeSnapshot(
      channels = channels,
      channelsWithChildren = mapOf(1 to ChannelWithChildren(channel)),
      childChannelIds = emptySet()
    )
    every { channelsTreeSnapshotFactory.create(channels, relations) } returns snapshot

    // when
    val firstObserver = useCase().test()
    relationsSubject.onNext(relations)
    channelsSubject.onNext(channels)
    val secondObserver = useCase().test()

    // then
    firstObserver.assertValue(snapshot)
    secondObserver.assertValue(snapshot)
    verify(exactly = 1) { channelRepository.findObservableList() }
    verify(exactly = 1) { channelRelationRepository.findChildrenToParentsRelations() }
    verify(exactly = 1) { channelsTreeSnapshotFactory.create(channels, relations) }
  }
}
