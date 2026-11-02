package org.supla.android.usecases.channelrelation
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.lib.SuplaChannelGroupRelation

class UpdateChannelGroupRelationUseCaseTest {

  @MockK
  private lateinit var channelGroupRelationRepository: ChannelGroupRelationRepository

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @InjectMockKs
  private lateinit var useCase: UpdateChannelGroupRelationUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should insert relation when not found`() {
    val relation = relation(234, 123)
    val profile = profileEntity(456)

    coEvery { channelGroupRelationRepository.findByGroupAndChannel(123, 234) } returns null
    coEvery { profileRepository.findActiveProfileKtx() } returns profile
    coEvery { channelGroupRelationRepository.insert(any()) } returns Unit

    val result = useCase.invoke(relation)

    assertThat(result).isTrue()

    val relationSlot = slot<ChannelGroupRelationEntity>()
    coVerify {
      channelGroupRelationRepository.findByGroupAndChannel(123, 234)
      profileRepository.findActiveProfileKtx()
      channelGroupRelationRepository.insert(capture(relationSlot))
    }
    confirmVerified(channelGroupRelationRepository, profileRepository)

    with(relationSlot.captured) {
      assertThat(id).isNull()
      assertThat(groupId).isEqualTo(123)
      assertThat(channelId).isEqualTo(234)
      assertThat(visible).isEqualTo(1)
      assertThat(profileId).isEqualTo(profile.id)
    }
  }

  @Test
  fun `should update relation when found and hidden`() {
    val relation = relation(234, 123)
    val existing = relationEntity(123, 234, visible = 0)

    coEvery { channelGroupRelationRepository.findByGroupAndChannel(123, 234) } returns existing
    coEvery { channelGroupRelationRepository.updateEntity(any()) } returns Unit

    val result = useCase.invoke(relation)

    assertThat(result).isTrue()

    val relationSlot = slot<ChannelGroupRelationEntity>()
    coVerify {
      channelGroupRelationRepository.findByGroupAndChannel(123, 234)
      channelGroupRelationRepository.updateEntity(capture(relationSlot))
    }
    confirmVerified(channelGroupRelationRepository, profileRepository)

    with(relationSlot.captured) {
      assertThat(id).isEqualTo(existing.id)
      assertThat(groupId).isEqualTo(existing.groupId)
      assertThat(channelId).isEqualTo(existing.channelId)
      assertThat(visible).isEqualTo(1)
      assertThat(profileId).isEqualTo(existing.profileId)
    }
  }

  @Test
  fun `should not update relation when found and visible`() {
    val relation = relation(234, 123)
    val existing = relationEntity(123, 234)

    coEvery { channelGroupRelationRepository.findByGroupAndChannel(123, 234) } returns existing

    val result = useCase.invoke(relation)

    assertThat(result).isFalse()

    coVerify {
      channelGroupRelationRepository.findByGroupAndChannel(123, 234)
    }
    confirmVerified(channelGroupRelationRepository, profileRepository)
  }

  @Test
  fun `should not insert relation when active profile not found`() {
    val relation = relation(234, 123)

    coEvery { channelGroupRelationRepository.findByGroupAndChannel(123, 234) } returns null
    coEvery { profileRepository.findActiveProfileKtx() } returns null

    val result = useCase.invoke(relation)

    assertThat(result).isFalse()

    coVerify {
      channelGroupRelationRepository.findByGroupAndChannel(123, 234)
      profileRepository.findActiveProfileKtx()
    }
    confirmVerified(channelGroupRelationRepository, profileRepository)
  }

  private fun relation(channelId: Int, groupId: Int) = SuplaChannelGroupRelation().apply {
    ChannelID = channelId
    ChannelGroupID = groupId
  }

  private fun relationEntity(
    groupId: Int,
    channelId: Int,
    visible: Int = 1
  ) = ChannelGroupRelationEntity(
    id = 321,
    groupId = groupId,
    channelId = channelId,
    visible = visible,
    profileId = 456
  )

  private fun profileEntity(profileId: Long) = mockk<ProfileEntity> {
    every { id } returns profileId
  }
}
