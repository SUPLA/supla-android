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
import io.mockk.Called
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.model.general.EntityUpdateResult
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.testhelpers.suplaChannel
import org.supla.android.testhelpers.suplaChannelValue

class UpdateChannelValueUseCaseTest {

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var channelValueRepository: ChannelValueRepository

  @InjectMockKs
  private lateinit var useCase: UpdateChannelValueUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should insert value`() {
    // given
    val channelRemoteId = 123
    val profileId = 321L
    val suplaChannelValue = suplaChannelValue()
    val suplaChannel = suplaChannel(channelId = channelRemoteId, value = suplaChannelValue, status = SuplaChannelAvailabilityStatus.ONLINE)
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    every { channelValueRepository.findByRemoteId(channelRemoteId) } returns Maybe.empty()
    every { profileRepository.findActiveProfile() } returns Single.just(profileEntity)
    every { channelValueRepository.insert(any()) } returns Completable.complete()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify { channelValueRepository.findByRemoteId(channelRemoteId) }
    verify { profileRepository.findActiveProfile() }

    val captor = slot<ChannelValueEntity>()
    verify { channelValueRepository.insert(capture(captor)) }
    with(captor.captured) {
      assertThat(this.channelRemoteId).isEqualTo(channelRemoteId)
      assertThat(status).isEqualTo(SuplaChannelAvailabilityStatus.ONLINE)
      assertThat(this.profileId).isEqualTo(profileId)
    }

    confirmVerified(channelValueRepository, profileRepository)
  }

  @Test
  fun `should update value`() {
    // given
    val channelRemoteId = 123
    val suplaChannelValue = suplaChannelValue()
    val status = SuplaChannelAvailabilityStatus.OFFLINE
    val suplaChannel = suplaChannel(channelId = channelRemoteId, value = suplaChannelValue, status = status)
    val channelValueEntity: ChannelValueEntity = mockk {
      every { differsFrom(suplaChannelValue, status) } returns true
      every { updatedBy(suplaChannelValue, status) } returns this
    }

    every { channelValueRepository.findByRemoteId(channelRemoteId) } returns Maybe.just(channelValueEntity)
    every { channelValueRepository.update(channelValueEntity) } returns Completable.complete()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify {
      channelValueRepository.findByRemoteId(channelRemoteId)
      channelValueRepository.update(channelValueEntity)
      channelValueEntity.differsFrom(suplaChannelValue, status)
      channelValueEntity.updatedBy(suplaChannelValue, status)
    }

    confirmVerified(channelValueRepository, profileRepository)
  }

  @Test
  fun `should not update value when equals`() {
    // given
    val channelRemoteId = 123
    val suplaChannelValue = suplaChannelValue()
    val status = SuplaChannelAvailabilityStatus.OFFLINE
    val suplaChannel = suplaChannel(channelId = channelRemoteId, value = suplaChannelValue, status = status)
    val channelValueEntity: ChannelValueEntity = mockk {
      every { differsFrom(suplaChannelValue, status) } returns false
    }

    every { channelValueRepository.findByRemoteId(channelRemoteId) } returns Maybe.just(channelValueEntity)

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.NOP)

    verify { channelValueRepository.findByRemoteId(channelRemoteId) }
    verify { channelValueEntity.differsFrom(suplaChannelValue, status) }
    confirmVerified(channelValueEntity, channelValueRepository, profileRepository)
  }
}
