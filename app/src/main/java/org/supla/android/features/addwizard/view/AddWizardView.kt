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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.features.addwizard.AddWizardViewModelState
import org.supla.android.features.addwizard.model.AddWizardScreen
import org.supla.android.features.addwizard.view.dialogs.ProvidePasswordDialog
import org.supla.android.features.addwizard.view.dialogs.ProvidePasswordScope
import org.supla.android.features.addwizard.view.dialogs.SetPasswordDialog
import org.supla.android.features.addwizard.view.dialogs.SetPasswordScope
import org.supla.android.features.addwizard.view.dialogs.WiFiListDialog
import org.supla.android.features.addwizard.view.dialogs.WiFiListDialogScope
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsColumn
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.dialogs.DialogMessage
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton

interface AddWizardScope :
  AddWizardWelcomeScope,
  AddWizardNetworkSelectionScope,
  AddWizardConfigurationScope,
  AddWizardMessageScope,
  AddWizardSuccessScope,
  WiFiListDialogScope,
  ProvidePasswordScope,
  SetPasswordScope {
  fun closeCloudDialog()
  fun openCloud()
  fun continueAfterManualReconnect()
}

@Composable
fun AddWizardScope.View(modelState: AddWizardViewModelState) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.primaryContainer)
  ) {
    when (val screen = modelState.screen) {
      AddWizardScreen.NetworkSelection -> AddWizardNetworkSelectionView(
        state = modelState.networkSelectionState ?: AddWizardNetworkSelectionState()
      )

      AddWizardScreen.Configuration ->
        AddWizardConfigurationView(
          processing = modelState.processing,
          progress = modelState.processingProgress,
          progressLabel = modelState.processingProgressLabel
        )

      AddWizardScreen.Welcome, null -> AddWizardWelcomeView()
      is AddWizardScreen.Message -> AddWizardMessageView(screen)
      is AddWizardScreen.Success -> AddWizardSuccessView(modelState.parameters)
    }

    modelState.scannerDialogState?.let { WiFiListDialog(it) }
    modelState.showCloudFollowupPopup.ifTrue { FollowupPopup() }
    modelState.setPasswordState?.let { SetPasswordDialog(it) }
    modelState.providePasswordState?.let { ProvidePasswordDialog(it) }
    modelState.canceling.ifTrue { LoadingScrim() }
    modelState.showReconnectDialog.ifTrue { ReconnectDialog() }
  }
}

@Composable
private fun AddWizardScope.FollowupPopup() =
  Dialog(onDismiss = {}) {
    DialogHeader(title = stringResource(R.string.add_device_needs_cloud_title))
    Separator(style = SeparatorStyle.LIGHT)
    DialogMessage(message = stringResource(R.string.add_device_needs_cloud_message))
    Separator(style = SeparatorStyle.LIGHT)
    DialogButtonsColumn {
      OutlinedButton(
        onClick = { closeCloudDialog() },
        text = stringResource(R.string.add_device_needs_cloud_dismiss),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )
      Button(
        onClick = { openCloud() },
        text = stringResource(R.string.add_device_needs_cloud_go),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )
    }
  }

@Composable
private fun AddWizardScope.ReconnectDialog() =
  AlertDialog(
    title = stringResource(R.string.wizard_state_finishing),
    message = stringResource(R.string.add_wizard_manual_reconnect),
    positiveButtonTitle = stringResource(R.string.next),
    negativeButtonTitle = null,
    onPositiveClick = { continueAfterManualReconnect() }
  )
