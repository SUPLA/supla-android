package org.supla.android.ui.dialogs
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.TextField
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton

data class AuthorizationDialogState(
  val userName: String,
  val isCloudAccount: Boolean,
  val userNameEnabled: Boolean,
  val error: StringProvider? = null,
  val processing: Boolean = false
)

@Composable
fun AuthorizationDialog(
  dialogState: AuthorizationDialogState,
  onDismiss: () -> Unit = {},
  onCancel: () -> Unit = {},
  onAuthorize: (userName: String, password: String) -> Unit = { _, _ -> },
  onStateChange: (AuthorizationDialogState) -> Unit = { }
) {
  var password by rememberSaveable(dialogState.userName) { mutableStateOf("") }
  var passwordVisible by rememberSaveable { mutableStateOf(false) }

  Dialog(onDismiss = onDismiss) {
    DialogHeader(title = getDialogTitle(isCloudAccount = dialogState.isCloudAccount))
    Separator(style = SeparatorStyle.LIGHT)

    UserNameTextField(
      state = dialogState,
      enabled = dialogState.userNameEnabled && dialogState.processing.not(),
      onStateChange = onStateChange
    )
    PasswordTextField(
      password = password,
      passwordVisible = passwordVisible,
      isError = dialogState.error != null,
      enabled = dialogState.processing.not(),
      onVisibilityChange = { passwordVisible = !passwordVisible },
      onValueChange = { password = it }
    )
    ErrorText(text = dialogState.error?.let { it(LocalContext.current) } ?: "")

    Separator(style = SeparatorStyle.LIGHT, modifier = Modifier.padding(top = Distance.default))
    DialogButtonsRow {
      OutlinedButton(
        onClick = onCancel,
        text = stringResource(id = R.string.cancel),
        modifier = Modifier.weight(1f),
        enabled = dialogState.processing.not()
      )

      if (dialogState.processing) {
        Box(modifier = Modifier.weight(1f)) {
          CircularProgressIndicator(
            modifier = Modifier
              .align(Alignment.Center)
              .size(32.dp)
          )
        }
      } else {
        Button(
          onClick = { onAuthorize(dialogState.userName, password) },
          text = stringResource(id = R.string.ok),
          enabled = password.isNotEmpty(),
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun getDialogTitle(isCloudAccount: Boolean) =
  if (isCloudAccount) {
    stringResource(id = R.string.enter_suplaorg_credentails)
  } else {
    stringResource(id = R.string.enter_superuser_credentials)
  }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UserNameTextField(
  state: AuthorizationDialogState,
  enabled: Boolean,
  onStateChange: (AuthorizationDialogState) -> Unit = { }
) {
  val emailAutofill = AutofillNode(
    autofillTypes = listOf(AutofillType.EmailAddress),
    onFill = { onStateChange(state.copy(userName = it)) }
  )
  val autofill = LocalAutofill.current

  LocalAutofillTree.current += emailAutofill

  TextField(
    value = state.userName,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = Distance.default, top = Distance.default, end = Distance.default)
      .onGloballyPositioned {
        emailAutofill.boundingBox = it.boundsInWindow()
      }
      .onFocusChanged {
        autofill?.run {
          if (it.isFocused) {
            requestAutofillForNode(emailAutofill)
          } else {
            cancelAutofillForNode(emailAutofill)
          }
        }
      },
    label = { Text(text = stringResource(id = R.string.email)) },
    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
    enabled = enabled,
    singleLine = true,
    onValueChange = { onStateChange(state.copy(userName = it)) }
  )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PasswordTextField(
  password: String,
  passwordVisible: Boolean,
  isError: Boolean,
  enabled: Boolean,
  onVisibilityChange: () -> Unit,
  onValueChange: (String) -> Unit = { }
) {
  val emailAutofill = AutofillNode(
    autofillTypes = listOf(AutofillType.Password),
    onFill = { onValueChange(it) }
  )
  val autofill = LocalAutofill.current

  LocalAutofillTree.current += emailAutofill
  TextField(
    value = password,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = Distance.default, top = Distance.default, end = Distance.default)
      .onGloballyPositioned {
        emailAutofill.boundingBox = it.boundsInWindow()
      }
      .onFocusChanged {
        autofill?.run {
          if (it.isFocused) {
            requestAutofillForNode(emailAutofill)
          } else {
            cancelAutofillForNode(emailAutofill)
          }
        }
      },
    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
    label = { Text(text = stringResource(id = R.string.password)) },
    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
    singleLine = true,
    onValueChange = onValueChange,
    isError = isError,
    enabled = enabled,
    trailingIcon = {
      IconButton(onClick = onVisibilityChange) {
        Icon(
          imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
          contentDescription = null
        )
      }
    }
  )
}

@Composable
private fun ErrorText(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.error,
    modifier = Modifier.padding(
      start = Distance.default.plus(Distance.small),
      end = Distance.default.plus(Distance.small),
      top = 4.dp
    )
  )

@Preview
@Composable
private fun Preview_Normal() {
  SuplaTheme {
    AuthorizationDialog(
      AuthorizationDialogState(
        userName = "some_user@supla.org",
        isCloudAccount = true,
        userNameEnabled = true
      )
    )
  }
}

@Preview
@Composable
private fun Preview_Loading() {
  SuplaTheme {
    AuthorizationDialog(
      AuthorizationDialogState(
        userName = "some_user@supla.org",
        isCloudAccount = true,
        userNameEnabled = true,
        processing = true
      )
    )
  }
}
