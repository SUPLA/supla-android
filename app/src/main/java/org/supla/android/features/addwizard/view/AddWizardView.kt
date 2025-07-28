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
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.views.LoadingScrim

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
  }
}

@Composable
private fun AddWizardScope.FollowupPopup() =
  AlertDialog(
    title = stringResource(R.string.add_device_needs_cloud_title),
    message = stringResource(R.string.add_device_needs_cloud_message),
    negativeButtonTitle = stringResource(R.string.add_device_needs_cloud_dismiss),
    positiveButtonTitle = stringResource(R.string.add_device_needs_cloud_go),
    onPositiveClick = { openCloud() },
    onNegativeClick = { closeCloudDialog() }
  )
