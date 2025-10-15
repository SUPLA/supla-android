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

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.AuthInfo
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager

@RunWith(MockitoJUnitRunner::class)
class SaveProfileUseCaseTest {
  @Mock
  private lateinit var profileManager: ProfileManager

  @Mock
  private lateinit var profileIdHolder: ProfileIdHolder

  @InjectMocks
  private lateinit var useCase: SaveProfileUseCase

  @Test
  fun `should create new profile`() {
    // given
    val profile = profileWithEmailMock()

    whenever(profileManager.create(profile)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(emptyList()))

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertComplete()

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)
    verifyNoMoreInteractions(profileManager)
    verifyNoInteractions(profileIdHolder)
  }

  @Test
  fun `should create new profile and put it to holder when it's first profile`() {
    // given
    val profile = profileWithEmailMock()
    profile.isActive = true

    doAnswer {
      profile.id = 123L
      Completable.complete()
    }.whenever(profileManager).create(profile)
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(emptyList()))

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertComplete()

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)

    verify(profileIdHolder).profileId = 123L

    verifyNoMoreInteractions(profileManager, profileIdHolder)
  }

  @Test
  fun `should update profile`() {
    // given
    val profile = profileWithEmailMock().apply { id = 123L }

    whenever(profileManager.update(profile)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(emptyList()))

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertComplete()

    verify(profileManager).getAllProfiles()
    verify(profileManager).update(profile)
    verifyNoMoreInteractions(profileManager)
    verifyNoInteractions(profileIdHolder)
  }

  @Test
  fun `should throw when name is empty`() {
    // given
    val profile = profileWithEmailMock().apply { name = "" }

    whenever(profileManager.create(profile)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(listOf(profileWithEmailMock())))

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertError(SaveProfileUseCase.SaveAccountException.EmptyName)

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)
    verifyNoMoreInteractions(profileManager)
    verifyNoInteractions(profileIdHolder)
  }

  @Test
  fun `should throw when name is duplicated`() {
    // given
    val profile = profileWithEmailMock()

    whenever(profileManager.create(profile)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(
      Observable.just(
        listOf(
          profileWithEmailMock().apply { id = 123 }
        )
      )
    )

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertError(SaveProfileUseCase.SaveAccountException.DuplicatedName)

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)
    verifyNoMoreInteractions(profileManager)
    verifyNoInteractions(profileIdHolder)
  }

  @Test
  fun `should throw when name is duplicated (trimming)`() {
    // given
    val profile = profileWithEmailMock()

    whenever(profileManager.create(profile)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(
      Observable.just(
        listOf(
          profileWithEmailMock().apply {
            id = 123
            name += " "
          }
        )
      )
    )

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertError(SaveProfileUseCase.SaveAccountException.DuplicatedName)

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)
    verifyNoMoreInteractions(profileManager)
    verifyNoInteractions(profileIdHolder)
  }

  @Test
  fun `should throw when auth data is not complete`() {
    // given
    val profile = profileWithEmailMock().apply { authInfo.emailAddress = "" }

    whenever(profileManager.create(profile)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(listOf(profileWithEmailMock())))

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertError(SaveProfileUseCase.SaveAccountException.DataIncomplete)

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)
    verifyNoMoreInteractions(profileManager)
    verifyNoInteractions(profileIdHolder)
  }

  private fun profileWithEmailMock(): AuthProfileItem {
    return AuthProfileItem(
      name = "test name",
      authInfo = AuthInfo(
        emailAuth = true,
        serverAutoDetect = true,
        serverForEmail = "",
        serverForAccessID = "",
        emailAddress = "test@supla.org",
        accessID = 0,
        accessIDpwd = ""
      ),
      advancedAuthSetup = false,
      isActive = false
    )
  }
}
