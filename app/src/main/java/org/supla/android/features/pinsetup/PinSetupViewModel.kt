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

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.R
import org.supla.android.core.infrastructure.BiometricUtils
import org.supla.android.core.infrastructure.ShaHashHelper
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.model.general.LockScreenSettings
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.forms.PIN_LENGTH
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PinSetupViewModel @Inject constructor(
  private val encryptedPreferences: EncryptedPreferences,
  private val shaHashHelper: ShaHashHelper,
  private val biometricUtils: BiometricUtils,
  suplaSchedulers: SuplaSchedulers
) : BaseViewModel<PinSetupViewModelState, PinSetupViewEvent>(PinSetupViewModelState(), suplaSchedulers) {

  override fun onViewCreated() {
    updateState { it.copy(viewState = it.viewState.copy(biometricStatus = biometricUtils.canAuthenticate())) }
  }

  fun onPinChange(pin: String) {
    updateState { it.copy(viewState = it.viewState.copy(pin = pin, errorStringRes = null)) }
  }

  fun onSecondPinChange(pin: String) {
    updateState { it.copy(viewState = it.viewState.copy(secondPin = pin, errorStringRes = null)) }
  }

  fun onBiometricAuthenticationChange(biometricAuthentication: Boolean) {
    updateState { it.copy(viewState = it.viewState.copy(biometricAuthentication = biometricAuthentication)) }
  }

  fun onSaveClick(lockScreenScope: LockScreenScope) {
    val state = currentState()

    if (state.viewState.pin.length == PIN_LENGTH && state.viewState.pin == state.viewState.secondPin) {
      setUpLockScreen(lockScreenScope, state.viewState)
    } else {
      updateState {
        it.copy(
          viewState = it.viewState.copy(
            pin = "",
            secondPin = "",
            errorStringRes = R.string.pin_setup_entry_different
          )
        )
      }
    }
  }

  private fun setUpLockScreen(lockScreenScope: LockScreenScope, state: PinSetupViewState) {
    try {
      val pinHash = shaHashHelper.getHash(state.pin)
      encryptedPreferences.lockScreenSettings = LockScreenSettings(lockScreenScope, pinHash, state.biometricAuthentication)
      sendEvent(PinSetupViewEvent.Close)
    } catch (exception: Exception) {
      Timber.e(exception, "Pin setup failed!")

      updateState { it.copy(viewState = it.viewState.copy(errorStringRes = R.string.pin_setup_failed)) }
    }
  }
}

sealed class PinSetupViewEvent : ViewEvent {
  data object Close : PinSetupViewEvent()
}

data class PinSetupViewModelState(
  val viewState: PinSetupViewState = PinSetupViewState()
) : ViewState()
