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

import io.mockk.called
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
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.model.general.EntityUpdateResult
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.db.Location
import org.supla.android.lib.SuplaConst
import org.supla.android.testhelpers.suplaChannel

@Suppress("UnusedDataClassCopyResult")
@RunWith(MockitoJUnitRunner::class)
class UpdateChannelUseCaseTest {
  @Mock
  private lateinit var profileRepository: RoomProfileRepository

  @Mock
  private lateinit var channelRepository: RoomChannelRepository

  @Mock
  private lateinit var locationRepository: LocationRepository

  @Mock
  private lateinit var channelConfigRepository: ChannelConfigRepository

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @InjectMocks
  private lateinit var useCase: UpdateChannelUseCase

  @Test
  fun `should insert channel when not exist`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234
    val profileId = 111L

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.DEFAULT
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.empty())
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profileEntity))
    whenever(channelRepository.insert(any())).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(profileRepository).findActiveProfile()

    val captor = argumentCaptor<ChannelEntity>()
    verify(channelRepository).insert(captor.capture())
    with(captor.firstValue) {
      assertThat(remoteId).isEqualTo(channelRemoteId)
      assertThat(locationId).isEqualTo(locationRemoteId.toLong())
      assertThat(this.profileId).isEqualTo(profileId)
      assertThat(position).isEqualTo(0)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository)
    verifyZeroInteractions(channelConfigRepository, suplaClientProvider)
  }

  @Test
  fun `should not insert channel when location not exist`() {
    // given
    val locationRemoteId = 123

    val suplaChannel = suplaChannel(locationRemoteId)

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.empty())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.ERROR)

    verify(locationRepository).findByRemoteId(locationRemoteId)

    verifyNoMoreInteractions(locationRepository)
    verifyZeroInteractions(channelConfigRepository, suplaClientProvider, channelRepository, profileRepository)
  }

  @Test
  fun `should insert channel when not exist and set position to last`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234
    val profileId = 111L

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.USER_DEFINED
      every { remoteId } returns locationRemoteId
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.empty())
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profileEntity))
    whenever(channelRepository.insert(any())).thenReturn(Completable.complete())
    whenever(channelRepository.findChannelCountInLocation(locationRemoteId)).thenReturn(Single.just(5))

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(profileRepository).findActiveProfile()
    verify(channelRepository).findChannelCountInLocation(locationRemoteId)

    val captor = argumentCaptor<ChannelEntity>()
    verify(channelRepository).insert(captor.capture())
    with(captor.firstValue) {
      assertThat(remoteId).isEqualTo(channelRemoteId)
      assertThat(locationId).isEqualTo(locationRemoteId.toLong())
      assertThat(this.profileId).isEqualTo(profileId)
      assertThat(position).isEqualTo(6)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository)
    verifyZeroInteractions(channelConfigRepository, suplaClientProvider)
  }

  @Test
  fun `should update channel when exist`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.DEFAULT
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns locationRemoteId.toLong()
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(position = 0) wasNot called
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository)
    verifyZeroInteractions(channelConfigRepository, suplaClientProvider)
  }

  @Test
  fun `should update channel when exist and set position to last`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.USER_DEFINED
      every { remoteId } returns locationRemoteId
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns 333
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
      every { copy(id = 444, locationId = 333, position = 6) } returns this
      every { id } returns 444
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())
    whenever(channelRepository.findChannelCountInLocation(locationRemoteId)).thenReturn(Single.just(5))

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    verify(channelRepository).findChannelCountInLocation(locationRemoteId)

    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(position = 6)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository)
    verifyZeroInteractions(channelConfigRepository, suplaClientProvider)
  }

  @Test
  fun `should update channel when exist and set position to 0 when location sorting is default`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.DEFAULT
      every { remoteId } returns locationRemoteId
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns 333
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 5
      every { copy(id = 444, locationId = 333, position = 0) } returns this
      every { id } returns 444
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)

    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(position = 0)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository)
    verifyZeroInteractions(channelConfigRepository, suplaClientProvider)
  }

  @Test
  fun `should update channel when exist is same but not visible`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.DEFAULT
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns false
      every { visible } returns 0
      every { locationId } returns locationRemoteId.toLong()
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(position = 0) wasNot called
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository)
    verifyZeroInteractions(channelConfigRepository, suplaClientProvider)
  }

  @Test
  fun `should not update channel when exist is same but and visible`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.DEFAULT
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns false
      every { visible } returns 1
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.NOP)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)

    verifyNoMoreInteractions(locationRepository, channelRepository)
    verifyZeroInteractions(channelConfigRepository, suplaClientProvider, profileRepository)
  }

  @Test
  fun `should update channel and ask for channel config`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234
    val function = SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
    val crc32 = 999L

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId, function = function, crc32 = 888L)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.DEFAULT
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns locationRemoteId.toLong()
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
    }
    val channelConfigEntity: ChannelConfigEntity = mockk {
      every { configCrc32 } returns crc32
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())
    whenever(channelConfigRepository.findForRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelConfigEntity))

    val suplaClient: SuplaClientApi = mockk {
      every { getChannelConfig(channelRemoteId, ChannelConfigType.DEFAULT) } returns true
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    verify(channelConfigRepository).findForRemoteId(channelRemoteId)
    verify(suplaClientProvider).provide()
    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(position = 0) wasNot called
      suplaClient.getChannelConfig(channelRemoteId, ChannelConfigType.DEFAULT)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, channelConfigRepository, suplaClientProvider)
  }

  @Test
  fun `should update channel and not ask for channel config where crc32 equals`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234
    val function = SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
    val crc32 = 999L

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId, function = function, crc32 = crc32)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.DEFAULT
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns locationRemoteId.toLong()
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
    }
    val channelConfigEntity: ChannelConfigEntity = mockk {
      every { configCrc32 } returns crc32
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())
    whenever(channelConfigRepository.findForRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelConfigEntity))

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    verify(channelConfigRepository).findForRemoteId(channelRemoteId)
    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(position = 0) wasNot called
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, channelConfigRepository)
    verifyZeroInteractions(suplaClientProvider)
  }

  @Test
  fun `should insert channel and ask for channel config`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234
    val profileId = 111L
    val function = SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId, function = function)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.DEFAULT
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.empty())
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profileEntity))
    whenever(channelRepository.insert(any())).thenReturn(Completable.complete())
    whenever(channelConfigRepository.findForRemoteId(channelRemoteId)).thenReturn(Maybe.empty())

    val suplaClient: SuplaClientApi = mockk {
      every { getChannelConfig(channelRemoteId, ChannelConfigType.DEFAULT) } returns true
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(profileRepository).findActiveProfile()
    verify(channelConfigRepository).findForRemoteId(channelRemoteId)
    verify(suplaClientProvider).provide()

    val captor = argumentCaptor<ChannelEntity>()
    verify(channelRepository).insert(captor.capture())
    with(captor.firstValue) {
      assertThat(remoteId).isEqualTo(channelRemoteId)
      assertThat(locationId).isEqualTo(locationRemoteId.toLong())
      assertThat(this.profileId).isEqualTo(profileId)
      assertThat(position).isEqualTo(0)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, channelConfigRepository, suplaClientProvider)
  }
}
