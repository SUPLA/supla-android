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
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.db.Location
import org.supla.android.testhelpers.suplaChannel
import org.supla.android.usecases.channelconfig.RequestChannelConfigUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetManager
import org.supla.android.widget.WidgetPreferences
import org.supla.core.shared.data.model.general.SuplaFunction

@Suppress("UnusedDataClassCopyResult")
@RunWith(MockitoJUnitRunner::class)
class UpdateChannelUseCaseTest {
  @Mock
  private lateinit var requestChannelConfigUseCase: RequestChannelConfigUseCase

  @Mock
  private lateinit var profileRepository: RoomProfileRepository

  @Mock
  private lateinit var channelRepository: RoomChannelRepository

  @Mock
  private lateinit var locationRepository: LocationRepository

  @Mock
  private lateinit var widgetPreferences: WidgetPreferences

  @Mock
  private lateinit var widgetManager: WidgetManager

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
    whenever(requestChannelConfigUseCase.invoke(suplaChannel)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(profileRepository).findActiveProfile()
    verify(requestChannelConfigUseCase).invoke(suplaChannel)

    val captor = argumentCaptor<ChannelEntity>()
    verify(channelRepository).insert(captor.capture())
    with(captor.firstValue) {
      assertThat(remoteId).isEqualTo(channelRemoteId)
      assertThat(locationId).isEqualTo(locationRemoteId.toLong())
      assertThat(this.profileId).isEqualTo(profileId)
      assertThat(position).isEqualTo(0)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
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
    verifyNoInteractions(requestChannelConfigUseCase, channelRepository, profileRepository)
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
    whenever(requestChannelConfigUseCase.invoke(suplaChannel)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(profileRepository).findActiveProfile()
    verify(channelRepository).findChannelCountInLocation(locationRemoteId)
    verify(requestChannelConfigUseCase).invoke(suplaChannel)

    val captor = argumentCaptor<ChannelEntity>()
    verify(channelRepository).insert(captor.capture())
    with(captor.firstValue) {
      assertThat(remoteId).isEqualTo(channelRemoteId)
      assertThat(locationId).isEqualTo(locationRemoteId.toLong())
      assertThat(this.profileId).isEqualTo(profileId)
      assertThat(position).isEqualTo(6)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
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
      every { locationId } returns locationRemoteId
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
      every { profileId } returns 123
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())
    whenever(requestChannelConfigUseCase.invoke(suplaChannel)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    verify(requestChannelConfigUseCase).invoke(suplaChannel)
    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
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
      every { remoteId } returns channelRemoteId
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns 333
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
      every {
        copy(id = 444, remoteId = channelRemoteId, caption = "", function = SuplaFunction.NONE, locationId = 333, position = 6)
      } returns this
      every { id } returns 444
      every { caption } returns ""
      every { function } returns SuplaFunction.NONE
      every { visible } returns 0
      every { altIcon } returns 0
      every { userIcon } returns 0
      every { flags } returns 0
      every { profileId } returns 0
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())
    whenever(channelRepository.findChannelCountInLocation(locationRemoteId)).thenReturn(Single.just(5))
    whenever(requestChannelConfigUseCase.invoke(suplaChannel)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    verify(channelRepository).findChannelCountInLocation(locationRemoteId)
    verify(requestChannelConfigUseCase).invoke(suplaChannel)

    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(id = 444, remoteId = channelRemoteId, locationId = 333, position = 6)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
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
      every { remoteId } returns channelRemoteId
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns 333
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 5
      every {
        copy(id = 444, remoteId = channelRemoteId, caption = "", function = SuplaFunction.NONE, locationId = 333, position = 0)
      } returns this
      every { id } returns 444
      every { caption } returns ""
      every { function } returns SuplaFunction.NONE
      every { visible } returns 0
      every { altIcon } returns 0
      every { userIcon } returns 0
      every { flags } returns 0
      every { profileId } returns 0
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())
    whenever(requestChannelConfigUseCase.invoke(suplaChannel)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    verify(requestChannelConfigUseCase).invoke(suplaChannel)

    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(id = 444, remoteId = channelRemoteId, locationId = 333, position = 0)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
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
      every { locationId } returns locationRemoteId
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
      every { profileId } returns 123
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())
    whenever(requestChannelConfigUseCase.invoke(suplaChannel)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    verify(requestChannelConfigUseCase).invoke(suplaChannel)
    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }

  @Test
  fun `should not update channel when exist, is equal and visible`() {
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
    verifyNoInteractions(requestChannelConfigUseCase, profileRepository)
  }

  @Test
  fun `should update channel and update widget`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234
    val channelProfileId = 1234L
    val widgetId = 321
    val altIcon = 222
    val userIcon = 444

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId, altIcon = altIcon, userIcon = userIcon)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns Location.SortingType.DEFAULT
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns locationRemoteId
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
      every { profileId } returns channelProfileId
    }
    val widgetConfiguration: WidgetConfiguration = mockk {
      every { copy(altIcon = altIcon, subjectFunction = SuplaFunction.NONE, userIcon = userIcon) } returns this
    }

    whenever(locationRepository.findByRemoteId(locationRemoteId)).thenReturn(Maybe.just(locationEntity))
    whenever(channelRepository.findByRemoteId(channelRemoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelRepository.update(channelEntity)).thenReturn(Completable.complete())
    whenever(widgetManager.findWidgetConfig(channelProfileId, channelRemoteId)).thenReturn(Pair(widgetId, widgetConfiguration))
    whenever(requestChannelConfigUseCase.invoke(suplaChannel)).thenReturn(Completable.complete())

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify(locationRepository).findByRemoteId(locationRemoteId)
    verify(channelRepository).findByRemoteId(channelRemoteId)
    verify(channelRepository).update(channelEntity)
    verify(widgetPreferences).setWidgetConfiguration(widgetId, widgetConfiguration)
    verify(widgetManager).updateWidget(widgetId)
    verify(requestChannelConfigUseCase).invoke(suplaChannel)
    io.mockk.verify {
      channelEntity.updatedBy(suplaChannel)
    }

    verifyNoMoreInteractions(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }
}
