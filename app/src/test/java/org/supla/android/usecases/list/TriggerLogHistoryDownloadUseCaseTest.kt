package org.supla.android.usecases.list

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
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Maybe
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.settings.ImpulseCounterSettings
import org.supla.android.data.model.settings.ListValue
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import java.util.Date

class TriggerLogHistoryDownloadUseCaseTest {

  @MockK
  private lateinit var impulseCounterLogRepository: ImpulseCounterLogRepository

  @MockK
  private lateinit var profileRepository: RoomProfileRepository

  @MockK
  private lateinit var channelRepository: RoomChannelRepository

  @MockK
  private lateinit var userStateHolder: UserStateHolder

  @MockK
  private lateinit var downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase

  @MockK
  private lateinit var dateProvider: DateProvider

  @InjectMockKs
  private lateinit var useCase: TriggerLogHistoryDownloadUseCase

  private val profileId = 1L
  private val activeProfile = mockk<ProfileEntity> {
    every { id } returns profileId
  }

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    coEvery { profileRepository.findActiveProfileKtx() } returns activeProfile
  }

  @Test
  fun `should return early when no active profile`() = runTest {
    // given
    coEvery { profileRepository.findActiveProfileKtx() } returns null

    // when
    useCase.invoke()

    // then
    coVerify(exactly = 0) { channelRepository.findChannelsBy(any(), any()) }
  }

  @Test
  fun `should trigger download for channel without logs`() = runTest {
    // given
    val remoteId = 10
    val channel = createChannel(remoteId = remoteId)
    setupChannels(listOf(channel))
    setupSettings(remoteId, ListValue.CURRENT_HOUR)

    val now = Date(1000000)
    every { dateProvider.currentDate() } returns now
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    coEvery { downloadChannelMeasurementsUseCase.invoke(any()) } returns Unit

    // when
    useCase.invoke()

    // then
    coVerify { downloadChannelMeasurementsUseCase.invoke(match { it.channel == channel }) }
  }

  @Test
  fun `should not trigger download twice for channel without logs within interval`() = runTest {
    // given
    val remoteId = 10
    val channel = createChannel(remoteId = remoteId)
    setupChannels(listOf(channel))
    setupSettings(remoteId, ListValue.CURRENT_HOUR)

    val t1 = Date(1000000)
    val t2 = Date(1000000 + 5 * 60 * 1000) // 5 minutes later, interval is 10 min

    every { dateProvider.currentDate() } returns t1
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    coEvery { downloadChannelMeasurementsUseCase.invoke(any()) } returns Unit

    // when
    useCase.invoke() // first call

    every { dateProvider.currentDate() } returns t2
    useCase.invoke() // second call

    // then
    coVerify(exactly = 1) { downloadChannelMeasurementsUseCase.invoke(any()) }
  }

  @Test
  fun `should trigger download twice for channel without logs after interval`() = runTest {
    // given
    val remoteId = 10
    val channel = createChannel(remoteId = remoteId)
    setupChannels(listOf(channel))
    setupSettings(remoteId, ListValue.CURRENT_HOUR)

    val t1 = Date(1000000)
    val t2 = Date(1000000 + 11 * 60 * 1000) // 11 minutes later, interval is 10 min

    every { dateProvider.currentDate() } returns t1
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    coEvery { downloadChannelMeasurementsUseCase.invoke(any()) } returns Unit

    // when
    useCase.invoke() // first call

    every { dateProvider.currentDate() } returns t2
    useCase.invoke() // second call

    // then
    coVerify(exactly = 2) { downloadChannelMeasurementsUseCase.invoke(any()) }
  }

  @Test
  fun `should trigger download for channel with old logs`() = runTest {
    // given
    val remoteId = 10
    val channel = createChannel(remoteId = remoteId)
    setupChannels(listOf(channel))
    setupSettings(remoteId, ListValue.CURRENT_HOUR)

    val lastEntryDate = Date(1000000)
    val now = Date(1000000 + 11 * 60 * 1000) // 11 minutes later, interval is 10 min

    val lastEntry = mockk<ImpulseCounterLogEntity> {
      every { date } returns lastEntryDate
    }

    every { dateProvider.currentDate() } returns now
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntry)
    coEvery { downloadChannelMeasurementsUseCase.invoke(any()) } returns Unit

    // when
    useCase.invoke()

    // then
    coVerify { downloadChannelMeasurementsUseCase.invoke(any()) }
  }

  @Test
  fun `should not trigger download for channel with fresh logs`() = runTest {
    // given
    val remoteId = 10
    val channel = createChannel(remoteId = remoteId)
    setupChannels(listOf(channel))
    setupSettings(remoteId, ListValue.CURRENT_HOUR)

    val lastEntryDate = Date(1000000)
    val now = Date(1000000 + 5 * 60 * 1000) // 5 minutes later, interval is 10 min

    val lastEntry = mockk<ImpulseCounterLogEntity> {
      every { date } returns lastEntryDate
    }

    every { dateProvider.currentDate() } returns now
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntry)

    // when
    useCase.invoke()

    // then
    coVerify(exactly = 0) { downloadChannelMeasurementsUseCase.invoke(any()) }
  }

  @Test
  fun `should use longer interval for OCR channel`() = runTest {
    // given
    val remoteId = 10
    val channel = createChannel(remoteId = remoteId, flags = 0x8) // OCR flag rawValue = 0x8
    setupChannels(listOf(channel))
    setupSettings(remoteId, ListValue.CURRENT_HOUR)

    val lastEntryDate = Date(1000000)
    val now1 = Date(1000000 + 30 * 60 * 1000) // 30 minutes later. Normal (10) would trigger, OCR (60) should not.

    val lastEntry = mockk<ImpulseCounterLogEntity> {
      every { date } returns lastEntryDate
    }

    every { dateProvider.currentDate() } returns now1
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntry)

    // when
    useCase.invoke()

    // then
    coVerify(exactly = 0) { downloadChannelMeasurementsUseCase.invoke(any()) }

    // try after OCR interval (61 minutes)
    val now2 = Date(1000000 + 61 * 60 * 1000)
    every { dateProvider.currentDate() } returns now2
    coEvery { downloadChannelMeasurementsUseCase.invoke(any()) } returns Unit

    useCase.invoke()

    coVerify(exactly = 1) { downloadChannelMeasurementsUseCase.invoke(any()) }
  }

  @Test
  fun `should skip channel when showOnList is COUNTER_STATE`() = runTest {
    // given
    val remoteId = 11
    val channel = createChannel(remoteId = remoteId)
    setupChannels(listOf(channel))
    setupSettings(remoteId, ListValue.COUNTER_STATE)

    // when
    useCase.invoke()

    // then
    verify(exactly = 0) { impulseCounterLogRepository.findOldestEntity(any(), any()) }
  }

  private fun createChannel(remoteId: Int, flags: Long = 0L) = mockk<ChannelDataEntity> {
    every { this@mockk.remoteId } returns remoteId
    every { this@mockk.profileId } returns this@TriggerLogHistoryDownloadUseCaseTest.profileId
    every { this@mockk.flags } returns flags
  }

  private fun setupChannels(channels: List<ChannelDataEntity>) {
    coEvery { channelRepository.findChannelsBy(profileId, SuplaFunction.IC_GAS_METER) } returns channels
    coEvery { channelRepository.findChannelsBy(profileId, SuplaFunction.IC_HEAT_METER) } returns emptyList()
    coEvery { channelRepository.findChannelsBy(profileId, SuplaFunction.IC_WATER_METER) } returns emptyList()
    coEvery { channelRepository.findChannelsBy(profileId, SuplaFunction.IC_ELECTRICITY_METER) } returns emptyList()
    coEvery { channelRepository.findChannelsBy(profileId, SuplaFunction.STAIRCASE_TIMER) } returns emptyList()
    coEvery { channelRepository.findChannelsBy(profileId, SuplaFunction.LIGHTSWITCH) } returns emptyList()
    coEvery { channelRepository.findChannelsBy(profileId, SuplaFunction.POWER_SWITCH) } returns emptyList()
  }

  private fun setupSettings(remoteId: Int, showOnList: ListValue) {
    every { userStateHolder.getImpulseCounterSettings(profileId, remoteId) } returns ImpulseCounterSettings(
      showOnList = showOnList
    )
  }
}
