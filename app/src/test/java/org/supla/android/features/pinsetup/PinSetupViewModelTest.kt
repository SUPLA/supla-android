package org.supla.android.features.pinsetup
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.BiometricUtils
import org.supla.android.core.infrastructure.ShaHashHelper
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.model.general.LockScreenSettings
import org.supla.android.tools.SuplaSchedulers

class PinSetupViewModelTest : BaseViewModelTest<PinSetupViewModelState, PinSetupViewEvent, PinSetupViewModel>(
  mockSchedulers = MockSchedulers.MOCKK
) {

  @MockK
  private lateinit var encryptedPreferences: EncryptedPreferences

  @MockK
  private lateinit var shaHashHelper: ShaHashHelper

  @MockK
  private lateinit var biometricUtils: BiometricUtils

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: PinSetupViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)

    super.setUp()
  }

  @Test
  fun `should setup pin`() {
    // given
    val scope = LockScreenScope.APPLICATION
    val pin = "1234"
    val pinHash = "pin hash"
    val lockScreenSettings = LockScreenSettings(scope, pinHash, true)
    every { shaHashHelper.getHash(pin) } returns pinHash
    every { encryptedPreferences.lockScreenSettings = lockScreenSettings } answers {}

    // when
    viewModel.onPinChange(pin)
    viewModel.onSecondPinChange(pin)
    viewModel.onBiometricAuthenticationChange(true)
    viewModel.onSaveClick(scope)

    // then
    assertThat(states).containsExactly(
      PinSetupViewModelState(PinSetupViewState(pin = pin)),
      PinSetupViewModelState(PinSetupViewState(pin = pin, secondPin = pin)),
      PinSetupViewModelState(PinSetupViewState(pin = pin, secondPin = pin, biometricAuthentication = true))
    )
    assertThat(events).containsExactly(PinSetupViewEvent.Close)

    verify {
      shaHashHelper.getHash(pin)
      encryptedPreferences.lockScreenSettings = lockScreenSettings
    }
    confirmVerified(shaHashHelper, encryptedPreferences, biometricUtils)
  }

  @Test
  fun `should inform about different pins`() {
    // given
    val scope = LockScreenScope.APPLICATION
    viewModel.setState(PinSetupViewModelState(PinSetupViewState(pin = "1234", secondPin = "2345")))

    // when
    viewModel.onSaveClick(scope)

    // then
    assertThat(states).containsExactly(
      PinSetupViewModelState(PinSetupViewState(pin = "1234", secondPin = "2345")),
      PinSetupViewModelState(PinSetupViewState(errorStringRes = R.string.pin_setup_entry_different))
    )
    assertThat(events).isEmpty()

    confirmVerified(shaHashHelper, encryptedPreferences, biometricUtils)
  }
}
