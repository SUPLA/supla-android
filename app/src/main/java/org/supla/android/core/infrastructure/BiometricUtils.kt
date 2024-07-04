package org.supla.android.core.infrastructure
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

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.supla.android.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricUtils @Inject constructor(
  private val biometricManager: BiometricManager
) {

  fun canAuthenticate(): AuthenticationStatus =
    when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
      BiometricManager.BIOMETRIC_SUCCESS -> AuthenticationStatus.POSSIBLE
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthenticationStatus.NOT_ENROLLED
      else -> AuthenticationStatus.UNAVAILABLE
    }

  fun showBiometricPrompt(
    fragment: Fragment,
    onAuthenticated: () -> Unit,
    onAuthenticationFailed: () -> Unit,
    onError: (errorCode: Int, errString: CharSequence) -> Unit
  ) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle(fragment.getString(R.string.app_name))
      .setSubtitle(fragment.getString(R.string.biometric_prompt_subtitle))
      .setNegativeButtonText(fragment.getString(R.string.cancel))
      .setAllowedAuthenticators(BIOMETRIC_STRONG)
      .build()

    val biometricPrompt = BiometricPrompt(
      fragment,
      ContextCompat.getMainExecutor(fragment.requireContext()),
      object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
          super.onAuthenticationSucceeded(result)
          onAuthenticated()
        }

        override fun onAuthenticationFailed() {
          onAuthenticationFailed()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
          onError(errorCode, errString)
        }
      }
    )

    biometricPrompt.authenticate(promptInfo)
  }

  enum class AuthenticationStatus {
    POSSIBLE, UNAVAILABLE, NOT_ENROLLED
  }
}
