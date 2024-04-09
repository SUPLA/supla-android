package org.supla.android.features.createaccount

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.Preferences
import org.supla.android.core.BaseViewModelTest
import org.supla.android.db.AuthProfileItem
import org.supla.android.features.deleteaccountweb.DeleteAccountWebFragment
import org.supla.android.profile.AuthInfo
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.profile.DeleteProfileUseCase
import org.supla.android.usecases.profile.SaveProfileUseCase

@RunWith(MockitoJUnitRunner::class)
class CreateAccountViewModelTest : BaseViewModelTest<CreateAccountViewState, CreateAccountViewEvent, CreateAccountViewModel>() {

  @get:Rule
  var instantTaskExecutorRule = InstantTaskExecutorRule()

  @Mock
  private lateinit var profileManager: ProfileManager

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @Mock
  private lateinit var saveProfileUseCase: SaveProfileUseCase

  @Mock
  private lateinit var deleteProfileUseCase: DeleteProfileUseCase

  @InjectMocks
  override lateinit var viewModel: CreateAccountViewModel

  @Before
  override fun setUp() {
    super.setUp()
    whenever(schedulers.io).thenReturn(Schedulers.trampoline())
    whenever(schedulers.ui).thenReturn(Schedulers.trampoline())
  }

  @Test
  fun `should only update state when creating new profile`() {
    // given
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)

