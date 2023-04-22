package org.supla.android.usecases.account

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.Preferences
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.AuthInfo
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager

@RunWith(MockitoJUnitRunner::class)
class SaveAccountUseCaseTest {
  @Mock
  private lateinit var profileManager: ProfileManager

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  private lateinit var profileIdHolder: ProfileIdHolder

  @InjectMocks
  private lateinit var useCase: SaveAccountUseCase

  @Test
  fun `should create new profile`() {
    // given
    val profile = profileWithEmailMock()

    whenever(preferences.isAnyAccountRegistered).thenReturn(true)
    whenever(profileManager.create(profile)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(emptyList()))

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertComplete()

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)
    verifyNoMoreInteractions(profileManager)
    verifyZeroInteractions(profileIdHolder)
  }

  @Test
  fun `should create new profile and put it to holder when it's first profile`() {
    // given
    val profile = profileWithEmailMock()

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

    whenever(preferences.isAnyAccountRegistered).thenReturn(true)
    whenever(profileManager.update(profile)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(emptyList()))

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertComplete()

    verify(profileManager).getAllProfiles()
    verify(profileManager).update(profile)
    verifyNoMoreInteractions(profileManager)
    verifyZeroInteractions(profileIdHolder)
  }

  @Test
  fun `should throw when name is empty`() {
    // given
    val profile = profileWithEmailMock().apply { name = "" }

    whenever(preferences.isAnyAccountRegistered).thenReturn(true)
    whenever(profileManager.create(profile)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(listOf(profileWithEmailMock())))

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertError(SaveAccountUseCase.SaveAccountException.EmptyName)

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)
    verifyNoMoreInteractions(profileManager)
    verifyZeroInteractions(profileIdHolder)
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
    testObserver.assertError(SaveAccountUseCase.SaveAccountException.DuplicatedName)

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)
    verifyNoMoreInteractions(profileManager)
    verifyZeroInteractions(profileIdHolder)
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
    testObserver.assertError(SaveAccountUseCase.SaveAccountException.DataIncomplete)

    verify(profileManager).getAllProfiles()
    verify(profileManager).create(profile)
    verifyNoMoreInteractions(profileManager)
    verifyZeroInteractions(profileIdHolder)
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
      isActive = false,
    )
  }
}