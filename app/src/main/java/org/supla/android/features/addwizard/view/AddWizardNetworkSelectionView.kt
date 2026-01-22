package org.supla.android.features.addwizard.view
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiFind
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.addwizard.AddWizardScope
import org.supla.android.features.addwizard.model.AddWizardScreen
import org.supla.android.features.addwizard.view.components.AddWizardContentText
import org.supla.android.features.addwizard.view.components.AddWizardScaffold
import org.supla.android.features.addwizard.view.components.AddWizardTextFieldContainer
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsColumn
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.forms.Checkbox
import org.supla.android.ui.views.forms.PasswordTextField
import org.supla.android.ui.views.forms.TextField

data class AddWizardNetworkSelectionState(
  val networkName: String = "",
  val networkPassword: String = "",
  val networkPasswordVisible: Boolean = false,
  val rememberPassword: Boolean = false,
  val error: Boolean = false,
  val showSpacesPopup: Boolean = false
) {
  val networkNameCanTrim: Boolean
    get() = networkName != networkName.trim()
}

interface AddWizardNetworkSelectionScope : AddWizardScope {
  fun onNetworkNameChanged(name: String)
  fun onNetworkPasswordChanged(password: String)
  fun onNetworkPasswordVisibilityChanged()
  fun onNetworkRememberPasswordChanged(remember: Boolean)
  fun onNetworkScanClicked()
  fun onRemoveSpaces()
}

@Composable
fun AddWizardNetworkSelectionScope.AddWizardNetworkSelectionView(
  state: AddWizardNetworkSelectionState
) {
  val autofillManager = LocalAutofillManager.current

  AddWizardScaffold(
    iconRes = R.drawable.add_wizard_step_2,
    buttonTextId = R.string.next,
    onNext = {
      autofillManager?.commit()
      onStepFinished(AddWizardScreen.NetworkSelection)
    }
  ) {
    AddWizardContentText(R.string.add_wizard_step_2_message)

    val (passwordFocusRequester) = FocusRequester.createRefs()

    AddWizardTextFieldContainer(R.string.add_wizard_network_name) {
      TextField(
        value = state.networkName,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = { onNetworkNameChanged(it) },
        isError = state.error,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions { passwordFocusRequester.requestFocus() }
      )
    }

    AddWizardTextFieldContainer(R.string.password) {
      PasswordTextField(
        password = state.networkPassword,
        passwordVisible = state.networkPasswordVisible,
        onVisibilityChange = { onNetworkPasswordVisibilityChanged() },
        onValueChange = { onNetworkPasswordChanged(it) },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(passwordFocusRequester)
          .semantics { contentType = ContentType.Password },
        isError = state.error,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions { onStepFinished(AddWizardScreen.NetworkSelection) }
      )
      Checkbox(
        checked = state.rememberPassword,
        label = stringResource(R.string.add_wizard_remember_passwd),
        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        checkmarkColor = MaterialTheme.colorScheme.primary,
        onCheckedChange = { onNetworkRememberPasswordChanged(it) }
      )
    }

    NetworkSearchButton { onNetworkScanClicked() }
  }

  if (state.showSpacesPopup) {
    SpaceAlertDialog(
      networkName = state.networkName,
      onAccept = { onStepFinished(AddWizardScreen.NetworkSelection) },
      onModify = { onRemoveSpaces() }
    )
  }
}

@Composable
private fun NetworkSearchButton(onClick: () -> Unit) =
  TextButton(
    onClick = onClick
  ) {
    Image(
      imageVector = Icons.Outlined.WifiFind,
      contentDescription = null,
      colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
    )
    Spacer(modifier = Modifier.width(Distance.small))
    Text(
      text = stringResource(R.string.add_wizard_step_2_search_network),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )
  }

@Composable
private fun SpaceAlertDialog(
  networkName: String,
  onAccept: () -> Unit = {},
  onModify: () -> Unit = {}
) {
  Dialog(onDismiss = {}) {
    DialogHeader(title = stringResource(R.string.add_wizard_spaces_title))
    Separator(style = SeparatorStyle.LIGHT)
    SpaceAlertMessage(networkName)
    Separator(style = SeparatorStyle.LIGHT)
    DialogButtonsColumn {
      OutlinedButton(
        onClick = onAccept,
        text = stringResource(R.string.add_wizard_spaces_accept),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )
      Button(
        onClick = onModify,
        text = stringResource(R.string.add_wizard_spaces_modify),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )
    }
  }
}

@Composable
private fun SpaceAlertMessage(networkName: String) {
  val message = stringResource(R.string.add_wizard_spaces_message, networkName)
  val networkNameTrimmed = networkName.trim()
  val startIndex = message.indexOf(networkName)
  val startIndexTrimmed = message.indexOf(networkNameTrimmed)

  if (startIndex == -1) {
    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .padding(all = Distance.default)
        .fillMaxWidth()
    )
  } else {
    val endIndex = startIndex + networkName.length
    val endIndexTrimmed = startIndexTrimmed + networkNameTrimmed.length

    val annotatedString = buildAnnotatedString {
      append(message.substring(0, startIndex))

      if (startIndex != startIndexTrimmed) {
        withStyle(
          style = SpanStyle(
            background = MaterialTheme.colorScheme.error
          )
        ) {
          append(message.substring(startIndex, startIndexTrimmed))
        }
      }

      append(message.substring(startIndexTrimmed, endIndexTrimmed))

      if (endIndex != endIndexTrimmed) {
        withStyle(
          style = SpanStyle(
            background = MaterialTheme.colorScheme.error
          )
        ) {
          append(message.substring(endIndexTrimmed, endIndex))
        }
      }

      append(message.substring(endIndex))
    }
    Text(
      text = annotatedString,
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .padding(all = Distance.default)
        .fillMaxWidth()
    )
  }
}

private val previewScope = object : AddWizardNetworkSelectionScope {
  override fun onNetworkNameChanged(name: String) {}
  override fun onNetworkPasswordChanged(password: String) {}
  override fun onNetworkPasswordVisibilityChanged() {}
  override fun onNetworkRememberPasswordChanged(remember: Boolean) {}
  override fun onNetworkScanClicked() {}
  override fun onRemoveSpaces() {}

  override fun onStepFinished(step: AddWizardScreen) {}
  override fun onClose(step: AddWizardScreen) {}
}

@Preview(backgroundColor = 0xFF12A71E, showBackground = true)
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.AddWizardNetworkSelectionView(AddWizardNetworkSelectionState("test", "test", true, false))
  }
}

@Preview(backgroundColor = 0xFF12A71E, showBackground = true)
@Composable
private fun Preview_Error() {
  SuplaTheme {
    previewScope.AddWizardNetworkSelectionView(AddWizardNetworkSelectionState("", "", false, true))
  }
}
