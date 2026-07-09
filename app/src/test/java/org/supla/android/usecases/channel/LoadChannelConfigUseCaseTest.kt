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

import android.annotation.SuppressLint
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.core.shared.data.model.general.SuplaFunction

class LoadChannelConfigUseCaseTest {

  @MockK
  private lateinit var channelConfigRepository: ChannelConfigRepository

  @MockK
  private lateinit var channelRepository: ChannelRepository

  @InjectMockKs
  private lateinit var useCase: LoadChannelConfigUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should load general purpose measurement config`() {
    // given
    val profileId = 213L
    val remoteId = 123
    val channelEntity: ChannelEntity = mockk {
      every { function } returns SuplaFunction.GENERAL_PURPOSE_MEASUREMENT
      every { this@mockk.profileId } returns profileId
    }
    val config: SuplaChannelConfig = mockk()
    every { channelRepository.findByRemoteId(remoteId) } returns Maybe.just(channelEntity)
    every { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_MEASUREMENT) } returns
      Single.just(config)

    // when
    val resultConfig = useCase(remoteId).test()

    // then
    resultConfig.assertComplete()
    resultConfig.assertResult(config)
  }

  @Test
  fun `should load general purpose meter config`() {
    // given
    val profileId = 213L
    val remoteId = 123
    val channelEntity: ChannelEntity = mockk {
      every { function } returns SuplaFunction.GENERAL_PURPOSE_METER
      every { this@mockk.profileId } returns profileId
    }
    val config: SuplaChannelConfig = mockk()
    every { channelRepository.findByRemoteId(remoteId) } returns Maybe.just(channelEntity)
    every { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER) } returns
      Single.just(config)

    // when
    val resultConfig = useCase(remoteId).test()

    // then
    resultConfig.assertComplete()
    resultConfig.assertResult(config)
  }

  @SuppressLint("CheckResult")
  @Test
  fun `should fail when function not supported yet`() {
    // given
    val remoteId = 123
    val channelEntity: ChannelEntity = mockk {
      every { function } returns SuplaFunction.ALARM
      every { profileId } returns 1L
    }
    every { channelRepository.findByRemoteId(remoteId) } returns Maybe.just(channelEntity)

    // when
    val throwable = catchThrowable {
      useCase(remoteId).blockingGet()
    }

    // then
    assertThat(throwable)
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Channel not supported (function: `${SuplaFunction.ALARM}`)")
  }
}
