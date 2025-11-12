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

import android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.forms.PIN_LENGTH
import org.supla.android.usecases.lock.CheckPinUseCase
import org.supla.core.shared.infrastructure.localizedString
import javax.inject.Inject

private const val BIOMETRIC_ERROR_CANCEL_BUTTON_PRESSED = 13

@HiltViewModel
class LockScreenViewModel @Inject constructor(
  private val encryptedPreferences: EncryptedPreferences,
  private val dateProvider: DateProvider,
  private val checkPinUseCase: CheckPinUseCase,
  suplaSchedulers: SuplaSchedulers
) : BaseViewModel<LockScreenViewModelState, LockScreenViewEvent>(LockScreenViewModelState(), suplaSchedulers) {

  override fun onViewCreated() {
    val lockScreenSettings = encryptedPreferences.lockScreenSettings
    val isPinLocked = lockScreenSettings.isLocked(dateProvider)
    if (lockScreenSettings.biometricAllowed && isPinLocked.not()) {
      sendEvent(LockScreenViewEvent.ShowBiometricPrompt)
    }

    val lockedTime = if (isPinLocked) lockScreenSettings.lockTime else null
    updateState { it.copy(viewState = it.viewState.copy(lockedTime = lockedTime, biometricAllowed = lockScreenSettings.biometricAllowed)) }
  }

  override fun setLoading(loading: Boolean) {
    updateState { it.copy(loading = loading) }
  }

  fun onCreate(unlockAction: UnlockAction) {
    updateState { it.copy(viewState = it.viewState.copy(unlockAction = unlockAction)) }
  }

  fun onPinChange(pin: String) {
    if (currentState().viewState.pin == pin) {
      return // nothing changed
    }

    updateState { it.copy(viewState = it.viewState.copy(pin = pin, wrongPin = false)) }

    if (pin.length == PIN_LENGTH) {
      verifyPin(CheckPinUseCase.PinAction.CheckPin(pin))
    }
  }

  fun onForgottenCodeButtonClick() {
    updateState { it.copy(showForgottenCodeDialog = true) }
  }

  fun hideForgottenCodeDialog() {
    updateState { it.copy(showForgottenCodeDialog = false) }
  }

  fun onBiometricError(code: Int, errorMessage: CharSequence) {
    if (code != BIOMETRIC_ERROR_USER_CANCELED && code != BIOMETRIC_ERROR_CANCEL_BUTTON_PRESSED) {
      updateState {
        it.copy(
          viewState = it.viewState.copy(
            authenticatorError = localizedString(R.string.lock_screen_biometric_error, errorMessage)
          )
        )
      }
    }
  }

  fun onBiometricFailure() {
    verifyPin(CheckPinUseCase.PinAction.BiometricRejected)
  }

  fun onBiometricSuccess() {
    verifyPin(CheckPinUseCase.PinAction.BiometricGranted)
  }

  private fun verifyPin(pinAction: CheckPinUseCase.PinAction) {
    checkPinUseCase(currentState().viewState.unlockAction, pinAction)
      .attachLoadable()
      .subscribeBy(
        onSuccess = {
          when (it) {
            CheckPinUseCase.Result.Unlocked -> sendEvent(LockScreenViewEvent.Close)
            CheckPinUseCase.Result.UnlockedNoAccount -> {} // No action
            CheckPinUseCase.Result.Failure ->
              updateState { state ->
                state.copy(
                  viewState = state.viewState.copy(
                    pin = "",
                    wrongPin = true,
                    lockedTime = encryptedPreferences.lockScreenSettings.lockTime
                  )
                )
              }
          }
        }
      )
      .disposeBySelf()
  }
}

sealed class LockScreenViewEvent : ViewEvent {
  data object Close : LockScreenViewEvent()
  data object ShowBiometricPrompt : LockScreenViewEvent()
}

data class LockScreenViewModelState(
  val viewState: LockScreenViewState = LockScreenViewState(),
  val showForgottenCodeDialog: Boolean = false,
  val loading: Boolean = false
) : ViewState()
