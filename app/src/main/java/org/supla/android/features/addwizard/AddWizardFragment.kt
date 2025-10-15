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
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.viewModels
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.UpHandler
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.allGranted
import org.supla.android.extensions.applicationName
import org.supla.android.features.addwizard.view.View
import org.supla.android.navigator.MainNavigator
import org.supla.android.navigator.NavigationSubcontroller
import org.supla.android.ui.ToolbarVisibilityController
import org.supla.android.ui.dialogs.AuthorizationDialog
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AddWizardFragment : BaseComposeFragment<AddWizardViewModelState, AddWizardViewEvent>(), NavigationSubcontroller, UpHandler {

  override val viewModel: AddWizardViewModel by viewModels()

  private val onBackPressedDispatcher = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
      viewModel.onBackPressed()
    }
  }

  @Inject
  lateinit var navigator: MainNavigator

  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
      val allGranted = isGranted.values.fold(true) { acc, granted -> acc && granted }
      if (!allGranted) {
        viewModel.showMissingPermissionError(requireContext().applicationName)
      } else {
        viewModel.registerSsidObserver()
      }
    }

  private val barCodeScanner: GmsBarcodeScanner by lazy {
    val options = GmsBarcodeScannerOptions.Builder()
      .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
      .enableAutoZoom()
      .build()

    GmsBarcodeScanning.getClient(requireContext(), options)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedDispatcher)
  }

  @Composable
  override fun ComposableContent(modelState: AddWizardViewModelState) {
    SuplaTheme {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.primaryContainer)
      ) {
        viewModel.View(modelState)
        modelState.authorizationDialogState?.let { viewModel.AuthorizationDialog(it) }
      }
    }
  }

  override fun handleEvents(event: AddWizardViewEvent) {
    when (event) {
      is AddWizardViewEvent.Close -> if (event.clientWorking) navigator.back() else navigator.forcePopToStatus()
      AddWizardViewEvent.OpenScanner ->
        barCodeScanner.startScan()
          .addOnSuccessListener {
            Timber.i("Barcode scanner success: ${it.rawValue}")
            Toast.makeText(requireContext(), R.string.add_wizard_wrong_qr_code, Toast.LENGTH_LONG).show()
          }
          .addOnCanceledListener { Timber.i("Barcode scanner canceled") }
          .addOnFailureListener { Timber.i("Barcode scanner failure") }

      AddWizardViewEvent.CheckPermissions -> checkPermissions()
      AddWizardViewEvent.OpenCloud -> navigator.navigateToCloudExternal()
    }
  }

  override fun handleViewState(state: AddWizardViewModelState) {
    super.handleViewState(state)
    onBackPressedDispatcher.isEnabled = state.customBackEnabled
  }

  override fun getToolbarVisibility(): ToolbarVisibilityController.ToolbarVisibility =
    ToolbarVisibilityController.ToolbarVisibility(
      visible = true,
      toolbarColorRes = R.color.primary_container,
      navigationBarColorRes = R.color.primary_container,
      shadowVisible = false,
      isLight = false
    )

  private fun checkPermissions() {
    val permissions = listOf(
      Manifest.permission.ACCESS_NETWORK_STATE,
      Manifest.permission.ACCESS_WIFI_STATE,
      Manifest.permission.CHANGE_WIFI_STATE,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.CHANGE_NETWORK_STATE
    )
    if (requireContext().allGranted(permissions)) {
      viewModel.registerSsidObserver()
      return
    }
    if (permissions.anyPermissionRevoked) {
      viewModel.showMissingPermissionError(requireContext().applicationName)
      return
    }

    requestPermissionLauncher.launch(permissions.subList(0, 4).toTypedArray())
  }

  private val List<String>.anyPermissionRevoked: Boolean
    get() = fold(false) { acc, permission ->
      acc || shouldShowRequestPermissionRationale(permission)
    }

  override fun screenTakeoverAllowed(): Boolean = false

  override fun onUpPressed(): Boolean =
    viewState.screen?.let {
      viewModel.onClose(it)
      true
    } ?: false
}
