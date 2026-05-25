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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelValueRepository

class SetChannelsOfflineUseCaseTest {

  @MockK
  private lateinit var channelValueRepository: ChannelValueRepository

  @InjectMockKs
  private lateinit var useCase: SetChannelsOfflineUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should return true when any channel was updated`() {
    coEvery { channelValueRepository.setChannelsOffline() } returns true

    val result = useCase.invoke()

    assertThat(result).isTrue()

    coVerify {
      channelValueRepository.setChannelsOffline()
    }
    confirmVerified(channelValueRepository)
  }

  @Test
  fun `should return false when no channels were updated`() {
    coEvery { channelValueRepository.setChannelsOffline() } returns false

    val result = useCase.invoke()

    assertThat(result).isFalse()

    coVerify {
      channelValueRepository.setChannelsOffline()
    }
    confirmVerified(channelValueRepository)
  }
}
