package org.supla.android.ui.dialogs.authorize

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.usecases.client.AuthorizationException
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString

@RunWith(MockitoJUnitRunner::class)
class BaseAuthorizationViewModelTest :
  BaseViewModelTest<TestAuthorizationModelState, TestAuthorizationViewEvent, TestAuthorizationViewModel>() {

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  private lateinit var profileRepository: RoomProfileRepository

  @Mock
  private lateinit var loginUseCase: LoginUseCase

  @Mock
  private lateinit var authorizeUseCase: AuthorizeUseCase

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: TestAuthorizationViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should show authorization dialog`() {
    // given
    val profile: ProfileEntity = mockk {
      every { email } returns "some-email@supla.org"
      every { isCloudAccount } returns true
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    // when
    viewModel.showAuthorizationDialog()

    // then
    assertThat(states).containsExactly(
      TestAuthorizationModelState(
        authorizationDialogState = AuthorizationDialogState(
          userName = "some-email@supla.org",
          isCloudAccount = true,
          userNameEnabled = false
        )
      )
    )

    verify(suplaClientProvider, times(2)).provide()
    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(suplaClientProvider, profileRepository)
    verifyNoInteractions(loginUseCase, authorizeUseCase)
  }

  @Test
  fun `shouldn't show authorization dialog when already authorized`() {
    // given
    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns true
      every { isSuperUserAuthorized() } returns true
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    viewModel.showAuthorizationDialog()

    // then
    assertThat(states).containsExactly(
      TestAuthorizationModelState(
        authorizationsCount = 1
      )
    )
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(suplaClientProvider)
    verifyNoInteractions(loginUseCase, authorizeUseCase, profileRepository)
  }

  @Test
  fun `shouldn't authorize when there is no supla client`() {
    // given
    val username = "username"
    val password = "password"
    whenever(authorizeUseCase.invoke(username, password))
      .thenReturn(Single.error(IllegalStateException("SuplaClient is null")))

    // when
    viewModel.authorize(username, password)

    // then
    assertThat(states)
      .extracting({ it.authorizationsCount }, { it.errors.count() }, { it.authorizationDialogState })
      .containsExactly(tuple(0, 1, null))
    assertThat(states[0].errors[0]).isInstanceOfAny(IllegalStateException::class.java)

    verify(authorizeUseCase).invoke(username, password)
    verifyNoMoreInteractions(authorizeUseCase)
    verifyNoInteractions(suplaClientProvider, loginUseCase, profileRepository)
  }

  @Test
  fun `should authorize with success`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    whenever(authorizeUseCase.invoke(userName, password)).thenReturn(Single.just(AuthorizeUseCase.Result.Authorized))

    // when
    viewModel.authorize(userName, password)

    // then
    assertThat(states).containsExactly(
      TestAuthorizationModelState(
        authorizationsCount = 1
      )
    )
    verify(authorizeUseCase).invoke(userName, password)
    verifyNoMoreInteractions(authorizeUseCase)
    verifyNoInteractions(loginUseCase, suplaClientProvider, profileRepository)
  }

  @Test
  fun `should authorize with error`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    whenever(authorizeUseCase.invoke(userName, password))
      .thenReturn(Single.error(AuthorizationException.WithResource(R.string.incorrect_email_or_password)))

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { isCloudAccount } returns true
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    viewModel.showAuthorizationDialog()
    viewModel.authorize(userName, password)

    // then
    assertThat(states)
      .extracting(
        { it.authorizationDialogState?.error },
        { it.authorizationDialogState?.processing },
        { it.errors.count() },
        { it.authorizationsCount }
      )
      .containsExactly(
        tuple(null, false, 0, 0),
        tuple(null, true, 0, 0),
        tuple(null, false, 0, 0),
        tuple(localizedString(R.string.incorrect_email_or_password), false, 0, 0)
      )
    verify(suplaClientProvider, times(2)).provide()
    verify(profileRepository).findActiveProfile()
    verify(authorizeUseCase).invoke(userName, password)
    verifyNoMoreInteractions(suplaClientProvider, authorizeUseCase, profileRepository)
    verifyNoInteractions(loginUseCase)
  }

  @Test
  fun `should not authorize without throwing an error`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    whenever(authorizeUseCase.invoke(userName, password))
      .thenReturn(Single.just(AuthorizeUseCase.Result.Unauthorized))

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { isCloudAccount } returns true
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    viewModel.showAuthorizationDialog()
    viewModel.authorize(userName, password)

    // then
    assertThat(states)
      .extracting(
        { it.authorizationDialogState?.error },
        { it.authorizationDialogState?.processing },
        { it.errors.count() },
        { it.authorizationsCount }
      )
      .containsExactly(
        tuple(null, false, 0, 0),
        tuple(null, true, 0, 0),
        tuple(null, false, 0, 0),
        tuple(localizedString(R.string.status_unknown_err), false, 0, 0)
      )
    verify(suplaClientProvider, times(2)).provide()
    verify(profileRepository).findActiveProfile()
    verify(authorizeUseCase).invoke(userName, password)
    verifyNoMoreInteractions(suplaClientProvider, authorizeUseCase, profileRepository)
    verifyNoInteractions(loginUseCase)
  }

  @Test
  fun `should login with success`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    whenever(loginUseCase.invoke(userName, password)).thenReturn(Single.just(LoginUseCase.Result.Authorized))

    // when
    viewModel.login(userName, password)

    // then
    assertThat(states).containsExactly(
      TestAuthorizationModelState(
        authorizationsCount = 1
      )
    )
    verify(loginUseCase).invoke(userName, password)
    verifyNoMoreInteractions(loginUseCase)
    verifyNoInteractions(authorizeUseCase, suplaClientProvider, profileRepository)
  }

  @Test
  fun `should not login without throwing an error`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    whenever(loginUseCase.invoke(userName, password))
      .thenReturn(Single.just(LoginUseCase.Result.Unauthorized))

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { isCloudAccount } returns true
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    viewModel.showAuthorizationDialog()
    viewModel.login(userName, password)

    // then
    assertThat(states)
      .extracting(
        { it.authorizationDialogState?.error },
        { it.authorizationDialogState?.processing },
        { it.errors.count() },
        { it.authorizationsCount }
      )
      .containsExactly(
        tuple(null, false, 0, 0),
        tuple(null, true, 0, 0),
        tuple(null, false, 0, 0),
        tuple(localizedString(R.string.status_unknown_err), false, 0, 0)
      )
    verify(suplaClientProvider, times(2)).provide()
    verify(profileRepository).findActiveProfile()
    verify(loginUseCase).invoke(userName, password)
    verifyNoMoreInteractions(suplaClientProvider, loginUseCase, profileRepository)
    verifyNoInteractions(authorizeUseCase)
  }

  @Test
  fun `should login with error`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    val exception = AuthorizationException.WithLocalizedString(localizedString(LocalizedStringId.RESULT_CODE_CLIENT_LIMIT_EXCEEDED))
    whenever(loginUseCase.invoke(userName, password))
      .thenReturn(Single.error(exception))

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { isCloudAccount } returns true
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    viewModel.showAuthorizationDialog()
    viewModel.login(userName, password)

    // then
    assertThat(states)
      .extracting(
        { it.authorizationDialogState?.error },
        { it.authorizationDialogState?.processing },
        { it.errors.count() },
        { it.authorizationsCount }
      )
      .containsExactly(
        tuple(null, false, 0, 0),
        tuple(null, true, 0, 0),
        tuple(null, false, 0, 0),
        tuple(localizedString(LocalizedStringId.RESULT_CODE_CLIENT_LIMIT_EXCEEDED), false, 0, 0)
      )
    verify(suplaClientProvider, times(2)).provide()
    verify(profileRepository).findActiveProfile()
    verify(loginUseCase).invoke(userName, password)
    verifyNoMoreInteractions(suplaClientProvider, loginUseCase, profileRepository)
    verifyNoInteractions(authorizeUseCase)
  }
}

