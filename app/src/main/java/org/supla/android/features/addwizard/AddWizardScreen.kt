package org.supla.android.features.addwizard
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

import android.Manifest
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.extensions.allGranted
import org.supla.android.extensions.applicationName
import org.supla.android.extensions.findActivity
import org.supla.android.features.addwizard.view.View
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.ViewModelHostBase
import org.supla.android.main.scaffold.LocalScaffoldPadding
import org.supla.android.main.scaffold.screenPaddings
import org.supla.android.main.view.BackOnlyTopBar
import org.supla.android.ui.dialogs.AuthorizationDialog

@Composable
fun AddWizardScreen(
  navigator: MainComposeNavigator,
  viewModel: AddWizardViewModel = hiltViewModel()
) {
  Scaffold(
    topBar = {
      BackOnlyTopBar { viewModel.onClose() }
    },
  ) { paddings ->
    CompositionLocalProvider(LocalScaffoldPadding provides paddings) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.primaryContainer)
          .screenPaddings()
      ) {
        Content(navigator, viewModel)
      }
    }
  }
}

@Composable
private fun Content(
  navigator: MainComposeNavigator,
  viewModel: AddWizardViewModel
) {
  val context = LocalContext.current
  val requestPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
      val allGranted = isGranted.values.fold(true) { acc, granted -> acc && granted }
      if (!allGranted) {
        viewModel.showMissingPermissionError(context.applicationName)
      } else {
        viewModel.registerSsidObserver()
      }
    }

  ViewModelHostBase(
    viewModel = viewModel,
    eventHandler = {
      handleEvent(
        event = it,
        navigator = navigator,
        checkPermissions = { checkPermissions(context, viewModel, requestPermissionLauncher) }
      )
    }
  ) { viewState ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
      BackHandler(enabled = viewState.customBackEnabled) {
        viewModel.onBackPressed()
      }

      viewState.authorizationDialogState?.let {
        viewModel.AuthorizationDialog(state = it)
      }

      viewModel.View(viewState)
    }
  }
}

private fun handleEvent(event: AddWizardViewEvent, navigator: MainComposeNavigator, checkPermissions: () -> Unit) {
  when (event) {
    is AddWizardViewEvent.Close -> if (event.clientWorking) navigator.back() else navigator.replaceTop(MainRoute.Status)
    AddWizardViewEvent.CheckPermissions -> checkPermissions()
    AddWizardViewEvent.OpenCloud -> navigator.navigateToCloudExternal()
  }
}

private fun checkPermissions(
  context: Context,
  viewModel: AddWizardViewModel,
  requestPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>
) {
  val permissions = listOf(
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.CHANGE_NETWORK_STATE
  )

  if (context.allGranted(permissions)) {
    viewModel.registerSsidObserver()
    return
  }

  val activity = context.findActivity()
  val anyPermissionRevoked = permissions.fold(false) { acc, permission ->
    acc || (activity?.shouldShowRequestPermissionRationale(permission) == true)
  }

  if (anyPermissionRevoked) {
    viewModel.showMissingPermissionError(context.applicationName)
    return
  }

  requestPermissionLauncher.launch(permissions.subList(0, 4).toTypedArray())
}
