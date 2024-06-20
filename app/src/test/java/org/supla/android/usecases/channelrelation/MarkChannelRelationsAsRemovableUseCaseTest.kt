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

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.profile.ProfileManager
import org.supla.android.testhelpers.profileMock

@RunWith(MockitoJUnitRunner::class)
class MarkChannelRelationsAsRemovableUseCaseTest {

  @Mock
  lateinit var profileManager: ProfileManager

  @Mock
  lateinit var channelRelationRepository: ChannelRelationRepository

  @InjectMocks
  lateinit var useCase: MarkChannelRelationsAsRemovableUseCase

  @Test
  fun `should mark as removable`() {
    // given
    val profileId = 123L
    val profile = profileMock(profileId)

    whenever(profileManager.getCurrentProfile()).thenReturn(Maybe.just(profile))
    whenever(channelRelationRepository.markAsRemovable(profileId)).thenReturn(Completable.complete())

    // when
    val observer = useCase().test()

    // then
    observer.assertComplete()
    verify(profileManager).getCurrentProfile()
    verify(channelRelationRepository).markAsRemovable(profileId)
    verifyNoMoreInteractions(profileManager, channelRelationRepository)
  }
}
