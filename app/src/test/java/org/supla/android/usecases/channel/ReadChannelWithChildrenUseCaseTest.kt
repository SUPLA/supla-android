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

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.core.shared.data.model.channel.ChannelRelationType

class ReadChannelWithChildrenUseCaseTest {

  @MockK
  lateinit var channelRepository: ChannelRepository

  @MockK
  lateinit var channelRelationRepository: ChannelRelationRepository

  private lateinit var useCase: ReadChannelWithChildrenUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    useCase = ReadChannelWithChildrenUseCase(
      ObserveChannelsTreeSnapshotUseCase(
        channelRepository,
        channelRelationRepository,
        ChannelsTreeSnapshotFactory(GetChannelChildrenTreeUseCase())
      )
    )
  }

  @Test
  fun `should load channel with children`() {
    // given
    val remoteId = 1
    val entity = mockChannelDataEntity(1)
    val childEntity = mockChannelDataEntity(2)
    val grandchildEntity = mockChannelDataEntity(3)
    val firstRelation = ChannelRelationEntity(2, 1, ChannelRelationType.MASTER_THERMOSTAT, 1L, false)
    val secondRelation = ChannelRelationEntity(3, 2, ChannelRelationType.MAIN_THERMOMETER, 1L, false)
    val relationMap = mapOf(1 to listOf(firstRelation), 2 to listOf(secondRelation))

    every { channelRepository.findObservableList() } returns Observable.just(listOf(entity, childEntity, grandchildEntity))
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(relationMap)

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()
    val result = observer.values()[0]

    assertThat(result.channel).isSameAs(entity)
    assertThat(result.children).hasSize(1)
    assertThat(result.children[0].channelDataEntity).isSameAs(childEntity)
    assertThat(result.children[0].children).hasSize(1)
    assertThat(result.children[0].children[0].channelDataEntity).isSameAs(grandchildEntity)

    verify { channelRepository.findObservableList() }
    verify { channelRelationRepository.findChildrenToParentsRelations() }
    confirmVerified(channelRelationRepository, channelRepository)
  }

  @Test
  fun `should load complete channel tree`() {
    // given
    val remoteId = 1
    val relations = mapOf(
      1 to listOf(channelRelationEntity(2, 1), channelRelationEntity(3, 1)),
      2 to listOf(channelRelationEntity(4, 2, ChannelRelationType.MAIN_THERMOMETER))
    )
    val channels = listOf(
      mockChannelDataEntity(1),
      mockChannelDataEntity(2),
      mockChannelDataEntity(3),
      mockChannelDataEntity(4)
    )
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(relations)
    every { channelRepository.findObservableList() } returns Observable.just(channels)

    // when
    val result = useCase.invoke(remoteId).test()

    // then
    result.assertComplete()
    assertThat(result.values().first()).isEqualTo(
      ChannelWithChildren(
        channel = channels[0],
        children = listOf(
          ChannelChildEntity(
            channelDataEntity = channels[1],
            channelRelationEntity = relations[1]!![0],
            children = listOf(ChannelChildEntity(relations[2]!![0], channels[3], emptyList()))
          ),
          ChannelChildEntity(
            channelDataEntity = channels[2],
            channelRelationEntity = relations[1]!![1],
            children = emptyList()
          )
        )
      )
    )
  }

  @Test
  fun `should skip circular dependencies while loading channel tree`() {
    // given
    val remoteId = 1
    val relations = mapOf(
      1 to listOf(channelRelationEntity(2, 1)),
      2 to listOf(channelRelationEntity(3, 2)),
      3 to listOf(channelRelationEntity(1, 3))
    )
    val channels = listOf(
      mockChannelDataEntity(1),
      mockChannelDataEntity(2),
      mockChannelDataEntity(3),
      mockChannelDataEntity(4)
    )
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(relations)
    every { channelRepository.findObservableList() } returns Observable.just(channels)

    // when
    val result = useCase.invoke(remoteId).test()

    // then
    result.assertComplete()
    assertThat(result.values().first()).isEqualTo(
      ChannelWithChildren(
        channel = channels[0],
        children = listOf(
          ChannelChildEntity(
            channelDataEntity = channels[1],
            channelRelationEntity = relations[1]!![0],
            children = listOf(ChannelChildEntity(relations[2]!![0], channels[2], emptyList()))
          )
        )
      )
    )
  }

  @Test
  fun `should report missing channel when loading channel tree`() {
    // given
    val remoteId = 4
    val relations = mapOf(
      1 to listOf(channelRelationEntity(2, 1)),
      2 to listOf(channelRelationEntity(3, 2))
    )
    val channels = listOf(
      mockChannelDataEntity(1),
      mockChannelDataEntity(2),
      mockChannelDataEntity(3)
    )
    every { channelRelationRepository.findChildrenToParentsRelations() } returns Observable.just(relations)
    every { channelRepository.findObservableList() } returns Observable.just(channels)

    // when
    val result = useCase.invoke(remoteId).test()

    // then
    result.assertNoValues()
    result.assertError(NoSuchElementException::class.java)
  }

  private fun channelRelationEntity(
    channelId: Int,
    parentId: Int,
    relationType: ChannelRelationType = ChannelRelationType.MASTER_THERMOSTAT
  ) = ChannelRelationEntity(channelId, parentId, relationType, 1L, false)

  private fun mockChannelDataEntity(remoteId: Int): ChannelDataEntity = mockk {
    every { this@mockk.remoteId } returns remoteId
  }
}
