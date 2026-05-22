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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity

class MarkChannelRelationsAsRemovableUseCaseTest {

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var channelRelationRepository: ChannelRelationRepository

  @InjectMockKs
  private lateinit var useCase: MarkChannelRelationsAsRemovableUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should mark as removable`() {
    // given
    val profileId = 123L
    val profile: ProfileEntity = mockk { every { id } returns profileId }

    every { profileRepository.findActiveProfile() } returns Single.just(profile)
    every { channelRelationRepository.markAsRemovable(profileId) } returns Completable.complete()

    // when
    val observer = useCase().test()

    // then
    observer.assertComplete()

    verify {
      profileRepository.findActiveProfile()
      channelRelationRepository.markAsRemovable(profileId)
    }
    confirmVerified(profileRepository, channelRelationRepository)
  }
}
