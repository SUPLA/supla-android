package org.supla.android.usecases.icon
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
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository

class FindIconsToDownloadUseCaseTest {

  @MockK
  private lateinit var channelGroupRepository: ChannelGroupRepository

  @MockK
  private lateinit var channelRepository: ChannelRepository

  @MockK
  private lateinit var sceneRepository: SceneRepository

  @InjectMockKs
  private lateinit var useCase: FindIconsToDownloadUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should return one icon from each repository`() = runTest {
    // given
    val profileId = 123L
    coEvery { channelGroupRepository.findIconIdsToDownload(profileId) } returns listOf(1)
    coEvery { channelRepository.findIconIdsToDownload(profileId) } returns listOf(2)
    coEvery { sceneRepository.findIconIdsToDownload(profileId) } returns listOf(3)

    // when
    val result = useCase(profileId)

    // then
    assertThat(result).containsExactly(1, 2, 3)
    coVerify {
      channelGroupRepository.findIconIdsToDownload(profileId)
      channelRepository.findIconIdsToDownload(profileId)
      sceneRepository.findIconIdsToDownload(profileId)
    }
    confirmVerified(channelGroupRepository, channelRepository, sceneRepository)
  }
}
