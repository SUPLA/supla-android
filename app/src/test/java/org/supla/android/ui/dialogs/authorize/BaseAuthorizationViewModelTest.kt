package org.supla.android.ui.dialogs.authorize
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
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.CoroutineTest
import org.supla.android.core.MainDispatcherRule
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.extensions.isNull
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.usecases.client.AuthorizationException
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString

@OptIn(ExperimentalCoroutinesApi::class)
class BaseAuthorizationViewModelTest : CoroutineTest {

  @get:Rule
  override val mainDispatcherRule = MainDispatcherRule()

  @MockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var loginUseCase: LoginUseCase

  @MockK
  private lateinit var authorizeUseCase: AuthorizeUseCase

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  private val testScope = TestScope()

  @SpyK
  @InjectMockKs
  private lateinit var viewModel: TestAuthorizationViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should show authorization dialog`() = testScope.runTest {
    // given
    val email = "some-email@supla.org"
    val profile: ProfileEntity = mockk {
      every { this@mockk.email } returns email
      every { this@mockk.isCloudAccount } returns true
    }
    every { profileRepository.findActiveProfile() } returns Single.just(profile)
    every { suplaClientProvider.provide() } returns null

    // when
    viewModel.showAuthorizationDialog()
    advanceUntilIdle()

    // then
    assertCreatedState {
      assertThat(userName).isEqualTo(email)
      assertThat(isCloudAccount).isTrue
      assertThat(userNameEnabled).isFalse
      assertThat(reason).isEqualTo(AuthorizationReason.Default)
      assertThat(clarification).isNull
    }
    verify(exactly = 2) { suplaClientProvider.provide() }
    verify { profileRepository.findActiveProfile() }
    confirmAllDependenciesVerified()
  }

  @Test
  fun `shouldn't show authorization dialog when already authorized`() {
    // given
    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns true
      every { isSuperUserAuthorized() } returns true
    }
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    viewModel.showAuthorizationDialog()

    // then
    verify { viewModel.onAuthorized(AuthorizationReason.Default) }
    verify { suplaClientProvider.provide() }
    confirmVerified(suplaClientProvider, loginUseCase, authorizeUseCase, profileRepository)
  }

  @Test
  fun `shouldn't authorize when there is no supla client`() = testScope.runTest {
    // given
    val username = "username"
    val password = "password"
    val error = IllegalStateException("SuplaClient is null")
    every { authorizeUseCase.invoke(username, password) } returns Single.error(error)

    // when
    viewModel.authorize(username, password)
    advanceUntilIdle()

    // then
    assertUpdatedState(2) {
      val firstUpdate = requireNotNull(it[0])
      assertThat(firstUpdate.processing).isTrue

      val secondUpdate = requireNotNull(it[1])
      assertThat(secondUpdate.processing).isFalse
    }
    verify { viewModel.onError(match { it is IllegalStateException && it.message == "SuplaClient is null" }) }
    verify { authorizeUseCase.invoke(username, password) }
    confirmVerified(authorizeUseCase, suplaClientProvider, loginUseCase, profileRepository)
  }

  @Test
  fun `should authorize with success`() = testScope.runTest {
    // given
    val userName = "test@supla.org"
    val password = "password"
    every { authorizeUseCase.invoke(userName, password) } returns Single.just(AuthorizeUseCase.Result.Authorized)

    // when
    viewModel.authorize(userName, password)
    advanceUntilIdle()

    // then
    assertUpdatedState(2) {
      val firstUpdate = requireNotNull(it[0])
      assertThat(firstUpdate.processing).isTrue

      val secondUpdate = requireNotNull(it[1])
      assertThat(secondUpdate.processing).isFalse
    }
    verify { viewModel.onAuthorized(AuthorizationReason.Default) }
    verify { authorizeUseCase.invoke(userName, password) }
    confirmVerified(authorizeUseCase, loginUseCase, suplaClientProvider, profileRepository)
  }

  @Test
  fun `should not authorize without throwing an error`() = testScope.runTest {
    // given
    val userName = "test@supla.org"
    val password = "password"
    every { authorizeUseCase.invoke(userName, password) } returns
      Single.error(AuthorizationException.WithResource(R.string.incorrect_email_or_password))

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { isCloudAccount } returns true
    }
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
    }
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    viewModel.showAuthorizationDialog(reason = AuthorizationReason.ZWaveWizard)
    advanceUntilIdle()
    viewModel.authorize(userName, password)
    advanceUntilIdle()

    // then
    assertUpdatedState(3) {
      // from viewModel.showAuthorizationDialog
      var state = requireNotNull(it[0])
      assertThat(state.userName).isEqualTo(userName)
      assertThat(state.isCloudAccount).isTrue
      assertThat(state.userNameEnabled).isFalse
      assertThat(state.reason).isEqualTo(AuthorizationReason.ZWaveWizard)
      assertThat(state.clarification).isNull

      // from viewModel.authorize()
      state = requireNotNull(it[1])
      assertThat(state.processing).isTrue

      state = requireNotNull(it[2])
      assertThat(state.processing).isFalse
      assertThat(state.error).isEqualTo(localizedString(R.string.incorrect_email_or_password))
    }
    verify(exactly = 2) { suplaClientProvider.provide() }
    verify { profileRepository.findActiveProfile() }
    verify { authorizeUseCase.invoke(userName, password) }
    confirmVerified(suplaClientProvider, authorizeUseCase, profileRepository, loginUseCase)
  }

  @Test
  fun `should not authorize`() = testScope.runTest {
    // given
    val userName = "test@supla.org"
    val password = "password"
    every { authorizeUseCase.invoke(userName, password) } returns Single.just(AuthorizeUseCase.Result.Unauthorized)

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { isCloudAccount } returns true
    }
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
    }
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    viewModel.showAuthorizationDialog()
    advanceUntilIdle()
    viewModel.authorize(userName, password)
    advanceUntilIdle()

    // then
    assertUpdatedState(4) {
      // from viewModel.showAuthorizationDialog
      var state = requireNotNull(it[0])
      assertThat(state.userName).isEqualTo(userName)
      assertThat(state.isCloudAccount).isTrue
      assertThat(state.userNameEnabled).isFalse
      assertThat(state.reason).isEqualTo(AuthorizationReason.Default)
      assertThat(state.clarification).isNull

      // from viewModel.authorize()
      state = requireNotNull(it[1])
      assertThat(state.processing).isTrue

      state = requireNotNull(it[2])
      assertThat(state.processing).isFalse

      state = requireNotNull(it[3])
      assertThat(state.error).isEqualTo(localizedString(R.string.status_unknown_err))
    }

    verify(exactly = 2) { suplaClientProvider.provide() }
    verify { profileRepository.findActiveProfile() }
    verify { authorizeUseCase.invoke(userName, password) }
    confirmVerified(suplaClientProvider, authorizeUseCase, profileRepository, loginUseCase)
  }

  @Test
  fun `should login with success`() = testScope.runTest {
    // given
    val userName = "test@supla.org"
    val password = "password"
    every { loginUseCase.invoke(userName, password) } returns Single.just(LoginUseCase.Result.Authorized)

    // when
    viewModel.login(userName, password)
    advanceUntilIdle()

    // then
    verify { viewModel.onAuthorized(AuthorizationReason.Default) }
    verify { loginUseCase.invoke(userName, password) }
    confirmVerified(loginUseCase, authorizeUseCase, suplaClientProvider, profileRepository)
  }

  @Test
  fun `should not login returning unauthorized`() = testScope.runTest {
    // given
    val userName = "test@supla.org"
    val password = "password"
    every { loginUseCase.invoke(userName, password) } returns Single.just(LoginUseCase.Result.Unauthorized)

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { isCloudAccount } returns true
    }
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
    }
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    viewModel.showAuthorizationDialog()
    advanceUntilIdle()
    viewModel.login(userName, password)
    advanceUntilIdle()

    // then
    assertUpdatedState(4) {
      // from viewModel.showAuthorizationDialog
      var state = requireNotNull(it[0])
      assertThat(state.userName).isEqualTo(userName)
      assertThat(state.isCloudAccount).isTrue
      assertThat(state.userNameEnabled).isFalse
      assertThat(state.reason).isEqualTo(AuthorizationReason.Default)
      assertThat(state.clarification).isNull

      // from viewModel.authorize()
      state = requireNotNull(it[1])
      assertThat(state.processing).isTrue

      state = requireNotNull(it[2])
      assertThat(state.processing).isFalse

      state = requireNotNull(it[3])
      assertThat(state.error).isEqualTo(localizedString(R.string.status_unknown_err))
    }
    verify(exactly = 2) { suplaClientProvider.provide() }
    verify { profileRepository.findActiveProfile() }
    verify { loginUseCase.invoke(userName, password) }
    confirmVerified(suplaClientProvider, loginUseCase, profileRepository, authorizeUseCase)
  }

  @Test
  fun `should login with error`() = testScope.runTest {
    // given
    val userName = "test@supla.org"
    val password = "password"
    val exception = AuthorizationException.WithLocalizedString(localizedString(LocalizedStringId.RESULT_CODE_CLIENT_LIMIT_EXCEEDED))
    every { loginUseCase.invoke(userName, password) } returns Single.error(exception)

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { isCloudAccount } returns true
    }
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
    }
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    viewModel.showAuthorizationDialog()
    advanceUntilIdle()
    viewModel.login(userName, password)
    advanceUntilIdle()

    // then
    assertUpdatedState(3) {
      // from viewModel.showAuthorizationDialog
      var state = requireNotNull(it[0])
      assertThat(state.userName).isEqualTo(userName)
      assertThat(state.isCloudAccount).isTrue
      assertThat(state.userNameEnabled).isFalse
      assertThat(state.reason).isEqualTo(AuthorizationReason.Default)
      assertThat(state.clarification).isNull

      // from viewModel.authorize()
      state = requireNotNull(it[1])
      assertThat(state.processing).isTrue

      state = requireNotNull(it[2])
      assertThat(state.processing).isFalse
      assertThat(state.error).isEqualTo(localizedString(LocalizedStringId.RESULT_CODE_CLIENT_LIMIT_EXCEEDED))
    }
    verify(exactly = 2) { suplaClientProvider.provide() }
    verify { profileRepository.findActiveProfile() }
    verify { loginUseCase.invoke(userName, password) }
    confirmVerified(suplaClientProvider, loginUseCase, profileRepository, authorizeUseCase)
  }

  private fun confirmAllDependenciesVerified() {
    confirmVerified(
      suplaClientProvider,
      loginUseCase,
      profileRepository,
      authorizeUseCase
    )
  }

  private fun assertCreatedState(assertions: AuthorizationDialogState.() -> Unit) {
    val stateSlot = slot<(AuthorizationDialogState?) -> AuthorizationDialogState?>()
    verify {
      viewModel.updateAuthorizationDialogState(capture(stateSlot))
    }
    val state = stateSlot.captured.invoke(null)
    assertThat(state).isNotNull

    val stateNotNull = requireNotNull(state)
    assertions.invoke(stateNotNull)
  }

  private fun assertUpdatedState(count: Int = 1, assertions: (List<AuthorizationDialogState?>) -> Unit) {
    val captured = mutableListOf<(AuthorizationDialogState?) -> AuthorizationDialogState?>()
    verify(exactly = count) {
      viewModel.updateAuthorizationDialogState(capture(captured))
    }
    val initialState = AuthorizationDialogState(userName = "", isCloudAccount = false, userNameEnabled = false)

    assertThat(captured.size).isEqualTo(count)
    assertions.invoke(captured.map { it.invoke(initialState) })
  }
}

class TestAuthorizationViewModel(
  override val suplaClientProvider: SuplaClientProvider,
  override val profileRepository: ProfileRepository,
  override val loginUseCase: LoginUseCase,
  override val authorizeUseCase: AuthorizeUseCase,
  override val schedulers: SuplaSchedulers,
  private val testScope: TestScope
) : BaseAuthorizationViewModelScope {

  override fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
  }

  override fun getAuthorizationDialogState(): AuthorizationDialogState? = null

  override fun onAuthorized(reason: AuthorizationReason) {
  }

  override fun launch(launcher: suspend CoroutineScope.() -> Unit) {
    testScope.launch {
      launcher()
    }
  }

  override fun onAuthorizationDismiss() {
  }

  override fun onAuthorizationCancel() {
  }

  override fun onAuthorize(userName: String, password: String) {
  }

  override fun onStateChange(state: AuthorizationDialogState) {
  }
}
