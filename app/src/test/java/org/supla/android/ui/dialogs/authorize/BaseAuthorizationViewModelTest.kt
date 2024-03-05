package org.supla.android.ui.dialogs.authorize

import android.content.Context
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_UNAUTHORIZED
import org.supla.android.lib.SuplaRegisterError
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState

@RunWith(MockitoJUnitRunner::class)
class BaseAuthorizationViewModelTest :
  BaseViewModelTest<TestAuthorizationModelState, TestAuthorizationViewEvent, TestAuthorizationViewModel>() {

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  private lateinit var profileRepository: RoomProfileRepository

  @Mock
  private lateinit var suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper

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
    verifyZeroInteractions(suplaClientMessageHandlerWrapper)
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
    verifyZeroInteractions(suplaClientMessageHandlerWrapper, profileRepository)
  }

  @Test
  fun `shouldn't authorize when there is no supla client`() {
    // when
    viewModel.authorize("test", "test")

    // then
    assertThat(states)
      .extracting({ it.authorizationsCount }, { it.errors.count() }, { it.authorizationDialogState })
      .containsExactly(tuple(0, 1, null))
    assertThat(states[0].errors[0]).isInstanceOfAny(IllegalStateException::class.java)

    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(suplaClientProvider)
    verifyZeroInteractions(suplaClientMessageHandlerWrapper, profileRepository)
  }

  @Test
  fun `should authorize with success`() {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val suplaClient: SuplaClientApi = mockk {
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onSuperuserAuthorizationResult
      every { isSuccess } returns true
    }
    var listener: OnSuplaClientMessageListener? = null
    doAnswer {
      listener = it.arguments[0] as OnSuplaClientMessageListener
      listener?.onSuplaClientMessageReceived(message)
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

    // when
    viewModel.authorize(userName, password)

    // then
    assertThat(states).containsExactly(
      TestAuthorizationModelState(
        authorizationsCount = 1
      )
    )
    verify(suplaClientProvider).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper, times(2)).unregisterMessageListener(listener!!)
    verifyNoMoreInteractions(suplaClientProvider, suplaClientMessageHandlerWrapper)
    verifyZeroInteractions(profileRepository)
  }

  @Test
  fun `should authorize with unauthorized error`() {
    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onSuperuserAuthorizationResult
      every { isSuccess } returns false
      every { code } returns SUPLA_RESULTCODE_UNAUTHORIZED
    }

    doAuthorizationTestWithError(message, R.string.incorrect_email_or_password)
  }

  @Test
  fun `should authorize with temporarily unavailable error`() {
    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onSuperuserAuthorizationResult
      every { isSuccess } returns false
      every { code } returns SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE
    }

    doAuthorizationTestWithError(message, R.string.status_temporarily_unavailable)
  }

  @Test
  fun `should authorize with unknown error`() {
    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onSuperuserAuthorizationResult
      every { isSuccess } returns false
      every { code } returns 0
    }

    doAuthorizationTestWithError(message, R.string.status_unknown_err)
  }

  @Test
  fun `should login with success`() {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val suplaClient: SuplaClientApi = mockk {
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onRegistered
    }
    var listener: OnSuplaClientMessageListener? = null
    doAnswer {
      listener = it.arguments[0] as OnSuplaClientMessageListener
      listener?.onSuplaClientMessageReceived(message)
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

    // when
    viewModel.login(userName, password)

    // then
    assertThat(states).containsExactly(
      TestAuthorizationModelState(
        authorizationsCount = 1
      )
    )
    verify(suplaClientProvider).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper, times(2)).unregisterMessageListener(listener!!)
    verifyNoMoreInteractions(suplaClientProvider, suplaClientMessageHandlerWrapper)
    verifyZeroInteractions(profileRepository)
  }

  @Test
  fun `should login with error`() {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { serverForEmail } returns "cloud.supla.org"
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    val message: SuplaClientMsg = mockk {
      every { type } returns SuplaClientMsg.onRegisterError
      every { registerError } returns SuplaRegisterError().also {
        it.ResultCode = SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED
      }
      every { code } returns SUPLA_RESULTCODE_UNAUTHORIZED
    }
    var listener: OnSuplaClientMessageListener? = null
    doAnswer {
      listener = it.arguments[0] as OnSuplaClientMessageListener
      listener?.onSuplaClientMessageReceived(message)
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

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
        tuple(R.string.status_climit_exceded, true, 0, 0),
        tuple(R.string.status_climit_exceded, false, 0, 0)
      )
    verify(suplaClientProvider, times(3)).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper, times(2)).unregisterMessageListener(listener!!)
    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(suplaClientProvider, suplaClientMessageHandlerWrapper, profileRepository)
  }

  @Test
  fun `should authorize with timeout`() {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { serverForEmail } returns "cloud.supla.org"
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    var listener: OnSuplaClientMessageListener? = null
    doAnswer {
      listener = it.arguments[0] as OnSuplaClientMessageListener
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

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
        tuple(R.string.time_exceeded, true, 0, 0),
        tuple(R.string.time_exceeded, false, 0, 0)
      )
    verify(suplaClientProvider, times(3)).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper).unregisterMessageListener(listener!!)
    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(suplaClientProvider, suplaClientMessageHandlerWrapper, profileRepository)
  }

  private fun doAuthorizationTestWithError(message: SuplaClientMsg, errorMessage: Int) {
    // given
    val userName = "test@supla.org"
    val password = "password"

    val profile: ProfileEntity = mockk {
      every { email } returns userName
      every { serverForEmail } returns "cloud.supla.org"
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
      every { superUserAuthorizationRequest(userName, password) } answers {}
    }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    var listener: OnSuplaClientMessageListener? = null
    doAnswer {
      listener = it.arguments[0] as OnSuplaClientMessageListener
      listener?.onSuplaClientMessageReceived(message)
    }.whenever(suplaClientMessageHandlerWrapper).registerMessageListener(any())

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
        tuple(errorMessage, true, 0, 0),
        tuple(errorMessage, false, 0, 0)
      )
    verify(suplaClientProvider, times(3)).provide()
    verify(suplaClientMessageHandlerWrapper).registerMessageListener(listener!!)
    verify(suplaClientMessageHandlerWrapper, times(2)).unregisterMessageListener(listener!!)
    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(suplaClientProvider, suplaClientMessageHandlerWrapper, profileRepository)
  }
}

class TestAuthorizationViewModel(
  suplaClientProvider: SuplaClientProvider,
  profileRepository: RoomProfileRepository,
  suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper,
  schedulers: SuplaSchedulers
) : BaseAuthorizationViewModel<TestAuthorizationModelState, TestAuthorizationViewEvent>(
  suplaClientProvider,
  profileRepository,
  suplaClientMessageHandlerWrapper,
  TestAuthorizationModelState(),
  schedulers,
  1L
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
