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
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.infrastructure.storage.DebugFileLoggingTree
import org.supla.android.core.infrastructure.storage.FileExporter
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.db.DbHelper
import org.supla.android.db.room.measurements.MeasurementsDatabase
import org.supla.android.extensions.setupOrientationLock
import org.supla.android.navigator.MainNavigator
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DeveloperInfoFragment : BaseFragment<DeveloperInfoViewModelState, DeveloperInfoViewEvent>(R.layout.fragment_compose) {
  override val viewModel: DeveloperInfoViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  @Inject
  internal lateinit var navigator: MainNavigator

  @Inject
  internal lateinit var applicationPreferences: ApplicationPreferences

  @Inject
  internal lateinit var debugFileLoggingTree: DebugFileLoggingTree

  private val exportSuplaDbLauncher = registerForActivityResult(
    ActivityResultContracts.CreateDocument("application/octet-stream")
  ) { uri: Uri? -> performDatabaseExport(uri, DbHelper.DATABASE_NAME) }

  private val exportMeasurementsDbLauncher = registerForActivityResult(
    ActivityResultContracts.CreateDocument("application/octet-stream")
  ) { uri: Uri? -> performDatabaseExport(uri, MeasurementsDatabase.NAME) }

  private val exportLogFileLauncher = registerForActivityResult(
    ActivityResultContracts.CreateDocument("application/octet-stream")
  ) { uri: Uri? -> performFileExport(uri, debugFileLoggingTree.logFile) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      val modelState by viewModel.getViewState().collectAsState()
      SuplaTheme {
        viewModel.View(
          viewState = modelState.state
        )
      }
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
    }
  }

  override fun handleViewState(state: DeveloperInfoViewModelState) {}

  private fun performDatabaseExport(uri: Uri?, databaseName: String) {
    if (uri != null) {
      if (FileExporter.copyDatabaseToUri(requireContext(), databaseName, uri)) {
        Toast.makeText(requireContext(), "Database successfully exported.", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(requireContext(), "Export failed!", Toast.LENGTH_LONG).show()
      }
    } else {
      // User cancelled the file picker.
      Toast.makeText(requireContext(), "Database export cancelled.", Toast.LENGTH_SHORT).show()
    }
  }

  private fun performFileExport(uri: Uri?, file: File) {
    if (uri != null) {
      if (FileExporter.copyFileToUri(requireContext(), file, uri)) {
        Toast.makeText(requireContext(), "File successfully exported.", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(requireContext(), "Export failed!", Toast.LENGTH_LONG).show()
      }
    } else {
      // User cancelled the file picker.
      Toast.makeText(requireContext(), "File export cancelled.", Toast.LENGTH_SHORT).show()
    }
  }
}
