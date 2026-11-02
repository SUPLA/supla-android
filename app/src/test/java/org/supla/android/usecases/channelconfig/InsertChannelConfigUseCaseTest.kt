package org.supla.android.usecases.channelconfig
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
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelConfigMeterCounterType
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeasurementConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeterConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.events.DownloadEventsManager
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_NONE

class InsertChannelConfigUseCaseTest {

  @MockK
  private lateinit var channelConfigRepository: ChannelConfigRepository

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository

  @MockK
  private lateinit var downloadEventsManager: DownloadEventsManager

  @InjectMockKs
  private lateinit var useCase: InsertChannelConfigUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should do nothing when result is false`() {
    // given
    val result = ConfigResult.RESULT_FALSE

    // when
    val observer = useCase.invoke(null, result).test()

    // then
    observer.assertComplete()
    verify {
      channelConfigRepository wasNot Called
      profileRepository wasNot Called
    }
    verify {
      generalPurposeMeterLogRepository wasNot Called
      downloadEventsManager wasNot Called
    }
  }

  @Test
  fun `should insert general purpose measurement config`() {
    // given
    val profileId = 222L
    val result = ConfigResult.RESULT_TRUE
    val config: SuplaChannelGeneralPurposeMeasurementConfig = mockk {
      every { remoteId } returns 123
      every { func } returns SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    every { profileRepository.findActiveProfile() } returns Single.just(profileEntity)
    every { channelConfigRepository.insertOrUpdate(profileId, config) } returns Completable.complete()

    // when
    val observer = useCase.invoke(config, result).test()

    // then
    observer.assertComplete()
    verify { profileRepository.findActiveProfile() }
    verify { channelConfigRepository.insertOrUpdate(profileId, config) }

    confirmVerified(profileRepository, channelConfigRepository)
    verify {
      generalPurposeMeterLogRepository wasNot Called
      downloadEventsManager wasNot Called
    }
  }

  @Test
  fun `should insert general purpose meter config when not found`() {
    // given
    val profileId = 222L
    val remoteId = 123
    val result = ConfigResult.RESULT_TRUE
    val config: SuplaChannelGeneralPurposeMeterConfig = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { func } returns SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    every { profileRepository.findActiveProfile() } returns Single.just(profileEntity)
    every { channelConfigRepository.insertOrUpdate(profileId, config) } returns Completable.complete()
    every { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER) } returns
      Single.error(EmptyResultSetException(""))

    // when
    val observer = useCase.invoke(config, result).test()

    // then
    observer.assertComplete()
    verify { profileRepository.findActiveProfile() }
    verify { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER) }
    verify(exactly = 2) { channelConfigRepository.insertOrUpdate(profileId, config) }

    confirmVerified(profileRepository, channelConfigRepository)
    verify {
      generalPurposeMeterLogRepository wasNot Called
      downloadEventsManager wasNot Called
    }
  }

  @Test
  fun `should insert general purpose meter config when found`() {
    // given
    val profileId = 222L
    val remoteId = 123
    val result = ConfigResult.RESULT_TRUE
    val config: SuplaChannelGeneralPurposeMeterConfig = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { func } returns SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
      every { counterType } returns SuplaChannelConfigMeterCounterType.ALWAYS_INCREMENT
      every { fillMissingData } returns true
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    every { profileRepository.findActiveProfile() } returns Single.just(profileEntity)
    every { channelConfigRepository.insertOrUpdate(profileId, config) } returns Completable.complete()
    every { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER) } returns
      Single.just(config)

    // when
    val observer = useCase.invoke(config, result).test()

    // then
    observer.assertComplete()
    verify { profileRepository.findActiveProfile() }
    verify { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER) }
    verify { channelConfigRepository.insertOrUpdate(profileId, config) }

    confirmVerified(profileRepository, channelConfigRepository)
    verify {
      generalPurposeMeterLogRepository wasNot Called
      downloadEventsManager wasNot Called
    }
  }

  @Test
  fun `should insert general purpose meter config and delete history`() {
    // given
    val profileId = 222L
    val remoteId = 123
    val result = ConfigResult.RESULT_TRUE
    val config: SuplaChannelGeneralPurposeMeterConfig = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { func } returns SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
      every { counterType } returns SuplaChannelConfigMeterCounterType.ALWAYS_INCREMENT
      every { fillMissingData } returns true andThen false
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    every { downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.Refresh) } just Runs
    every { profileRepository.findActiveProfile() } returns Single.just(profileEntity)
    every { channelConfigRepository.insertOrUpdate(profileId, config) } returns Completable.complete()
    every { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER) } returns
      Single.just(config)
    every { generalPurposeMeterLogRepository.delete(remoteId, profileId) } returns Completable.complete()

    // when
    val observer = useCase.invoke(config, result).test()

    // then
    observer.assertComplete()
    verify { profileRepository.findActiveProfile() }
    verify { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER) }
    verify { channelConfigRepository.insertOrUpdate(profileId, config) }
    verify { generalPurposeMeterLogRepository.delete(remoteId, profileId) }
    verify { downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.Refresh) }

    confirmVerified(profileRepository, channelConfigRepository, generalPurposeMeterLogRepository, downloadEventsManager)
  }

  @Test
  fun `should delete config if could not store it`() {
    // given
    val profileId = 222L
    val channelRemoteId = 123
    val result = ConfigResult.RESULT_TRUE
    val config: SuplaChannelConfig = mockk {
      every { remoteId } returns channelRemoteId
      every { func } returns SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
    }
    val profileEntity: ProfileEntity = mockk {
      every { id } returns profileId
    }

    every { profileRepository.findActiveProfile() } returns Single.just(profileEntity)
    every { channelConfigRepository.delete(profileId, channelRemoteId) } returns Completable.complete()

    // when
    val observer = useCase.invoke(config, result).test()

    // then
    observer.assertComplete()
    verify { profileRepository.findActiveProfile() }
    verify { channelConfigRepository.delete(profileId, channelRemoteId) }

    confirmVerified(profileRepository, channelConfigRepository)
    verify {
      generalPurposeMeterLogRepository wasNot Called
      downloadEventsManager wasNot Called
    }
  }

  @Test
  fun `should do nothing if config should not be stored`() {
    // given
    val result = ConfigResult.RESULT_TRUE
    val config: SuplaChannelWeeklyScheduleConfig = mockk {
      every { remoteId } returns 123
      every { func } returns SUPLA_CHANNELFNC_NONE
    }

    // when
    val observer = useCase.invoke(config, result).test()

    // then
    observer.assertComplete()
    confirmVerified(
      profileRepository,
      channelConfigRepository,
      generalPurposeMeterLogRepository,
      downloadEventsManager
    )
  }
}
