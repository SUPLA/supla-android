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
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_ALARM
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

@RunWith(MockitoJUnitRunner::class)
class LoadChannelConfigUseCaseTest {

  @Mock
  private lateinit var channelConfigRepository: ChannelConfigRepository

  @Mock
  private lateinit var channelRepository: RoomChannelRepository

  @InjectMocks
  private lateinit var useCase: LoadChannelConfigUseCase

  @Test
  fun `should load general purpose measurement config`() {
    // given
    val profileId = 213L
    val remoteId = 123
    val channelEntity: ChannelEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
    }
    val config: SuplaChannelConfig = mockk()
    whenever(channelRepository.findByRemoteId(profileId, remoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelConfigRepository.findGpmConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_MEASUREMENT))
      .thenReturn(Single.just(config))

    // when
    val resultConfig = useCase(profileId, remoteId).test()

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
      every { function } returns SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
    }
    val config: SuplaChannelConfig = mockk()
    whenever(channelRepository.findByRemoteId(profileId, remoteId)).thenReturn(Maybe.just(channelEntity))
    whenever(channelConfigRepository.findGpmConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER))
      .thenReturn(Single.just(config))

    // when
    val resultConfig = useCase(profileId, remoteId).test()

    // then
    resultConfig.assertComplete()
    resultConfig.assertResult(config)
  }

  @SuppressLint("CheckResult")
  @Test
  fun `should fail when function not supported yet`() {
    // given
    val profileId = 213L
    val remoteId = 123
    val channelEntity: ChannelEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_ALARM
    }
    whenever(channelRepository.findByRemoteId(profileId, remoteId)).thenReturn(Maybe.just(channelEntity))

    // when
    val throwable = catchThrowable {
      useCase(profileId, remoteId).blockingGet()
    }

    // then
    assertThat(throwable)
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Channel not supported (function: `$SUPLA_CHANNELFNC_ALARM`)")
  }
}
