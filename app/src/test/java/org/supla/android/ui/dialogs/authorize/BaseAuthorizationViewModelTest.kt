package org.supla.android.ui.dialogs.authorize

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
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
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.usecases.client.AuthorizationException
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase

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
      every { serverForEmail } returns "cloud.supla.org"
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
    verifyZeroInteractions(loginUseCase, authorizeUseCase)
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
    verifyZeroInteractions(loginUseCase, authorizeUseCase, profileRepository)
  }

  @Test
  fun `shouldn't authorize when there is no supla client`() {
    // given
    val username = "username"
    val password = "password"
    whenever(authorizeUseCase.invoke(username, password))
      .thenReturn(Completable.error(IllegalStateException("SuplaClient is null")))

    // when
    viewModel.authorize(username, password)

    // then
    assertThat(states)
      .extracting({ it.authorizationsCount }, { it.errors.count() }, { it.authorizationDialogState })
      .containsExactly(tuple(0, 1, null))
    assertThat(states[0].errors[0]).isInstanceOfAny(IllegalStateException::class.java)

    verify(authorizeUseCase).invoke(username, password)
    verifyNoMoreInteractions(loginUseCase)
    verifyZeroInteractions(suplaClientProvider, authorizeUseCase, profileRepository)
  }

  @Test
  fun `should authorize with success`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    whenever(authorizeUseCase.invoke(userName, password)).thenReturn(Completable.complete())

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
    verifyZeroInteractions(loginUseCase, suplaClientProvider, profileRepository)
  }

  @Test
  fun `should authorize with error`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    whenever(authorizeUseCase.invoke(userName, password))
      .thenReturn(Completable.error(AuthorizationException(R.string.incorrect_email_or_password)))

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { serverForEmail } returns "cloud.supla.org"
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
    val context: Context = mockk {
      every { getString(any()) } answers { "${it.invocation.args.first()}" }
    }
    assertThat(states)
      .extracting(
        { it.authorizationDialogState?.error?.let { it(context) }?.toInt() },
        { it.authorizationDialogState?.processing },
        { it.errors.count() },
        { it.authorizationsCount }
      )
      .containsExactly(
        tuple(null, false, 0, 0),
        tuple(null, true, 0, 0),
        tuple(null, false, 0, 0),
        tuple(R.string.incorrect_email_or_password, false, 0, 0)
      )
    verify(suplaClientProvider, times(2)).provide()
    verify(profileRepository).findActiveProfile()
    verify(authorizeUseCase).invoke(userName, password)
    verifyNoMoreInteractions(suplaClientProvider, authorizeUseCase, profileRepository)
    verifyZeroInteractions(loginUseCase)
  }

  @Test
  fun `should login with success`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    whenever(loginUseCase.invoke(userName, password)).thenReturn(Completable.complete())

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
    verifyZeroInteractions(authorizeUseCase, suplaClientProvider, profileRepository)
  }

  @Test
  fun `should login with error`() {
    // given
    val userName = "test@supla.org"
    val password = "password"
    whenever(loginUseCase.invoke(userName, password))
      .thenReturn(Completable.error(AuthorizationException(SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED)))

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { serverForEmail } returns "cloud.supla.org"
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
    val context: Context = mockk {
      every { resources } returns mockk {
        every { getString(any()) } answers { "${it.invocation.args.first()}" }
      }
    }
    assertThat(states)
      .extracting(
        { it.authorizationDialogState?.error?.let { it(context) }?.toInt() },
        { it.authorizationDialogState?.processing },
        { it.errors.count() },
        { it.authorizationsCount }
      )
      .containsExactly(
        tuple(null, false, 0, 0),
        tuple(null, true, 0, 0),
        tuple(null, false, 0, 0),
        tuple(R.string.status_climit_exceded, false, 0, 0)
      )
    verify(suplaClientProvider, times(2)).provide()
    verify(profileRepository).findActiveProfile()
    verify(loginUseCase).invoke(userName, password)
    verifyNoMoreInteractions(suplaClientProvider, loginUseCase, profileRepository)
    verifyZeroInteractions(authorizeUseCase)
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
  override fun updateDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { it.copy(authorizationDialogState = updater(it.authorizationDialogState)) }
  }

  override fun onAuthorized() {
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
