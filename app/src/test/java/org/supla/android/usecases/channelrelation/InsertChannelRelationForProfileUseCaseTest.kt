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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.lib.SuplaChannelRelation
import org.supla.android.testhelpers.relationMock
import org.supla.core.shared.data.model.channel.ChannelRelationType

@Suppress("SameParameterValue")
class InsertChannelRelationForProfileUseCaseTest {

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  lateinit var channelRelationRepository: ChannelRelationRepository

  @InjectMockKs
  lateinit var useCase: InsertChannelRelationForProfileUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should insert relation`() {
    // given
    val profileId = 321L
    val channelId = 123
    val parentId = 234
    val relationType = ChannelRelationType.MAIN_THERMOMETER

    val relation: SuplaChannelRelation = relationMock(channelId, parentId, relationType)
    val profile: ProfileEntity = mockk { every { id } returns profileId }

    val relationSlot = slot<ChannelRelationEntity>()
    every { channelRelationRepository.insertOrUpdate(capture(relationSlot)) } returns Completable.complete()
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    // when
    val observer = useCase(relation).test()

    // then
    observer.assertComplete()

    val entity = relationSlot.captured
    assertThat(entity.channelId).isEqualTo(channelId)
    assertThat(entity.parentId).isEqualTo(parentId)
    assertThat(entity.relationType).isEqualTo(relationType)
    assertThat(entity.profileId).isEqualTo(profileId)
    assertThat(entity.deleteFlag).isFalse

    verify {
      profileRepository.findActiveProfile()
      channelRelationRepository.insertOrUpdate(any())
    }
    confirmVerified(profileRepository, channelRelationRepository)
  }
}
