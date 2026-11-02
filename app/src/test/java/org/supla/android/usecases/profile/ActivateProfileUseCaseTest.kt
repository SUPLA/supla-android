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
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.supla.android.core.networking.suplacloud.SuplaCloudConfigHolder
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.usecases.client.ReconnectUseCase
import org.supla.android.usecases.icon.LoadUserIconsIntoCacheUseCase

class ActivateProfileUseCaseTest {

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var suplaCloudConfigHolder: SuplaCloudConfigHolder

  @MockK
  private lateinit var loadUserIconsIntoCacheUseCase: LoadUserIconsIntoCacheUseCase

  @MockK
  private lateinit var reconnectUseCase: ReconnectUseCase

  @InjectMockKs
  private lateinit var useCase: ActivateProfileUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should skip activation when profile active and force is false`() {
    // given
    val activeProfileId = 123L
    val activeProfile = mockk<ProfileEntity> {
      every { id } returns activeProfileId
      every { active } returns true
    }
    every { profileRepository.findActiveProfile() } returns Single.just(activeProfile)

    // when
    val testObserver = useCase.invoke(activeProfileId, false).test()

    // then
    testObserver.assertComplete()

    verify { profileRepository.findActiveProfile() }
    confirmVerified(profileRepository)
    verify {
      suplaCloudConfigHolder wasNot Called
    }
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
    every { profileRepository.findActiveProfile() } returns Single.just(activeProfile)
    every { profileRepository.activateProfile(newActiveProfileId) } returns Completable.complete()

    every { loadUserIconsIntoCacheUseCase.invoke() } returns Completable.complete()
    every { reconnectUseCase.invoke() } returns Completable.complete()
    every { suplaCloudConfigHolder.clean() } just Runs

    // when
    val testObserver = useCase.invoke(newActiveProfileId, false).test()

    // then
    testObserver.assertComplete()

    verify { profileRepository.findActiveProfile() }
    verify { profileRepository.activateProfile(newActiveProfileId) }
    verify { suplaCloudConfigHolder.clean() }
    verify { reconnectUseCase.invoke() }
    confirmVerified(profileRepository, suplaCloudConfigHolder, reconnectUseCase)
  }

  @Test
  fun `should reactivate same profile with force`() {
    // given
    val activeProfileId = 123L
    val activeProfile = mockk<ProfileEntity> {
      every { id } returns activeProfileId
      every { active } returns true
    }
    every { profileRepository.findActiveProfile() } returns Single.just(activeProfile)
    every { profileRepository.activateProfile(activeProfileId) } returns Completable.complete()

    every { loadUserIconsIntoCacheUseCase.invoke() } returns Completable.complete()
    every { reconnectUseCase.invoke() } returns Completable.complete()
    every { suplaCloudConfigHolder.clean() } just Runs

    // when
    val testObserver = useCase.invoke(activeProfileId, true).test()

    // then
    testObserver.assertComplete()

    verify { profileRepository.findActiveProfile() }
    verify { profileRepository.activateProfile(activeProfileId) }
    verify { suplaCloudConfigHolder.clean() }
    verify { reconnectUseCase.invoke() }
    confirmVerified(profileRepository, suplaCloudConfigHolder, reconnectUseCase)
  }

  @Test
  fun `should activate profile even if no active profile found`() {
    // given
    val activeProfileId = 123L
    every { profileRepository.findActiveProfile() } returns Single.error(EmptyResultSetException(""))
    every { profileRepository.activateProfile(activeProfileId) } returns Completable.complete()

    every { loadUserIconsIntoCacheUseCase.invoke() } returns Completable.complete()
    every { reconnectUseCase.invoke() } returns Completable.complete()
    every { suplaCloudConfigHolder.clean() } just Runs

    // when
    val testObserver = useCase.invoke(activeProfileId, true).test()

    // then
    testObserver.assertComplete()

    verify { profileRepository.findActiveProfile() }
    verify { profileRepository.activateProfile(activeProfileId) }
    verify { suplaCloudConfigHolder.clean() }
    verify { reconnectUseCase.invoke() }
    confirmVerified(profileRepository, suplaCloudConfigHolder, reconnectUseCase)
  }

  @Test
  fun `should not activate profile when other error occurs`() {
    // given
    val activeProfileId = 123L
    val error = IllegalStateException()
    every { profileRepository.findActiveProfile() } returns Single.error(error)

    // when
    val testObserver = useCase.invoke(activeProfileId, true).test()

    // then
    testObserver.assertError(error)

    verify { profileRepository.findActiveProfile() }
    confirmVerified(profileRepository, suplaCloudConfigHolder)
  }
}
