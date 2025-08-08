package org.supla.android.features.addwizard.view.dialogs
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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.addwizard.view.components.InstructionText
import org.supla.android.features.addwizard.view.components.SsidText
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogDoubleButtons
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.views.PasswordTextField
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle

data class SetPasswordState(
  val error: Boolean = false,
  val processing: Boolean = false,
  val ssid: String? = null
)

interface SetPasswordScope {
  fun closeSetPasswordDialog()
  fun onPasswordSet(password: String, repeatPassword: String)
}

@Composable
fun SetPasswordScope.SetPasswordDialog(state: SetPasswordState) {
  Dialog(onDismiss = {}) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var repeatPassword by remember { mutableStateOf("") }
    var repeatPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val autofillManager = LocalAutofillManager.current

    DialogHeader(stringResource(R.string.add_wizard_password_set_title))
    Separator(style = SeparatorStyle.LIGHT)
    SsidText(state.ssid)
    PasswordTextField(
      password = password,
      passwordVisible = passwordVisible,
      isError = state.error,
      onVisibilityChange = { passwordVisible = !passwordVisible },
      label = { Text(text = stringResource(id = R.string.add_wizard_password_new_label).uppercase()) },
      onValueChange = { password = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = Distance.default, top = Distance.default, end = Distance.default)
        .semantics { contentType = ContentType.NewPassword }
    )
    PasswordTextField(
      password = repeatPassword,
      passwordVisible = repeatPasswordVisible,
      isError = state.error,
      onVisibilityChange = { repeatPasswordVisible = !repeatPasswordVisible },
      label = { Text(text = stringResource(R.string.add_wizard_password_repeat_label).uppercase()) },
      onValueChange = { repeatPassword = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = Distance.default, top = Distance.default, end = Distance.default)
        .semantics { contentType = ContentType.NewPassword }
    )

    InstructionText(
      instructionRes = R.string.add_wizard_password_rules,
      color = if (state.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Separator(style = SeparatorStyle.LIGHT)

    DialogDoubleButtons(
      onNegativeClick = {
        autofillManager?.cancel()
        closeSetPasswordDialog()
      },
      onPositiveClick = {
        autofillManager?.commit()
        onPasswordSet(password, repeatPassword)
      },
      processing = state.processing,
      positiveEnabled = password.isNotEmpty() && password == repeatPassword,
      positiveTextRes = R.string.save
    )
  }
}

private val previewScope = object : SetPasswordScope {
  override fun closeSetPasswordDialog() {}
  override fun onPasswordSet(password: String, repeatPassword: String) {}
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.SetPasswordDialog(
      state = SetPasswordState(ssid = "SUPLA-EXAMPLE-78218479E27C")
    )
  }
}
