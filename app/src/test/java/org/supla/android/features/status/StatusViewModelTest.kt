package org.supla.android.features.status
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
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.networking.suplaclient.SuplaClientState
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_BAD_CREDENTIALS
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_REGISTRATION_DISABLED
import org.supla.android.lib.SuplaRegisterError
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.DisconnectUseCase
import org.supla.android.usecases.client.LoginUseCase

class StatusViewModelTest : BaseViewModelTest<StatusViewModelState, StatusViewEvent, StatusViewModel>(
  MockSchedulers.MOCKK
) {

  @MockK
  private lateinit var suplaClientStateHolder: SuplaClientStateHolder

  @MockK
  private lateinit var disconnectUseCase: DisconnectUseCase

  @MockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @MockK
  private lateinit var profileRepository: RoomProfileRepository

  @MockK
  private lateinit var loginUseCase: LoginUseCase

  @MockK
  private lateinit var authorizeUseCase: AuthorizeUseCase

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: StatusViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should navigate to main fragment when connected`() {
    // given
    every { suplaClientStateHolder.state() } returns Observable.just(SuplaClientState.Connected)

    // when
    viewModel.onStart()

    // then
    assertThat(events).containsExactly(StatusViewEvent.NavigateToMain)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should show authorization dialog`() {
    // given
    val registerError = SuplaRegisterError()
    registerError.ResultCode = SUPLA_RESULTCODE_REGISTRATION_DISABLED
    val error = SuplaClientState.Reason.RegisterError(registerError)
    val finishedWithAuthNeeded = SuplaClientState.Finished(error)
    every { suplaClientStateHolder.state() } returns Observable.just(finishedWithAuthNeeded)

    val profile: ProfileEntity = mockk {
      every { email } returns "user@supla.org"
      every { isCloudAccount } returns true
    }
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    val suplaClient: SuplaClientApi = mockk {
      every { registered() } returns false
    }
    every { suplaClientProvider.provide() } returns suplaClient

    // when
    viewModel.onStart()

    // then
    val state = AuthorizationDialogState(
      userName = "user@supla.org",
      isCloudAccount = true,
      userNameEnabled = false
    )
    assertThat(events).isEmpty()
    assertThat(states)
      .extracting({ it.authorizationDialogState }, { it.viewType })
      .containsExactly(
        tuple(state, StatusViewModelState.ViewType.CONNECTING),
        tuple(state, StatusViewModelState.ViewType.ERROR)
      )
  }

  @Test
  fun `should show error without authorization dialog`() {
    // given
    val registerError = SuplaRegisterError()
    registerError.ResultCode = SUPLA_RESULTCODE_BAD_CREDENTIALS
    val error = SuplaClientState.Reason.RegisterError(registerError)
    val finishedWithAuthNeeded = SuplaClientState.Finished(error)
    every { suplaClientStateHolder.state() } returns Observable.just(finishedWithAuthNeeded)

    // when
    viewModel.onStart()

    // then
    assertThat(events).isEmpty()
    assertThat(states)
      .extracting({ it.authorizationDialogState }, { it.viewType })
      .containsExactly(
        tuple(null, StatusViewModelState.ViewType.ERROR)
      )
  }

  @Test
  fun `should show initialization`() {
    // given
    val state = StatusViewModelState(viewType = StatusViewModelState.ViewType.ERROR)
    viewModel.setState(state)
    every { suplaClientStateHolder.state() } returns Observable.just(SuplaClientState.Initialization)

    // when
    viewModel.onStart()

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      state,
      StatusViewModelState(
        viewType = StatusViewModelState.ViewType.CONNECTING,
        viewState = StatusViewState(stateText = StatusViewStateText.INITIALIZING)
      )
    )
  }

  @Test
  fun `should show connecting`() {
    // given
    every { suplaClientStateHolder.state() } returns Observable.just(SuplaClientState.Connecting())

    // when
    viewModel.onStart()

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      StatusViewModelState(
        viewType = StatusViewModelState.ViewType.CONNECTING,
        viewState = StatusViewState(stateText = StatusViewStateText.CONNECTING)
      )
    )
  }

  @Test
  fun `should show disconnecting`() {
    // given
    every { suplaClientStateHolder.state() } returns Observable.just(SuplaClientState.Disconnecting)

    // when
    viewModel.onStart()

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      StatusViewModelState(
        viewType = StatusViewModelState.ViewType.CONNECTING,
        viewState = StatusViewState(stateText = StatusViewStateText.DISCONNECTING)
      )
    )
  }

  @Test
  fun `should disconnect and open profiles when connecting`() {
    // given
    every { disconnectUseCase.invoke() } returns Completable.complete()

    // when
    viewModel.cancelAndOpenProfiles()

    // then
    assertThat(events).containsExactly(StatusViewEvent.NavigateToProfiles)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should try again`() {
    // given
    every { suplaClientStateHolder.handleEvent(SuplaClientEvent.Initialized) } answers {}

    // when
    viewModel.tryAgainClick()

    // then
    verify { suplaClientStateHolder.handleEvent(SuplaClientEvent.Initialized) }
    confirmVerified(suplaClientStateHolder)
  }
}
