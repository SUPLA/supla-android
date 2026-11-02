package org.supla.android.usecases.channelrelation
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
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.usecases.channel.VisibilityChange

class SetChannelGroupRelationsVisibleUseCaseTest {

  @MockK
  private lateinit var channelGroupRelationRepository: ChannelGroupRelationRepository

  @InjectMockKs
  private lateinit var useCase: SetChannelGroupRelationsVisibleUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should set channel group relations visible`() {
    coEvery { channelGroupRelationRepository.setChannelGroupRelationsVisible(VisibilityChange.VISIBLE_TO_PROCESSING) } returns true

    val result = useCase(VisibilityChange.VISIBLE_TO_PROCESSING)

    assertThat(result).isTrue()
    coVerify { channelGroupRelationRepository.setChannelGroupRelationsVisible(VisibilityChange.VISIBLE_TO_PROCESSING) }
  }

  @Test
  fun `should return false when repository update fails`() {
    coEvery { channelGroupRelationRepository.setChannelGroupRelationsVisible(VisibilityChange.PROCESSING_TO_HIDE) } returns false

    val result = useCase(VisibilityChange.PROCESSING_TO_HIDE)

    assertThat(result).isFalse()
    coVerify { channelGroupRelationRepository.setChannelGroupRelationsVisible(VisibilityChange.PROCESSING_TO_HIDE) }
  }
}
