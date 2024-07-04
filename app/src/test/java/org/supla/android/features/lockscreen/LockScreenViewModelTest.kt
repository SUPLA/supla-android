package org.supla.android.features.lockscreen
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
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.model.general.LockScreenSettings
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.lock.CheckPinUseCase
import java.util.Date

class LockScreenViewModelTest : BaseViewModelTest<LockScreenViewModelState, LockScreenViewEvent, LockScreenViewModel>(
  mockSchedulers = MockSchedulers.MOCKK
) {
  @MockK
  private lateinit var encryptedPreferences: EncryptedPreferences

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var checkPinUseCase: CheckPinUseCase

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: LockScreenViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)

    super.setUp()
  }

  @Test
  fun `should show biometric prompt on start`() {
    // given
    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings(LockScreenScope.APPLICATION, "", true)

    // when
    viewModel.onViewCreated()

    // then
    assertThat(states).containsExactly(LockScreenViewModelState(LockScreenViewState(biometricAllowed = true)))
    assertThat(events).containsExactly(LockScreenViewEvent.ShowBiometricPrompt)
  }

  @Test
  fun `should not show biometric prompt when not allowed`() {
    // given
    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings(LockScreenScope.APPLICATION, "", false)

    // when
    viewModel.onViewCreated()

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()
  }

  @Test
  fun `should not show biometric prompt when pin locked`() {
    // given
    val lockTime = 100L
    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings(
      scope = LockScreenScope.APPLICATION,
      pinSum = "",
      biometricAllowed = true,
      failsCount = 0,
      lockTime = lockTime
    )
    every { dateProvider.currentDate() } returns Date(50)

    // when
    viewModel.onViewCreated()

    // then
    assertThat(states).containsExactly(LockScreenViewModelState(LockScreenViewState(biometricAllowed = true, lockedTime = lockTime)))
    assertThat(events).isEmpty()
  }

  @Test
  fun `should not verify pin when shorter then expected`() {
    // given
    val pin = "123"

    // when
    viewModel.onPinChange(pin)

    // then
    assertThat(states).containsExactly(LockScreenViewModelState(LockScreenViewState(pin)))
    assertThat(events).isEmpty()
    confirmVerified(checkPinUseCase)
  }

  @Test
  fun `should verify pin - unlock`() {
    // given
    val pin = "1234"
    every { checkPinUseCase.invoke(UnlockAction.AuthorizeApplication, CheckPinUseCase.PinAction.CheckPin(pin)) }
      .returns(Single.just(CheckPinUseCase.Result.Unlocked))

    // when
    viewModel.onPinChange(pin)

    // then
    assertThat(states).containsExactly(
      LockScreenViewModelState(LockScreenViewState(pin)),
      LockScreenViewModelState(LockScreenViewState(pin), loading = true),
      LockScreenViewModelState(LockScreenViewState(pin))
    )
    assertThat(events).containsExactly(LockScreenViewEvent.Close)
    verify {
      checkPinUseCase.invoke(UnlockAction.AuthorizeApplication, CheckPinUseCase.PinAction.CheckPin(pin))
    }
    confirmVerified(checkPinUseCase)
  }

  @Test
  fun `should verify pin - unlock no account`() {
    // given
    every { checkPinUseCase.invoke(UnlockAction.AuthorizeApplication, CheckPinUseCase.PinAction.BiometricGranted) }
      .returns(Single.just(CheckPinUseCase.Result.UnlockedNoAccount))

    // when
    viewModel.onBiometricSuccess()

    // then
    assertThat(states).containsExactly(
      LockScreenViewModelState(loading = true),
      LockScreenViewModelState()
    )
    assertThat(events).isEmpty()
    verify {
      checkPinUseCase.invoke(UnlockAction.AuthorizeApplication, CheckPinUseCase.PinAction.BiometricGranted)
    }
    confirmVerified(checkPinUseCase)
  }

  @Test
  fun `should verify pin - failure`() {
    // given
    val lockedTime = 123L
    every { encryptedPreferences.lockScreenSettings }
      .returns(LockScreenSettings(LockScreenScope.APPLICATION, "", true, 0, lockedTime))
    every { checkPinUseCase.invoke(UnlockAction.AuthorizeApplication, CheckPinUseCase.PinAction.BiometricRejected) }
      .returns(Single.just(CheckPinUseCase.Result.Failure))

    // when
    viewModel.onBiometricFailure()

    // then
    assertThat(states).containsExactly(
      LockScreenViewModelState(loading = true),
      LockScreenViewModelState(),
      LockScreenViewModelState(LockScreenViewState(wrongPin = true, lockedTime = lockedTime))
    )
    assertThat(events).isEmpty()
    verify {
      checkPinUseCase.invoke(UnlockAction.AuthorizeApplication, CheckPinUseCase.PinAction.BiometricRejected)
    }
    confirmVerified(checkPinUseCase)
  }

  @Test
  fun `should not verify pin - when does not changed`() {
    // given
    val pin = "1234"
    viewModel.setState(LockScreenViewModelState(LockScreenViewState(pin)))

    // when
    viewModel.onPinChange(pin)

    // then
    assertThat(events).isEmpty()
    assertThat(states).containsExactly(
      LockScreenViewModelState(LockScreenViewState(pin))
    )
    confirmVerified(checkPinUseCase)
  }
}
