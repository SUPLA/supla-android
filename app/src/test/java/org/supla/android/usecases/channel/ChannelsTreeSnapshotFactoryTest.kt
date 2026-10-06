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

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.core.shared.data.model.channel.ChannelRelationType

class ChannelsTreeSnapshotFactoryTest {

  private val factory = ChannelsTreeSnapshotFactory(GetChannelChildrenTreeUseCase())

  @Test
  fun `should create snapshot with complete channels trees`() {
    // given
    val channels = listOf(channel(1), channel(2), channel(3), channel(4))
    val relations = mapOf(
      1 to listOf(relation(2, 1), relation(3, 1)),
      2 to listOf(relation(4, 2))
    )

    // when
    val snapshot = factory.create(channels, relations)

    // then
    assertThat(snapshot.channels).isSameAs(channels)
    assertThat(snapshot.channelsWithChildren.keys).containsExactly(1, 2, 3, 4)
    assertThat(snapshot.childChannelIds).containsExactlyInAnyOrder(2, 3, 4)

    val firstTree = snapshot.channelsWithChildren.getValue(1)
    assertThat(firstTree.children.map { it.channelDataEntity.remoteId }).containsExactly(2, 3)
    assertThat(firstTree.children.first().children.map { it.channelDataEntity.remoteId }).containsExactly(4)
    assertThat(snapshot.channelsWithChildren.getValue(2).children.map { it.channelDataEntity.remoteId }).containsExactly(4)
    assertThat(snapshot.channelsWithChildren.getValue(3).children).isEmpty()
    assertThat(snapshot.channelsWithChildren.getValue(4).children).isEmpty()
  }

  @Test
  fun `should skip circular dependency`() {
    // given
    val channels = listOf(channel(1), channel(2), channel(3))
    val relations = mapOf(
      1 to listOf(relation(2, 1)),
      2 to listOf(relation(3, 2)),
      3 to listOf(relation(1, 3))
    )

    // when
    val snapshot = factory.create(channels, relations)

    // then
    val firstTree = snapshot.channelsWithChildren.getValue(1)
    assertThat(firstTree.children).hasSize(1)
    assertThat(firstTree.children.first().channelDataEntity.remoteId).isEqualTo(2)
    assertThat(firstTree.children.first().children).hasSize(1)
    assertThat(firstTree.children.first().children.first().channelDataEntity.remoteId).isEqualTo(3)
    assertThat(firstTree.children.first().children.first().children).isEmpty()
  }

  private fun channel(remoteId: Int): ChannelDataEntity = mockk {
    every { this@mockk.remoteId } returns remoteId
  }

  private fun relation(channelId: Int, parentId: Int) =
    ChannelRelationEntity(channelId, parentId, ChannelRelationType.DEFAULT, 1L, false)
}
