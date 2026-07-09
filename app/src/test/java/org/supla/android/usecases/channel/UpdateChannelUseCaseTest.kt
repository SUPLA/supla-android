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

import androidx.room.rxjava3.EmptyResultSetException
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
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.custom.LocationSortingType
import org.supla.android.testhelpers.suplaChannel
import org.supla.android.usecases.channelconfig.RequestChannelConfigUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetManager
import org.supla.android.widget.WidgetPreferences
import org.supla.core.shared.data.model.general.SuplaFunction

@Suppress("UnusedDataClassCopyResult")
class UpdateChannelUseCaseTest {

  @MockK
  private lateinit var requestChannelConfigUseCase: RequestChannelConfigUseCase

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var channelRepository: ChannelRepository

  @MockK
  private lateinit var locationRepository: LocationRepository

  @MockK
  private lateinit var widgetPreferences: WidgetPreferences

  @MockK
  private lateinit var widgetManager: WidgetManager

  @InjectMockKs
  private lateinit var useCase: UpdateChannelUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should insert channel when not exist`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234
    val profileId = 111L

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns LocationSortingType.DEFAULT
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.just(locationEntity)
    every { channelRepository.findByRemoteId(channelRemoteId) } returns Maybe.empty()
    every { profileRepository.findActiveProfile() } returns Single.just(profileEntity)
    every { channelRepository.insert(any()) } returns Completable.complete()
    every { requestChannelConfigUseCase.invoke(suplaChannel) } returns Completable.complete()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify { locationRepository.findByRemoteId(locationRemoteId) }
    verify { channelRepository.findByRemoteId(channelRemoteId) }
    verify { profileRepository.findActiveProfile() }
    verify { requestChannelConfigUseCase.invoke(suplaChannel) }

    val captor = slot<ChannelEntity>()
    verify { channelRepository.insert(capture(captor)) }
    with(captor.captured) {
      assertThat(remoteId).isEqualTo(channelRemoteId)
      assertThat(locationId).isEqualTo(locationRemoteId.toLong())
      assertThat(this.profileId).isEqualTo(profileId)
      assertThat(position).isEqualTo(0)
    }

    confirmVerified(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }

  @Test
  fun `should not insert channel when location not exist`() {
    // given
    val locationRemoteId = 123

    val suplaChannel = suplaChannel(locationRemoteId)

    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.empty()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.ERROR)

    verify { locationRepository.findByRemoteId(locationRemoteId) }

    confirmVerified(locationRepository, requestChannelConfigUseCase, channelRepository, profileRepository)
  }

  @Test
  fun `should insert channel when not exist and set position to last`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234
    val profileId = 111L

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns LocationSortingType.USER_DEFINED
      every { remoteId } returns locationRemoteId
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.just(locationEntity)
    every { channelRepository.findByRemoteId(channelRemoteId) } returns Maybe.empty()
    every { profileRepository.findActiveProfile() } returns Single.just(profileEntity)
    every { channelRepository.insert(any()) } returns Completable.complete()
    every { channelRepository.findMaxPositionInLocation(locationRemoteId) } returns Single.just(5)
    every { requestChannelConfigUseCase.invoke(suplaChannel) } returns Completable.complete()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify { locationRepository.findByRemoteId(locationRemoteId) }
    verify { channelRepository.findByRemoteId(channelRemoteId) }
    verify { profileRepository.findActiveProfile() }
    verify { channelRepository.findMaxPositionInLocation(locationRemoteId) }
    verify { requestChannelConfigUseCase.invoke(suplaChannel) }

    val captor = slot<ChannelEntity>()
    verify { channelRepository.insert(capture(captor)) }
    with(captor.captured) {
      assertThat(remoteId).isEqualTo(channelRemoteId)
      assertThat(locationId).isEqualTo(locationRemoteId.toLong())
      assertThat(this.profileId).isEqualTo(profileId)
      assertThat(position).isEqualTo(6)
    }

    confirmVerified(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }

  @Test
  fun `should update channel when exist`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns LocationSortingType.DEFAULT
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns locationRemoteId
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
      every { profileId } returns 123
    }

    every { widgetManager.findWidgetConfig(123, channelRemoteId) } returns null
    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.just(locationEntity)
    every { channelRepository.findByRemoteId(channelRemoteId) } returns Maybe.just(channelEntity)
    every { channelRepository.update(channelEntity) } returns Completable.complete()
    every { requestChannelConfigUseCase.invoke(suplaChannel) } returns Completable.complete()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify { locationRepository.findByRemoteId(locationRemoteId) }
    verify { channelRepository.findByRemoteId(channelRemoteId) }
    verify { channelRepository.update(channelEntity) }
    verify { requestChannelConfigUseCase.invoke(suplaChannel) }
    verify { channelEntity.updatedBy(suplaChannel) }

    confirmVerified(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }

  @Test
  fun `should update channel when exist and set position to last`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns LocationSortingType.USER_DEFINED
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

    every { widgetManager.findWidgetConfig(0, channelRemoteId) } returns null
    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.just(locationEntity)
    every { channelRepository.findByRemoteId(channelRemoteId) } returns Maybe.just(channelEntity)
    every { channelRepository.update(channelEntity) } returns Completable.complete()
    every { channelRepository.findMaxPositionInLocation(locationRemoteId) } returns Single.just(5)
    every { requestChannelConfigUseCase.invoke(suplaChannel) } returns Completable.complete()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify {
      locationRepository.findByRemoteId(locationRemoteId)
      channelRepository.findByRemoteId(channelRemoteId)
      channelRepository.update(channelEntity)
      channelRepository.findMaxPositionInLocation(locationRemoteId)
      requestChannelConfigUseCase.invoke(suplaChannel)
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(id = 444, remoteId = channelRemoteId, locationId = 333, position = 6)
    }

    confirmVerified(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }

  @Test
  fun `should update channel when exist and set position to 0 when location sorting is default`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns LocationSortingType.DEFAULT
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

    every { widgetManager.findWidgetConfig(0, channelRemoteId) } returns null
    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.just(locationEntity)
    every { channelRepository.findByRemoteId(channelRemoteId) } returns Maybe.just(channelEntity)
    every { channelRepository.update(channelEntity) } returns Completable.complete()
    every { requestChannelConfigUseCase.invoke(suplaChannel) } returns Completable.complete()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify {
      locationRepository.findByRemoteId(locationRemoteId)
      channelRepository.findByRemoteId(channelRemoteId)
      channelRepository.update(channelEntity)
      requestChannelConfigUseCase.invoke(suplaChannel)
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(id = 444, remoteId = channelRemoteId, locationId = 333, position = 0)
    }

    confirmVerified(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }

  @Test
  fun `should update channel when exist and set position to 0 when location sorting is user and no channel available`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns LocationSortingType.USER_DEFINED
      every { remoteId } returns locationRemoteId
    }
    val channelEntity: ChannelEntity = mockk {
      every { remoteId } returns channelRemoteId
      every { differsFrom(suplaChannel) } returns true
      every { locationId } returns 333
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 5
      every {
        copy(id = 444, remoteId = channelRemoteId, caption = "", function = SuplaFunction.NONE, locationId = 333, position = 1)
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

    every { widgetManager.findWidgetConfig(0, channelRemoteId) } returns null
    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.just(locationEntity)
    every { channelRepository.findByRemoteId(channelRemoteId) } returns Maybe.just(channelEntity)
    every { channelRepository.update(channelEntity) } returns Completable.complete()
    every { requestChannelConfigUseCase.invoke(suplaChannel) } returns Completable.complete()
    every { channelRepository.findMaxPositionInLocation(locationRemoteId) } returns Single.error(EmptyResultSetException(""))

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify { locationRepository.findByRemoteId(locationRemoteId) }
    verify { channelRepository.findByRemoteId(channelRemoteId) }
    verify { channelRepository.update(channelEntity) }
    verify { channelRepository.findMaxPositionInLocation(locationRemoteId) }
    verify { requestChannelConfigUseCase.invoke(suplaChannel) }

    verify {
      channelEntity.updatedBy(suplaChannel)
      channelEntity.copy(id = 444, remoteId = channelRemoteId, locationId = 333, position = 1)
    }

    confirmVerified(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }

  @Test
  fun `should update channel when exist is same but not visible`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns LocationSortingType.DEFAULT
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns false
      every { visible } returns 0
      every { locationId } returns locationRemoteId
      every { updatedBy(suplaChannel) } returns this
      every { position } returns 0
      every { profileId } returns 123
    }

    every { widgetManager.findWidgetConfig(123, channelRemoteId) } returns null
    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.just(locationEntity)
    every { channelRepository.findByRemoteId(channelRemoteId) } returns Maybe.just(channelEntity)
    every { channelRepository.update(channelEntity) } returns Completable.complete()
    every { requestChannelConfigUseCase.invoke(suplaChannel) } returns Completable.complete()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify { locationRepository.findByRemoteId(locationRemoteId) }
    verify { channelRepository.findByRemoteId(channelRemoteId) }
    verify { channelRepository.update(channelEntity) }
    verify { requestChannelConfigUseCase.invoke(suplaChannel) }
    verify { channelEntity.updatedBy(suplaChannel) }

    confirmVerified(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }

  @Test
  fun `should not update channel when exist, is equal and visible`() {
    // given
    val locationRemoteId = 123
    val channelRemoteId = 234

    val suplaChannel = suplaChannel(locationRemoteId, channelRemoteId)
    val locationEntity: LocationEntity = mockk {
      every { sorting } returns LocationSortingType.DEFAULT
    }
    val channelEntity: ChannelEntity = mockk {
      every { differsFrom(suplaChannel) } returns false
      every { visible } returns 1
    }

    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.just(locationEntity)
    every { channelRepository.findByRemoteId(channelRemoteId) } returns Maybe.just(channelEntity)

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.NOP)

    verify { locationRepository.findByRemoteId(locationRemoteId) }
    verify { channelRepository.findByRemoteId(channelRemoteId) }

    confirmVerified(locationRepository, channelRepository, requestChannelConfigUseCase, profileRepository)
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
      every { sorting } returns LocationSortingType.DEFAULT
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

    every { widgetPreferences.setWidgetConfiguration(widgetId, widgetConfiguration) } just Runs
    every { locationRepository.findByRemoteId(locationRemoteId) } returns Maybe.just(locationEntity)
    every { channelRepository.findByRemoteId(channelRemoteId) } returns Maybe.just(channelEntity)
    every { channelRepository.update(channelEntity) } returns Completable.complete()
    every { widgetManager.findWidgetConfig(channelProfileId, channelRemoteId) } returns Pair(widgetId, widgetConfiguration)
    every { widgetManager.updateWidget(widgetId) } just Runs
    every { requestChannelConfigUseCase.invoke(suplaChannel) } returns Completable.complete()

    // when
    val result = useCase.invoke(suplaChannel).test()

    // then
    result.assertComplete()
    result.assertResult(EntityUpdateResult.UPDATED)

    verify { locationRepository.findByRemoteId(locationRemoteId) }
    verify { channelRepository.findByRemoteId(channelRemoteId) }
    verify { channelRepository.update(channelEntity) }
    verify { widgetPreferences.setWidgetConfiguration(widgetId, widgetConfiguration) }
    verify { widgetManager.updateWidget(widgetId) }
    verify { requestChannelConfigUseCase.invoke(suplaChannel) }
    verify { channelEntity.updatedBy(suplaChannel) }

    confirmVerified(locationRepository, channelRepository, profileRepository, requestChannelConfigUseCase)
  }
}
