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
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.model.general.EntityUpdateResult
import org.supla.android.data.source.ChannelExtendedValueRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.extensions.date
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.usecases.channelstate.UpdateChannelStateUseCase
import java.util.Date

class UpdateChannelExtendedValueUseCaseTest {
  @MockK
  private lateinit var profileRepository: RoomProfileRepository

  @MockK
  private lateinit var channelExtendedValueRepository: ChannelExtendedValueRepository

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var updateChannelStateUseCase: UpdateChannelStateUseCase

  @InjectMockKs
  private lateinit var useCase: UpdateChannelExtendedValueUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should insert channel extended value when channel extended value not found`() {
    // given
    val channelId = 234
    val channelExtendedValue: SuplaChannelExtendedValue = mockk(relaxed = true) {
      every { timerEstimatedEndDate } returns null
    }
    val profile: ProfileEntity = mockk {
      every { id } returns 123
    }

    every { channelExtendedValueRepository.findByRemoteId(channelId) } returns Single.error(NoSuchElementException())
    every { profileRepository.findActiveProfile() } returns Single.just(profile)
    every { channelExtendedValueRepository.insert(any()) } returns Completable.complete()
    every { updateChannelStateUseCase.invoke(any()) } returns Completable.complete()

    // when
    val result = useCase.invoke(channelId, channelExtendedValue).test()

    // then
    result.assertComplete()
    val entitySlot = slot<ChannelExtendedValueEntity>()
    verify {
      channelExtendedValueRepository.findByRemoteId(channelId)
      profileRepository.findActiveProfile()
      channelExtendedValueRepository.insert(capture(entitySlot))
    }
    confirmVerified(channelExtendedValueRepository, profileRepository, dateProvider)

    assertThat(result.values()).isEqualTo(listOf(UpdateExtendedValueResult(EntityUpdateResult.UPDATED, false)))
    with(entitySlot.captured) {
      assertThat(this.channelId).isEqualTo(channelId)
      assertThat(this.profileId).isEqualTo(profile.id)
      assertThat(this.timerStartTime).isNull()
    }
  }

  @Test
  fun `should insert extended value when not found and create timestamp for timer start`() {
    // given
    val channelId = 234
    val timerStartDate = Date()
    val channelExtendedValue: SuplaChannelExtendedValue = mockk(relaxed = true) {
      every { timerEstimatedEndDate } returns Date()
    }
    val profile: ProfileEntity = mockk {
      every { id } returns 123
    }

    every { channelExtendedValueRepository.findByRemoteId(channelId) } returns Single.error(NoSuchElementException())
    every { profileRepository.findActiveProfile() } returns Single.just(profile)
    every { channelExtendedValueRepository.insert(any()) } returns Completable.complete()
    every { dateProvider.currentDate() } returns timerStartDate
    every { updateChannelStateUseCase.invoke(any()) } returns Completable.complete()

    // when
    val result = useCase.invoke(channelId, channelExtendedValue).test()

    // then
    result.assertComplete()
    val entitySlot = slot<ChannelExtendedValueEntity>()
    verify {
      channelExtendedValueRepository.findByRemoteId(channelId)
      profileRepository.findActiveProfile()
      channelExtendedValueRepository.insert(capture(entitySlot))
      dateProvider.currentDate()
    }
    confirmVerified(channelExtendedValueRepository, profileRepository, dateProvider)

    assertThat(result.values()).isEqualTo(listOf(UpdateExtendedValueResult(EntityUpdateResult.UPDATED, true)))
    with(entitySlot.captured) {
      assertThat(this.channelId).isEqualTo(channelId)
      assertThat(this.profileId).isEqualTo(profile.id)
      assertThat(this.timerStartTime).isEqualTo(timerStartDate)
    }
  }

  @Suppress("UnusedDataClassCopyResult")
  @Test
  fun `should update extended value when exists and do not change timer start time`() {
    // given
    val channelId = 234
    val startTime = Date()
    val channelExtendedValue: SuplaChannelExtendedValue = mockk(relaxed = true) {
      every { timerEstimatedEndDate } returns date(2024, 12, 4)
    }
    val entity: ChannelExtendedValueEntity = mockk(relaxed = true) {
      every { getSuplaValue() } returns mockk {
        every { timerEstimatedEndDate } returns date(2024, 12, 4)
      }
      every { timerStartTime } returns startTime
    }

    every { channelExtendedValueRepository.findByRemoteId(channelId) } returns Single.just(entity)
    every { channelExtendedValueRepository.update(any()) } returns Completable.complete()
    every { updateChannelStateUseCase.invoke(any()) } returns Completable.complete()

    // when
    val result = useCase.invoke(channelId, channelExtendedValue).test()

    // then
    result.assertComplete()
    verify {
      channelExtendedValueRepository.findByRemoteId(channelId)
      channelExtendedValueRepository.update(any())
      entity.copy(value = any(), timerStartTime = eq(startTime))
    }
    confirmVerified(channelExtendedValueRepository, profileRepository, dateProvider)

    assertThat(result.values()).isEqualTo(listOf(UpdateExtendedValueResult(EntityUpdateResult.UPDATED, false)))
  }

  @Suppress("UnusedDataClassCopyResult")
  @Test
  fun `should update extended value and cleanup timer start time`() {
    // given
    val channelId = 234
    val channelExtendedValue: SuplaChannelExtendedValue = mockk(relaxed = true) {
      every { timerEstimatedEndDate } returns null
    }
    val entity: ChannelExtendedValueEntity = mockk(relaxed = true) {
      every { getSuplaValue() } returns mockk {
        every { timerEstimatedEndDate } returns date(2024, 12, 4)
      }
      every { timerStartTime } returns date(2024, 12, 3)
    }

    every { channelExtendedValueRepository.findByRemoteId(channelId) } returns Single.just(entity)
    every { channelExtendedValueRepository.update(any()) } returns Completable.complete()
    every { updateChannelStateUseCase.invoke(any()) } returns Completable.complete()

    // when
    val result = useCase.invoke(channelId, channelExtendedValue).test()

    // then
    result.assertComplete()
    verify {
      channelExtendedValueRepository.findByRemoteId(channelId)
      channelExtendedValueRepository.update(any())
      entity.copy(value = any(), timerStartTime = isNull())
    }
    confirmVerified(channelExtendedValueRepository, profileRepository, dateProvider)

    assertThat(result.values()).isEqualTo(listOf(UpdateExtendedValueResult(EntityUpdateResult.UPDATED, true)))
  }

  @Suppress("UnusedDataClassCopyResult")
  @Test
  fun `should update extended value and set timer start time to current date`() {
    // given
    val channelId = 234
    val currentDate = Date()
    val channelExtendedValue: SuplaChannelExtendedValue = mockk(relaxed = true) {
      every { timerEstimatedEndDate } returns date(2024, 12, 4)
    }
    val entity: ChannelExtendedValueEntity = mockk(relaxed = true) {
      every { getSuplaValue() } returns mockk {
        every { timerEstimatedEndDate } returns date(2024, 12, 3)
      }
      every { timerStartTime } returns date(2024, 12, 2)
    }

    every { channelExtendedValueRepository.findByRemoteId(channelId) } returns Single.just(entity)
    every { channelExtendedValueRepository.update(any()) } returns Completable.complete()
    every { dateProvider.currentDate() } returns currentDate
    every { updateChannelStateUseCase.invoke(any()) } returns Completable.complete()

    // when
    val result = useCase.invoke(channelId, channelExtendedValue).test()

    // then
    result.assertComplete()
    verify {
      channelExtendedValueRepository.findByRemoteId(channelId)
      channelExtendedValueRepository.update(any())
      dateProvider.currentDate()
      entity.copy(value = any(), timerStartTime = eq(currentDate))
    }
    confirmVerified(channelExtendedValueRepository, profileRepository, dateProvider)

    assertThat(result.values()).isEqualTo(listOf(UpdateExtendedValueResult(EntityUpdateResult.UPDATED, true)))
  }
}
