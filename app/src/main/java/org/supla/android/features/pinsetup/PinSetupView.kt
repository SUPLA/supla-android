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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.infrastructure.BiometricUtils
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.BodyMedium
import org.supla.android.ui.views.PIN_LENGTH
import org.supla.android.ui.views.PinTextField
import org.supla.android.ui.views.Switch
import org.supla.android.ui.views.buttons.Button

data class PinSetupViewState(
  val pin: String = "",
  val secondPin: String = "",

  val biometricStatus: BiometricUtils.AuthenticationStatus = BiometricUtils.AuthenticationStatus.POSSIBLE,
  val biometricAuthentication: Boolean = false,

  val errorStringRes: Int? = null
) {
  val saveEnabled: Boolean
    get() = pin.length == PIN_LENGTH && secondPin.length == PIN_LENGTH
}

@Composable
fun PinSetupView(
  viewState: PinSetupViewState,
  onPinChange: (String) -> Unit = {},
  onSecondPinChange: (String) -> Unit = {},
  onBiometricAuthenticationChange: (Boolean) -> Unit = {},
  onSaveClick: () -> Unit = {}
) {
  val firstPinFocusRequester = remember { FocusRequester() }
  val secondPinFocusRequester = remember { FocusRequester() }
  LaunchedEffect(viewState.pin) {
    if (viewState.pin.isEmpty()) {
      firstPinFocusRequester.requestFocus()
    } else if (viewState.pin.length == PIN_LENGTH) {
      secondPinFocusRequester.requestFocus()
    }
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(Distance.default),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(all = Distance.default)
  ) {
    BodyMedium(text = stringResource(id = R.string.pin_setup_header))
    PinTextField(
      pin = viewState.pin,
      onPinChange = { pin, _ -> onPinChange(pin) },
      modifier = Modifier.focusRequester(firstPinFocusRequester)
    )
    BodyMedium(text = stringResource(id = R.string.pin_setup_repeat))
    PinTextField(
      pin = viewState.secondPin,
      onPinChange = { pin, _ -> onSecondPinChange(pin) },
      isError = viewState.errorStringRes != null,
      modifier = Modifier.focusRequester(secondPinFocusRequester)
    )
    viewState.errorStringRes?.let {
      BodyMedium(stringRes = it, color = MaterialTheme.colorScheme.error)
    }

    when (viewState.biometricStatus) {
      BiometricUtils.AuthenticationStatus.POSSIBLE ->
        BiometricSwitch(viewState, onBiometricAuthenticationChange)

      BiometricUtils.AuthenticationStatus.NOT_ENROLLED ->
        Text(
          text = stringResource(id = R.string.pin_setup_biometric_not_enrolled),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          textAlign = TextAlign.Center
        )

      else -> {}
    }

    Spacer(modifier = Modifier.weight(1f))

    Button(
      text = stringResource(id = R.string.save),
      onClick = onSaveClick,
      enabled = viewState.saveEnabled,
      modifier = Modifier
        .fillMaxWidth()
    )
  }
}

@Composable
private fun BiometricSwitch(viewState: PinSetupViewState, onBiometricAuthenticationChange: (Boolean) -> Unit) =
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.default),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = stringResource(id = R.string.pin_setup_use_biometric),
      style = MaterialTheme.typography.bodyMedium
    )
    Switch(checked = viewState.biometricAuthentication, onCheckedChange = onBiometricAuthenticationChange)
  }

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    PinSetupView(PinSetupViewState())
  }
}

@Preview
@Composable
private fun Preview_WithSecond() {
  SuplaTheme {
    PinSetupView(
      PinSetupViewState(
        pin = "1234",
        errorStringRes = R.string.pin_setup_entry_different,
        biometricStatus = BiometricUtils.AuthenticationStatus.NOT_ENROLLED
      )
    )
  }
}
