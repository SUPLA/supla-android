package org.supla.android.features.developerinfo
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

import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.core.infrastructure.storage.DebugFileLoggingTree
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.setupOrientationLock
import org.supla.android.navigator.MainNavigator
import javax.inject.Inject

@AndroidEntryPoint
class DeveloperInfoFragment : BaseComposeFragment<DeveloperInfoViewModelState, DeveloperInfoViewEvent>() {
  override val viewModel: DeveloperInfoViewModel by viewModels()

  @Inject
  internal lateinit var navigator: MainNavigator

  @Inject
  internal lateinit var applicationPreferences: ApplicationPreferences

  private val exportSuplaDbLauncher = registerForActivityResult(
    ActivityResultContracts.CreateDocument("application/octet-stream")
  ) { uri: Uri? -> viewModel.writeSuplaDatabaseFile(uri) }

  private val exportMeasurementsDbLauncher = registerForActivityResult(
    ActivityResultContracts.CreateDocument("application/octet-stream")
  ) { uri: Uri? -> viewModel.writeMeasurementsDatabaseFile(uri) }

  private val exportLogFileLauncher = registerForActivityResult(
    ActivityResultContracts.CreateDocument("application/octet-stream")
  ) { uri: Uri? -> viewModel.writeLogFile(uri) }

  @Composable
  override fun ComposableContent(modelState: DeveloperInfoViewModelState) {
    SuplaTheme {
      viewModel.View(
        viewState = modelState.state
      )
    }
  }

  override fun handleEvents(event: DeveloperInfoViewEvent) {
    when (event) {
      DeveloperInfoViewEvent.UpdateOrientationLock ->
        activity?.setupOrientationLock(applicationPreferences)

      DeveloperInfoViewEvent.ExportSuplaDatabase ->
        exportSuplaDbLauncher.launch("supla-${System.currentTimeMillis()}.db")

      DeveloperInfoViewEvent.ExportMeasurementsDatabase ->
        exportMeasurementsDbLauncher.launch("measurements-${System.currentTimeMillis()}.db")

      DeveloperInfoViewEvent.ExportLogFile ->
        exportLogFileLauncher.launch(DebugFileLoggingTree.FILE_NAME)

      DeveloperInfoViewEvent.LogFileRemovalFailed ->
        Toast.makeText(requireContext(), "Log file removal failed!", Toast.LENGTH_SHORT).show()

      DeveloperInfoViewEvent.LogFileRemoved ->
        Toast.makeText(requireContext(), "Log file removed", Toast.LENGTH_SHORT).show()

      DeveloperInfoViewEvent.SuplaExportNotPossible ->
        Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show()

      DeveloperInfoViewEvent.ExportCanceled ->
        Toast.makeText(requireContext(), "File export cancelled.", Toast.LENGTH_SHORT).show()
    }
  }

  override fun handleViewState(state: DeveloperInfoViewModelState) {}
}