    // when
    viewModel.loadProfile(null)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(profileNameVisible = true)
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should load profile and update state according to profile`() {
    // given
    val profileId = 123L
    val profile = profileMock()
    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))

    // when
    viewModel.loadProfile(profileId)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(
        profileNameVisible = false,
        deleteButtonVisible = false,
        advancedMode = profile.advancedAuthSetup,
        accountName = profile.name,
        emailAddress = profile.authInfo.emailAddress,
        authorizeByEmail = profile.authInfo.emailAuth,
        autoServerAddress = profile.authInfo.serverAutoDetect,
        emailAddressServer = profile.authInfo.serverForEmail,
        accessIdentifier = profile.authInfo.accessID.toString(),
        accessIdentifierPassword = profile.authInfo.accessIDpwd,
        accessIdentifierServer = profile.authInfo.serverForAccessID
      )
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should change mode to advanced and back to basic`() {
    // when
    viewModel.changeMode(advancedMode = true)
    viewModel.changeMode(advancedMode = false)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(advancedMode = true),
      CreateAccountViewState()
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should not change mode to basic, when authorize by id`() {
    // given
    viewModel.changeMode(advancedMode = true)
    viewModel.changeAuthorizeByEmail(false)

    // when
    viewModel.changeMode(advancedMode = false)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(advancedMode = true),
      CreateAccountViewState(advancedMode = true, authorizeByEmail = false),
      CreateAccountViewState(advancedMode = false, authorizeByEmail = false),
      CreateAccountViewState(advancedMode = true, authorizeByEmail = false)
    )
    assertThat(events).containsExactly(CreateAccountViewEvent.ShowBasicModeUnavailableDialog)
  }

  @Test
  fun `should not change mode to basic, when server automatic detection disabled`() {
    // given
    viewModel.changeMode(advancedMode = true)
    viewModel.toggleServerAutoDiscovery()

    // when
    viewModel.changeMode(advancedMode = false)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(advancedMode = true),
      CreateAccountViewState(advancedMode = true, autoServerAddress = false),
      CreateAccountViewState(advancedMode = false, autoServerAddress = false),
      CreateAccountViewState(advancedMode = true, autoServerAddress = false)
    )
    assertThat(events).containsExactly(CreateAccountViewEvent.ShowBasicModeUnavailableDialog)
  }

  @Test
  fun `should change profile name`() {
    // given
    val newName = "new name"

    // when
    viewModel.changeProfileName(newName)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(accountName = newName)
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should change profile email`() {
    // given
    val email = "new email"

    // when
    viewModel.changeEmail(email)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(emailAddress = email)
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should change authorization type`() {
    // when
    viewModel.changeAuthorizeByEmail(false)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(authorizeByEmail = false)
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should change email server address`() {
    // given
    val address = "some.supla.org"

    // when
    viewModel.changeEmailAddressServer(address)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(emailAddressServer = address)
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should change access identifier`() {
    // given
    val id = "12334"

    // when
    viewModel.changeAccessIdentifier(id)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(accessIdentifier = id)
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should change access identifier password`() {
    // given
    val password = "pass"

    // when
    viewModel.changeAccessIdentifierPassword(password)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(accessIdentifierPassword = password)
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should change access identifier server`() {
    // given
    val server = "some-server.supla.org"

    // when
    viewModel.changeAccessIdentifierServer(server)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(accessIdentifierServer = server)
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should navigate to create account`() {
    // when
    viewModel.createAccount()

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      CreateAccountViewEvent.NavigateToCreateAccount
    )
  }

  @Test
  fun `should update server address when auto discovery gets disabled`() {
    // given
    viewModel.changeEmail("some@test.supla.org")

    // when
    viewModel.toggleServerAutoDiscovery()

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(emailAddress = "some@test.supla.org"),
      CreateAccountViewState(
        emailAddress = "some@test.supla.org",
        autoServerAddress = false,
        emailAddressServer = "test.supla.org"
      )
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should clean server address when discovery gets enabled`() {
    // given
    viewModel.toggleServerAutoDiscovery()
    viewModel.changeEmailAddressServer("test.supla.org")

    // when
    viewModel.toggleServerAutoDiscovery()

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(autoServerAddress = false),
      CreateAccountViewState(autoServerAddress = false, emailAddressServer = "test.supla.org"),
      CreateAccountViewState(autoServerAddress = true)
    )
    assertThat(events).isEmpty()
  }

  @Test
  fun `should save new profile as active when no other profile exists`() {
    // given
    whenever(saveProfileUseCase(any())).thenReturn(Completable.complete())

    val defaultName = "default profile"
    val email = "test@supla.org"
    viewModel.loadProfile(null)
    viewModel.changeEmail(email)

    // when
    viewModel.saveProfile(null, defaultName)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(emailAddress = email)
    )
    assertThat(events).containsExactly(
      CreateAccountViewEvent.Reconnect
    )

    verify(saveProfileUseCase, times(1)).invoke(
      argThat { profile ->
        profile.isActive && profile.name == defaultName && profile.authInfo.emailAuth &&
          profile.authInfo.emailAddress == email && profile.advancedAuthSetup.not()
      }
    )
  }

  @Test
  fun `should save new profile as inactive when other profile exist`() {
    // given
    whenever(saveProfileUseCase(any())).thenReturn(Completable.complete())
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)

    val defaultName = "default profile"
    val email = "test@supla.org"
    viewModel.loadProfile(null)
    viewModel.changeEmail(email)

    // when
    viewModel.saveProfile(null, defaultName)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(profileNameVisible = true),
      CreateAccountViewState(profileNameVisible = true, emailAddress = email)
    )
    assertThat(events).containsExactly(
      CreateAccountViewEvent.Close
    )
    verify(saveProfileUseCase, times(1)).invoke(
      argThat { profile ->
        profile.isActive.not() && profile.name.isEmpty() && profile.authInfo.emailAuth &&
          profile.authInfo.emailAddress == email && profile.advancedAuthSetup.not()
      }
    )
  }

  @Test
  fun `should update profile without reconnect when no authorized data changed`() {
    // given
    whenever(saveProfileUseCase(any())).thenReturn(Completable.complete())
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)

    val profileId = 123L
    val profile = profileWithEmailMock()
    val newName = "new name"
    val originalName = profile.name
    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))

    // when
    viewModel.loadProfile(profileId)
    viewModel.changeProfileName(newName)
    viewModel.saveProfile(profileId, "default name")

    // then
    val state = CreateAccountViewState(profileNameVisible = true, deleteButtonVisible = true)
    assertThat(states).containsExactly(
      state,
      state.copy(emailAddress = profile.authInfo.emailAddress, accountName = originalName),
      state.copy(emailAddress = profile.authInfo.emailAddress, accountName = newName)
    )
    assertThat(events).containsExactly(
      CreateAccountViewEvent.Close
    )
    verify(saveProfileUseCase, times(1)).invoke(
      argThat { arg ->
        arg.isActive.not() && arg.authInfo.emailAuth &&
          arg.name == newName && arg.advancedAuthSetup.not()
      }
    )
  }

  @Test
  fun `should update profile with reconnect when email changed`() {
    // given
    whenever(saveProfileUseCase(any())).thenReturn(Completable.complete())
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)

    val profileId = 123L
    val profile = profileWithEmailMock()
    val newEmail = "other@supla.org"
    val originalEmail = profile.authInfo.emailAddress
    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))

    // when
    viewModel.loadProfile(profileId)
    viewModel.changeEmail(newEmail)
    viewModel.saveProfile(profileId, "default name")

    // then
    val state = CreateAccountViewState(profileNameVisible = true, deleteButtonVisible = true)
    assertThat(states).containsExactly(
      state,
      state.copy(emailAddress = originalEmail, accountName = profile.name),
      state.copy(emailAddress = newEmail, accountName = profile.name)
    )
    assertThat(events).containsExactly(
      CreateAccountViewEvent.Reconnect
    )
    verify(saveProfileUseCase, times(1)).invoke(
      argThat { arg ->
        arg.isActive.not() && arg.authInfo.emailAuth &&
          arg.authInfo.emailAddress == newEmail && arg.advancedAuthSetup.not()
      }
    )
  }

  @Test
  fun `should update profile with reconnect when server for email changed`() {
    // given
    val profile = profileWithEmailMock()

    // when & then
    testAuthorizationDataChange(profile) {
      viewModel.changeEmailAddressServer("test")
    }
  }

  @Test
  fun `should update profile with reconnect when server for access ID changed`() {
    // given
    val profile = profileWithEmailMock()

    // when & then
    testAuthorizationDataChange(profile) {
      viewModel.changeAccessIdentifierServer("test")
    }
  }

  @Test
  fun `should update profile with reconnect when access ID password changed`() {
    // given
    val profile = profileWithEmailMock()

    // when & then
    testAuthorizationDataChange(profile) {
      viewModel.changeAccessIdentifierPassword("test")
    }
  }

  @Test
  fun `should update profile with reconnect when access ID changed`() {
    // given
    val profile = profileWithEmailMock()

    // when & then
    testAuthorizationDataChange(profile) {
      viewModel.changeAccessIdentifier("1234")
    }
  }

  @Test
  fun `should update profile with reconnect when server auto detection changed`() {
    // given
    val profile = profileWithEmailMock()

    // when & then
    testAuthorizationDataChange(profile) {
      viewModel.toggleServerAutoDiscovery()
    }
  }

  @Test
  fun `should update profile with reconnect when authentication type changed`() {
    // given
    val profile = profileWithEmailMock()

    // when & then
    testAuthorizationDataChange(profile) {
      viewModel.changeAuthorizeByEmail(false)
    }
  }

  private fun testAuthorizationDataChange(profile: AuthProfileItem, action: (CreateAccountViewModel) -> Unit) {
    // given
    whenever(saveProfileUseCase(any())).thenReturn(Completable.complete())
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)

    val profileId = 123L
    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))

    // when
    viewModel.loadProfile(profileId)
    action(viewModel)
    viewModel.saveProfile(profileId, "default name")

    // then
    assertThat(states).hasSize(3)
    assertThat(events).containsExactly(
      CreateAccountViewEvent.Reconnect
    )
    verify(saveProfileUseCase, times(1)).invoke(profile)
  }

  @Test
  fun `should show empty name dialog`() {
    testSaveFailure(
      Completable.error(SaveProfileUseCase.SaveAccountException.EmptyName),
      CreateAccountViewEvent.ShowEmptyNameDialog
    )
  }

  @Test
  fun `should show duplicated name dialog`() {
    testSaveFailure(
      Completable.error(SaveProfileUseCase.SaveAccountException.DuplicatedName),
      CreateAccountViewEvent.ShowDuplicatedNameDialog
    )
  }

  @Test
  fun `should show incomplete data dialog`() {
    testSaveFailure(
      Completable.error(SaveProfileUseCase.SaveAccountException.DataIncomplete),
      CreateAccountViewEvent.ShowRequiredDataMissingDialog
    )
  }

  @Test
  fun `should show unknown error dialog`() {
    testSaveFailure(
      Completable.error(Exception()),
      CreateAccountViewEvent.ShowUnknownErrorDialog
    )
  }

  private fun testSaveFailure(saveResult: Completable, expectedEvent: CreateAccountViewEvent) {
    // given
    whenever(saveProfileUseCase(any())).thenReturn(saveResult)
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)

    val profileId = 123L
    val profile = profileWithEmailMock()
    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))

    // when
    viewModel.loadProfile(profileId)
    viewModel.saveProfile(profileId, "default name")

    // then
    val state = CreateAccountViewState(profileNameVisible = true, deleteButtonVisible = true)
    assertThat(states).containsExactly(
      state,
      state.copy(emailAddress = profile.authInfo.emailAddress, accountName = profile.name)
    )
    assertThat(events).containsExactly(expectedEvent)
    verify(saveProfileUseCase, times(1)).invoke(profile)
  }

  @Test
  fun `should delete profile and close fragment`() {
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)
    localDeleteTest(profileWithEmailMock(), CreateAccountViewEvent.Close)
  }

  @Test
  fun `should delete profile and reconnect`() {
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)
    val profile = profileWithEmailMock().apply { isActive = true }
    localDeleteTest(profile, CreateAccountViewEvent.Reconnect)
  }

  @Test
  fun `should delete profile and restart`() {
    val profile = profileWithEmailMock().apply { isActive = true }
    localDeleteTest(profile, CreateAccountViewEvent.RestartFlow)
  }

  private fun localDeleteTest(profile: AuthProfileItem, event: CreateAccountViewEvent) {
    // given
    val profileId = 123L
    profile.id = profileId
    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))
    whenever(deleteProfileUseCase(profileId)).thenReturn(Completable.complete())

    // when
    viewModel.deleteProfile(profileId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(event)
    verify(deleteProfileUseCase, times(1)).invoke(profileId)
  }

  @Test
  fun `should delete profile and navigate to web removal`() {
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)
    val serverAddress = "beta-cloud.supla.org"
    localAndWebDeleteTest(
      profileWithEmailMock().apply { authInfo.serverForEmail = serverAddress },
      CreateAccountViewEvent.NavigateToWebRemoval(
        serverAddress,
        DeleteAccountWebFragment.EndDestination.CLOSE
      )
    )
  }

  @Test
  fun `should delete profile and navigate to web removal with reconnect`() {
    whenever(preferences.isAnyAccountRegistered).thenReturn(true)
    localAndWebDeleteTest(
      profileWithEmailMock().apply { isActive = true },
      CreateAccountViewEvent.NavigateToWebRemoval(
        serverAddress = "",
        DeleteAccountWebFragment.EndDestination.RECONNECT
      )
    )
  }

  @Test
  fun `should delete profile and navigate to web removal with restart`() {
    localAndWebDeleteTest(
      profileWithEmailMock().apply { isActive = true },
      CreateAccountViewEvent.NavigateToWebRemoval(
        serverAddress = "",
        DeleteAccountWebFragment.EndDestination.RESTART
      )
    )
  }

  private fun localAndWebDeleteTest(profile: AuthProfileItem, event: CreateAccountViewEvent) {
    // given
    val profileId = 123L
    profile.id = profileId
    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))
    whenever(deleteProfileUseCase(profileId)).thenReturn(Completable.complete())

    // when
    viewModel.deleteProfileWithCloud(profileId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(event)
    verify(deleteProfileUseCase, times(1)).invoke(profileId)
  }

  private fun profileMock(): AuthProfileItem {
    return AuthProfileItem(
      name = "test name",
      authInfo = AuthInfo(
        emailAuth = false,
        serverAutoDetect = true,
        serverForEmail = "test.supla.org",
        serverForAccessID = "another-test.supla.org",
        emailAddress = "test@supla.org",
        accessID = 12345,
        accessIDpwd = "Test password"
      ),
      advancedAuthSetup = true,
      isActive = true
    )
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
