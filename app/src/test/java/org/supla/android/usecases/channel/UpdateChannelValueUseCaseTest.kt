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

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.model.general.EntityUpdateResult
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.testhelpers.suplaChannel
import org.supla.android.testhelpers.suplaChannelValue

@RunWith(MockitoJUnitRunner::class)
class UpdateChannelValueUseCaseTest {

  @Mock
  private lateinit var profileRepository: RoomProfileRepository

  @Mock
  private lateinit var channelValueRepository: ChannelValueRepository

  @InjectMocks
  private lateinit var useCase: UpdateChannelValueUseCase

  @Test
  fun `should insert value`() {
    // given
    val channelRemoteId = 123
    val profileId = 321L
    val suplaChannelValue = suplaChannelValue()
    val suplaChannel = suplaChannel(channelId = channelRemoteId, value = suplaChannelValue, online = true)
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    whenever(channelValueRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.empty())
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profileEntity))
    whenever(channelValueRepository.insert(any())).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(channelValueRepository).findByRemoteId(channelRemoteId)
    verify(profileRepository).findActiveProfile()

    val captor = argumentCaptor<ChannelValueEntity>()
    verify(channelValueRepository).insert(captor.capture())
    with(captor.firstValue) {
      assertThat(this.channelRemoteId).isEqualTo(channelRemoteId)
      assertThat(online).isTrue()
      assertThat(this.profileId).isEqualTo(profileId)
    }

    verifyNoMoreInteractions(channelValueRepository, profileRepository)
  }

  @Test
  fun `should update value`() {
    // given
    val channelRemoteId = 123
    val suplaChannelValue = suplaChannelValue()
    val online = false
    val suplaChannel = suplaChannel(channelId = channelRemoteId, value = suplaChannelValue, online = online)
    val channelValueEntity: ChannelValueEntity = mockk {
      every { differsFrom(suplaChannelValue, online) } returns true
      every { updatedBy(suplaChannelValue, online) } returns this
    }

    whenever(channelValueRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelValueEntity))
    whenever(channelValueRepository.update(channelValueEntity)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(channelValueRepository).findByRemoteId(channelRemoteId)
    verify(channelValueRepository).update(channelValueEntity)

    io.mockk.verify {
      channelValueEntity.differsFrom(suplaChannelValue, online)
      channelValueEntity.updatedBy(suplaChannelValue, online)
    }

    verifyNoMoreInteractions(channelValueRepository)
    verifyNoInteractions(profileRepository)
  }

  @Test
  fun `should not update value when equals`() {
    // given
    val channelRemoteId = 123
    val suplaChannelValue = suplaChannelValue()
    val online = false
    val suplaChannel = suplaChannel(channelId = channelRemoteId, value = suplaChannelValue, online = online)
    val channelValueEntity: ChannelValueEntity = mockk {
      every { differsFrom(suplaChannelValue, online) } returns false
    }

    whenever(channelValueRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelValueEntity))

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.NOP)

    verify(channelValueRepository).findByRemoteId(channelRemoteId)

    io.mockk.verify { channelValueEntity.differsFrom(suplaChannelValue, online) }
    confirmVerified(channelValueEntity)

    verifyNoMoreInteractions(channelValueRepository)
    verifyNoInteractions(profileRepository)
  }
}
