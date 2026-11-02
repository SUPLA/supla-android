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

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.core.infrastructure.storage.DebugFileLoggingTree
import org.supla.android.core.storage.LocalApplicationPreferences
import org.supla.android.extensions.findActivity
import org.supla.android.extensions.setupOrientationLock
import org.supla.android.main.ViewModelHost

@Composable
fun DeveloperInfoScreen(
  viewModel: DeveloperInfoViewModel = hiltViewModel()
) {
  val exportSuplaDbLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {
      viewModel.writeSuplaDatabaseFile(it)
    }
  val exportMeasurementsDbLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {
      viewModel.writeMeasurementsDatabaseFile(it)
    }
  val exportLogFileLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {
      viewModel.writeLogFile(it)
    }
  val context = LocalContext.current
  val activity = context.findActivity()
  val preferences = LocalApplicationPreferences.current

  ViewModelHost(
    viewModel = viewModel,
    eventHandler = {
      when (it) {
        DeveloperInfoViewEvent.ExportCanceled -> Toast.makeText(context, "File export cancelled.", Toast.LENGTH_SHORT).show()
        DeveloperInfoViewEvent.ExportLogFile -> exportLogFileLauncher.launch(DebugFileLoggingTree.FILE_NAME)
        DeveloperInfoViewEvent.ExportMeasurementsDatabase ->
          exportMeasurementsDbLauncher.launch("measurements-${System.currentTimeMillis()}.db")
        DeveloperInfoViewEvent.ExportSuplaDatabase -> exportSuplaDbLauncher.launch("supla-${System.currentTimeMillis()}.db")
        DeveloperInfoViewEvent.LogFileRemovalFailed -> Toast.makeText(context, "Log file removal failed!", Toast.LENGTH_SHORT).show()
        DeveloperInfoViewEvent.LogFileRemoved -> Toast.makeText(context, "Log file removed", Toast.LENGTH_SHORT).show()
        DeveloperInfoViewEvent.SuplaExportNotPossible -> Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
        DeveloperInfoViewEvent.UpdateOrientationLock -> activity?.setupOrientationLock(preferences)
      }
    }
  ) {
    viewModel.View(it.state)
  }
}
