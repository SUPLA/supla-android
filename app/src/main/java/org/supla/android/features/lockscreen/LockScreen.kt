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

import androidx.activity.compose.BackHandler
import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.AuthenticationResultCallback
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.extensions.findActivity
import org.supla.android.features.nfc.call.screens.ViewModelHost
import org.supla.android.main.MainComposeNavigator
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.views.LoadingScrim

@Composable
fun LockScreen(
  unlockAction: UnlockAction,
  navigator: MainComposeNavigator,
  viewModel: LockScreenViewModel = hiltViewModel()
) {
  val launcher = rememberAuthenticationLauncher(
    resultCallback = object : AuthenticationResultCallback {
      override fun onAuthResult(result: AuthenticationResult) {
        when (result) {
          is AuthenticationResult.Success -> viewModel.onBiometricSuccess()
          is AuthenticationResult.Error -> viewModel.onBiometricError(result.errorCode, result.errString)
          is AuthenticationResult.CustomFallbackSelected -> {} // should never happen
        }
      }

      override fun onAuthAttemptFailed() {
        viewModel.onBiometricFailure()
      }
    }
  )
  val biometricTitle = stringResource(R.string.app_name)
  val biometricSubtitle = stringResource(R.string.biometric_prompt_subtitle)

  val context = LocalContext.current
  BackHandler(enabled = true) {
    when (unlockAction) {
      UnlockAction.AuthorizeApplication -> context.findActivity()?.finishAffinity() ?: navigator.back()
      UnlockAction.TurnOffPin,
      UnlockAction.ConfirmAuthorizeAccounts,
      UnlockAction.ConfirmAuthorizeApplication,
      UnlockAction.AuthorizeAccountsCreate,
      is UnlockAction.AuthorizeAccountsEdit -> navigator.back()
    }
  }

  ViewModelHost(
    viewModel = viewModel,
    eventHandler = {
      handleEvent(it, unlockAction, navigator) {
        launcher.launch(
          AuthenticationRequest.Biometric.Builder(biometricTitle)
            .setSubtitle(biometricSubtitle)
            .build()
        )
      }
    },
    onCreate = { viewModel.onCreate(unlockAction) }
  ) { modelState ->
    if (modelState.showForgottenCodeDialog) {
      AlertDialog(
        title = stringResource(id = R.string.lock_screen_forgotten_code_title),
        message = stringResource(id = R.string.lock_screen_forgotten_code_message),
        positiveButtonTitle = stringResource(id = R.string.lock_screen_forgotten_code_button),
        negativeButtonTitle = null,
        onPositiveClick = viewModel::hideForgottenCodeDialog,
        onDismiss = viewModel::hideForgottenCodeDialog
      )
    }
    LockScreenView(
      viewState = modelState.viewState,
      onPinChange = viewModel::onPinChange,
      onForgottenCodeClick = viewModel::onForgottenCodeButtonClick,
      onFingerprintIconClick = {
        launcher.launch(
          AuthenticationRequest.Biometric.Builder(biometricTitle)
            .setSubtitle(biometricSubtitle)
            .build()
        )
      }
    )

    if (modelState.loading) {
      LoadingScrim()
    }
  }
}

private fun handleEvent(
  event: LockScreenViewEvent,
  unlockAction: UnlockAction,
  navigator: MainComposeNavigator,
  biometricLauncher: () -> Unit
) {
  when (event) {
    LockScreenViewEvent.Close ->
      when (unlockAction) {
        UnlockAction.AuthorizeApplication,
        UnlockAction.TurnOffPin,
        UnlockAction.ConfirmAuthorizeAccounts,
        UnlockAction.ConfirmAuthorizeApplication -> navigator.back()
        UnlockAction.AuthorizeAccountsCreate -> {
          // TODO: Not implemented yet - handled by fragment
          // configNavigator.back()
          // configNavigator.navigateTo(R.id.cfgNewProfile)
        }
        is UnlockAction.AuthorizeAccountsEdit -> {
          // TODO: Not implemented yet - handled by fragment
          // configNavigator.back()
          // configNavigator.navigateTo(R.id.cfgEditProfile, CreateAccountFragment.bundle(action.profileId))
        }
      }
    LockScreenViewEvent.ShowBiometricPrompt -> biometricLauncher()
  }
}
