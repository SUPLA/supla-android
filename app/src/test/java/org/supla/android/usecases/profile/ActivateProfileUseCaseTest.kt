package org.supla.android.usecases.profile
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
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.networking.suplacloud.SuplaCloudConfigHolder
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.usecases.client.ReconnectUseCase
import org.supla.android.usecases.icon.LoadUserIconsIntoCacheUseCase

@RunWith(MockitoJUnitRunner::class)
class ActivateProfileUseCaseTest {

  @Mock
  private lateinit var profileRepository: RoomProfileRepository

  @Mock
  private lateinit var profileIdHolder: ProfileIdHolder

  @Mock
  private lateinit var suplaCloudConfigHolder: SuplaCloudConfigHolder

  @Mock
  private lateinit var loadUserIconsIntoCacheUseCase: LoadUserIconsIntoCacheUseCase

  @Mock
  private lateinit var reconnectUseCase: ReconnectUseCase

  @InjectMocks
  private lateinit var useCase: ActivateProfileUseCase

  @Test
  fun `should skip activation when profile active and force is false`() {
    // given
    val activeProfileId = 123L
    val activeProfile = mockk<ProfileEntity> {
      every { id } returns activeProfileId
      every { active } returns true
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(activeProfile))

    // when
    val testObserver = useCase.invoke(activeProfileId, false).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(profileRepository)
    verifyNoInteractions(profileIdHolder, suplaCloudConfigHolder)
  }

  @Test
  fun `should activate other profile`() {
    // given
    val activeProfileId = 123L
    val newActiveProfileId = 234L

    val activeProfile = mockk<ProfileEntity> {
      every { id } returns activeProfileId
      every { active } returns true
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(activeProfile))
    whenever(profileRepository.activateProfile(newActiveProfileId)).thenReturn(Completable.complete())

    whenever(loadUserIconsIntoCacheUseCase.invoke()).thenReturn(Completable.complete())
    whenever(reconnectUseCase.invoke()).thenReturn(Completable.complete())

    // when
    val testObserver = useCase.invoke(newActiveProfileId, false).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).findActiveProfile()
    verify(profileRepository).activateProfile(newActiveProfileId)
    verify(profileIdHolder).profileId = newActiveProfileId
    verify(suplaCloudConfigHolder).clean()
    verify(reconnectUseCase).invoke()
    verifyNoMoreInteractions(profileRepository, profileIdHolder, suplaCloudConfigHolder, reconnectUseCase)
  }

  @Test
  fun `should reactivate same profile with force`() {
    // given
    val activeProfileId = 123L
    val activeProfile = mockk<ProfileEntity> {
      every { id } returns activeProfileId
      every { active } returns true
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(activeProfile))
    whenever(profileRepository.activateProfile(activeProfileId)).thenReturn(Completable.complete())

    whenever(loadUserIconsIntoCacheUseCase.invoke()).thenReturn(Completable.complete())
    whenever(reconnectUseCase.invoke()).thenReturn(Completable.complete())

    // when
    val testObserver = useCase.invoke(activeProfileId, true).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).findActiveProfile()
    verify(profileRepository).activateProfile(activeProfileId)
    verify(profileIdHolder).profileId = activeProfileId
    verify(suplaCloudConfigHolder).clean()
    verify(reconnectUseCase).invoke()
    verifyNoMoreInteractions(profileRepository, profileIdHolder, suplaCloudConfigHolder, reconnectUseCase)
  }

  @Test
  fun `should activate profile even if no active profile found`() {
    // given
    val activeProfileId = 123L
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.error(EmptyResultSetException("")))
    whenever(profileRepository.activateProfile(activeProfileId)).thenReturn(Completable.complete())

    whenever(loadUserIconsIntoCacheUseCase.invoke()).thenReturn(Completable.complete())
    whenever(reconnectUseCase.invoke()).thenReturn(Completable.complete())

    // when
    val testObserver = useCase.invoke(activeProfileId, true).test()

    // then
    testObserver.assertComplete()

    verify(profileRepository).findActiveProfile()
    verify(profileRepository).activateProfile(activeProfileId)
    verify(profileIdHolder).profileId = activeProfileId
    verify(suplaCloudConfigHolder).clean()
    verify(reconnectUseCase).invoke()
    verifyNoMoreInteractions(profileRepository, profileIdHolder, suplaCloudConfigHolder, reconnectUseCase)
  }

  @Test
  fun `should not activate profile when other error occurs`() {
    // given
    val activeProfileId = 123L
    val error = IllegalStateException()
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.error(error))

    // when
    val testObserver = useCase.invoke(activeProfileId, true).test()

    // then
    testObserver.assertError(error)

    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(profileRepository, profileIdHolder, suplaCloudConfigHolder)
  }
}
