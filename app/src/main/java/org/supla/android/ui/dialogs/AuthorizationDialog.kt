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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.PasswordTextField
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.TextField
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.core.shared.infrastructure.LocalizedString

data class AuthorizationDialogState(
  val userName: String,
  val isCloudAccount: Boolean,
  val userNameEnabled: Boolean,
  val error: LocalizedString? = null,
  val clarification: LocalizedString? = null,
  val processing: Boolean = false,
  val reason: AuthorizationReason = AuthorizationReason.Default
)

interface AuthorizationReason {
  data object Default : AuthorizationReason
}

interface AuthorizationDialogScope {
  fun onAuthorizationDismiss()
  fun onAuthorizationCancel()
  fun onAuthorize(userName: String, password: String)
  fun onStateChange(state: AuthorizationDialogState)
}

@Composable
fun AuthorizationDialogScope.AuthorizationDialog(
  state: AuthorizationDialogState
) {
  var password by rememberSaveable(state.userName) { mutableStateOf("") }
  var passwordVisible by rememberSaveable { mutableStateOf(false) }
  val autofillManager = LocalAutofillManager.current

  Dialog(onDismiss = { onAuthorizationDismiss() }) {
    DialogHeader(title = getDialogTitle(isCloudAccount = state.isCloudAccount))
    Separator(style = SeparatorStyle.LIGHT)

    state.clarification?.let {
      Text(
        text = it(LocalContext.current),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = Distance.small, top = Distance.small, end = Distance.small)
      )
    }

    UserNameTextField(
      state = state,
      enabled = state.userNameEnabled && state.processing.not(),
      onStateChange = { onStateChange(it) }
    )
    PasswordTextField(
      password = password,
      passwordVisible = passwordVisible,
      isError = state.error != null,
      enabled = state.processing.not(),
      onVisibilityChange = { passwordVisible = !passwordVisible },
      label = { Text(text = stringResource(id = R.string.password)) },
      onValueChange = { password = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = Distance.default, top = Distance.default, end = Distance.default)
    )
    ErrorText(text = state.error?.let { it(LocalContext.current) } ?: "")

    Separator(style = SeparatorStyle.LIGHT, modifier = Modifier.padding(top = Distance.small))
    DialogButtonsRow {
      OutlinedButton(
        onClick = {
          autofillManager?.cancel()
          onAuthorizationCancel()
        },
        text = stringResource(id = R.string.cancel),
        modifier = Modifier.weight(1f),
        enabled = state.processing.not()
      )

      if (state.processing) {
        Box(modifier = Modifier.weight(1f)) {
          CircularProgressIndicator(
            modifier = Modifier
              .align(Alignment.Center)
              .size(32.dp)
          )
        }
      } else {
        Button(
          onClick = {
            autofillManager?.commit()
            onAuthorize(state.userName, password)
          },
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
  TextField(
    value = state.userName,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = Distance.default, top = Distance.default, end = Distance.default)
      .semantics { contentType = ContentType.EmailAddress },
    label = { Text(text = stringResource(id = R.string.email)) },
    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
    enabled = enabled,
    singleLine = true,
    onValueChange = { onStateChange(state.copy(userName = it)) }
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

private val emptyScope = object : AuthorizationDialogScope {
  override fun onAuthorizationDismiss() {}
  override fun onAuthorizationCancel() {}
  override fun onAuthorize(userName: String, password: String) {}
  override fun onStateChange(state: AuthorizationDialogState) {}
}

@Preview
@Composable
private fun Preview_Normal() {
  SuplaTheme {
    emptyScope.AuthorizationDialog(
      AuthorizationDialogState(
        userName = "some_user@supla.org",
        isCloudAccount = true,
        userNameEnabled = true,
        clarification = LocalizedString.Constant("Simple clarification message which explains a reason of this dialog poped up")
      )
    )
  }
}

@Preview
@Composable
private fun Preview_Loading() {
  SuplaTheme {
    emptyScope.AuthorizationDialog(
      AuthorizationDialogState(
        userName = "some_user@supla.org",
        isCloudAccount = true,
        userNameEnabled = true,
        processing = true
      )
    )
  }
}
