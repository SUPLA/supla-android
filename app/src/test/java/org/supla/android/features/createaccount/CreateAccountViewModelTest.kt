package org.supla.android.features.createaccount

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.features.deleteaccountweb.DeleteAccountWebFragment
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.client.ReconnectUseCase
import org.supla.android.usecases.profile.DeleteProfileUseCase
import org.supla.android.usecases.profile.SaveProfileUseCase

@RunWith(MockitoJUnitRunner::class)
class CreateAccountViewModelTest : BaseViewModelTest<CreateAccountViewState, CreateAccountViewEvent, CreateAccountViewModel>(
  MockSchedulers.MOCKK
) {

  @get:Rule
  var instantTaskExecutorRule = InstantTaskExecutorRule()

  @MockK
  private lateinit var saveProfileUseCase: SaveProfileUseCase

  @MockK
  private lateinit var deleteProfileUseCase: DeleteProfileUseCase

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var reconnectUseCase: ReconnectUseCase

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: CreateAccountViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should only update state when creating new profile`() {
    // given
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(mockk()))
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
    every { profileRepository.findProfile(profileId) } returns Single.just(profile)
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf())

    // when
    viewModel.loadProfile(profileId)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(
        profileNameVisible = false,
        deleteButtonVisible = false,
        advancedMode = profile.advancedMode!!,
        accountName = profile.name,
        emailAddress = profile.email.orEmpty(),
        authorizeByEmail = profile.emailAuth,
        autoServerAddress = profile.serverAutoDetect,
        emailAddressServer = profile.serverForEmail.orEmpty(),
        accessIdentifier = profile.accessId.toString(),
        accessIdentifierPassword = profile.accessIdPassword.orEmpty(),
        accessIdentifierServer = profile.serverForAccessId.orEmpty()
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
  fun `should save new profile without reconnect`() {
    // given
    val profileSlot = slot<ProfileEntity>()
    every { saveProfileUseCase(profile = capture(profileSlot)) } returns Single.just(SaveProfileUseCase.Result(1, false))
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf())

    val email = "test@supla.org"
    viewModel.loadProfile(null)
    viewModel.changeEmail(email)

    // when
    viewModel.saveProfile(null)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(emailAddress = email)
    )
    assertThat(events).containsExactly(
      CreateAccountViewEvent.Close
    )

    verify {
      saveProfileUseCase(any())
    }
    assertThat(profileSlot.captured.active).isFalse
    assertThat(profileSlot.captured.name).isEmpty()
    assertThat(profileSlot.captured.emailAuth).isTrue
    assertThat(profileSlot.captured.email).isEqualTo(email)
    assertThat(profileSlot.captured.advancedMode).isFalse
  }

  @Test
  fun `should save new profile with reconnect`() {
    // given
    val profileSlot = slot<ProfileEntity>()
    every { saveProfileUseCase(profile = capture(profileSlot)) } returns Single.just(SaveProfileUseCase.Result(1, true))
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(mockk()))
    every { reconnectUseCase() } returns Completable.complete()

    val email = "test@supla.org"
    viewModel.loadProfile(null)
    viewModel.changeEmail(email)

    // when
    viewModel.saveProfile(null)

    // then
    assertThat(states).containsExactly(
      CreateAccountViewState(profileNameVisible = true),
      CreateAccountViewState(profileNameVisible = true, emailAddress = email)
    )
    assertThat(events).containsExactly(
      CreateAccountViewEvent.Close
    )
    verify {
      saveProfileUseCase.invoke(any())
    }
    assertThat(profileSlot.captured.active).isFalse
    assertThat(profileSlot.captured.name).isEmpty()
    assertThat(profileSlot.captured.emailAuth).isTrue
    assertThat(profileSlot.captured.email).isEqualTo(email)
    assertThat(profileSlot.captured.advancedMode).isFalse
  }

  @Test
  fun `should update profile without reconnect`() {
    // given
    val profileSlot = slot<ProfileEntity>()
    every { saveProfileUseCase(profile = capture(profileSlot)) } returns Single.just(SaveProfileUseCase.Result(1, false))
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(mockk()))

    val profileId = 123L
    val profile = profileWithEmailMock()
    val newName = "new name"
    val originalName = profile.name
    every { profileRepository.findProfile(profileId) } returns Single.just(profile)

    // when
    viewModel.loadProfile(profileId)
    viewModel.changeProfileName(newName)
    viewModel.saveProfile(profileId)

    // then
    val state = CreateAccountViewState(profileNameVisible = true, deleteButtonVisible = true)
    assertThat(states).containsExactly(
      state,
      state.copy(emailAddress = profile.email.orEmpty(), accountName = originalName),
      state.copy(emailAddress = profile.email.orEmpty(), accountName = newName)
    )
    assertThat(events).containsExactly(
      CreateAccountViewEvent.Close
    )

    verify {
      saveProfileUseCase.invoke(any())
    }
    assertThat(profileSlot.captured.active).isFalse
    assertThat(profileSlot.captured.name).isEqualTo(newName)
    assertThat(profileSlot.captured.emailAuth).isTrue
    assertThat(profileSlot.captured.advancedMode).isFalse
  }

  @Test
  fun `should show empty name dialog`() {
    testSaveFailure(
      Single.error(SaveProfileUseCase.SaveAccountException.EmptyName()),
      CreateAccountViewEvent.ShowEmptyNameDialog
    )
  }

  @Test
  fun `should show duplicated name dialog`() {
    testSaveFailure(
      Single.error(SaveProfileUseCase.SaveAccountException.DuplicatedName()),
      CreateAccountViewEvent.ShowDuplicatedNameDialog
    )
  }

  @Test
  fun `should show incomplete data dialog`() {
    testSaveFailure(
      Single.error(SaveProfileUseCase.SaveAccountException.DataIncomplete()),
      CreateAccountViewEvent.ShowRequiredDataMissingDialog
    )
  }

  @Test
  fun `should show unknown error dialog`() {
    testSaveFailure(
      Single.error(Exception()),
      CreateAccountViewEvent.ShowUnknownErrorDialog
    )
  }

  private fun testSaveFailure(saveResult: Single<SaveProfileUseCase.Result>, expectedEvent: CreateAccountViewEvent) {
    // given
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(mockk()))

    val profileId = 123L
    val profile = profileWithEmailMock()
    every { profileRepository.findProfile(profileId) } returns Single.just(profile)
    every { saveProfileUseCase(profile) } returns saveResult

    // when
    viewModel.loadProfile(profileId)
    viewModel.saveProfile(profileId)

    // then
    val state = CreateAccountViewState(profileNameVisible = true, deleteButtonVisible = true)
    assertThat(states).containsExactly(
      state,
      state.copy(emailAddress = profile.email.orEmpty(), accountName = profile.name)
    )
    assertThat(events).containsExactly(expectedEvent)
    verify {
      saveProfileUseCase(profile)
    }
  }

  @Test
  fun `should delete profile and close fragment`() {
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(mockk()))
    localDeleteTest(profileWithEmailMock(), CreateAccountViewEvent.Close)
  }

  @Test
  fun `should delete profile and restart`() {
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf())
    localDeleteTest(profileWithEmailMock(), CreateAccountViewEvent.RestartFlow)
  }

  private fun localDeleteTest(profile: ProfileEntity, event: CreateAccountViewEvent) {
    // given
    val profileId = 123L
    val profileWithId = profile.copy(id = profileId)
    every { profileRepository.findProfile(profileId) } returns Single.just(profileWithId)
    every { deleteProfileUseCase(profileWithId) } returns Completable.complete()

    // when
    viewModel.deleteProfile(profileId)

    // then
    assertThat(states).hasSize(2) // because of loading flag change
    assertThat(events).containsExactly(event)

    verify { deleteProfileUseCase(profileWithId) }
  }

  @Test
  fun `should delete profile and navigate to web removal`() {
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(mockk()))
    val serverAddress = "beta-cloud.supla.org"
    localAndWebDeleteTest(
      profileWithEmailMock(serverForEmail = serverAddress),
      CreateAccountViewEvent.NavigateToWebRemoval(
        serverAddress,
        DeleteAccountWebFragment.EndDestination.CLOSE
      )
    )
  }

  @Test
  fun `should delete profile and navigate to web removal with restart`() {
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf())
    localAndWebDeleteTest(
      profileWithEmailMock(),
      CreateAccountViewEvent.NavigateToWebRemoval(
        serverAddress = "",
        DeleteAccountWebFragment.EndDestination.RESTART
      )
    )
  }

  private fun localAndWebDeleteTest(profile: ProfileEntity, event: CreateAccountViewEvent) {
    // given
    val profileId = 123L
    val profileWithId = profile.copy(id = profileId)
    every { profileRepository.findProfile(profileId) } returns Single.just(profileWithId)
    every { deleteProfileUseCase(profileWithId) } returns Completable.complete()

    // when
    viewModel.deleteProfileWithCloud(profileId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(event)
    verify { deleteProfileUseCase(profileWithId) }
  }

  private fun profileMock(
    id: Long? = null,
    name: String = "test name",
    email: String = "test@supla.org",
    active: Boolean = false
  ): ProfileEntity =
    ProfileEntity(
      id = id,
      name = name,
      email = email,
      serverForAccessId = "another-test.supla.org",
      serverForEmail = "test.supla.org",
      serverAutoDetect = true,
      emailAuth = false,
      accessId = 12345,
      accessIdPassword = "Test password",
      preferredProtocolVersion = 0,
      active = active,
      advancedMode = true,
      position = 0,
      guid = byteArrayOf(),
      authKey = byteArrayOf()
    )

  private fun profileWithEmailMock(
    id: Long? = null,
    name: String = "test name",
    email: String = "test@supla.org",
    serverForEmail: String = "",
    active: Boolean = false
  ) =
    ProfileEntity(
      id = id,
      name = name,
      email = email,
      serverForAccessId = "",
      serverForEmail = serverForEmail,
      serverAutoDetect = true,
      emailAuth = true,
      accessId = 0,
      accessIdPassword = "",
      preferredProtocolVersion = 0,
      active = active,
      advancedMode = false,
      position = 0,
      guid = byteArrayOf(),
      authKey = byteArrayOf()
    )
}