class TestAuthorizationViewModel(
  suplaClientProvider: SuplaClientProvider,
  profileRepository: RoomProfileRepository,
  loginUseCase: LoginUseCase,
  authorizeUseCase: AuthorizeUseCase,
  schedulers: SuplaSchedulers
) : BaseAuthorizationViewModel<TestAuthorizationModelState, TestAuthorizationViewEvent>(
  suplaClientProvider,
  profileRepository,
  loginUseCase,
  authorizeUseCase,
  TestAuthorizationModelState(),
  schedulers
) {
  override fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { it.copy(authorizationDialogState = updater(it.authorizationDialogState)) }
  }

  override fun onAuthorized(reason: AuthorizationReason) {
    updateState { it.copy(authorizationsCount = it.authorizationsCount + 1) }
  }

  override fun onError(error: Throwable) {
    updateState { state ->
      state.copy(
        errors = mutableListOf<Throwable>().also {
          it.add(error)
          it.addAll(state.errors)
        }
      )
    }
  }
}

data class TestAuthorizationModelState(
  val authorizationsCount: Int = 0,
  val errors: List<Throwable> = emptyList(),
  override val authorizationDialogState: AuthorizationDialogState? = null,
) : AuthorizationModelState()

sealed class TestAuthorizationViewEvent : ViewEvent
