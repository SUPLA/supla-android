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

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.core.infrastructure.storage.DebugFileLoggingTree
import org.supla.android.core.infrastructure.storage.FileUtils
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.db.room.measurements.MeasurementsDatabase
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.db.MakeAnonymizedDatabaseCopyUseCase
import org.supla.android.usecases.developerinfo.LoadDatabaseDetailsUseCase
import org.supla.android.usecases.developerinfo.TableDetail
import org.supla.android.usecases.developerinfo.TableDetailType
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeveloperInfoViewModel @Inject constructor(
  private val makeAnonymizedDatabaseCopyUseCase: MakeAnonymizedDatabaseCopyUseCase,
  private val loadDatabaseDetailsUseCase: LoadDatabaseDetailsUseCase,
  private val applicationPreferences: ApplicationPreferences,
  private val encryptedPreferences: EncryptedPreferences,
  private val debugFileLoggingTree: DebugFileLoggingTree,
  private val notificationsHelper: NotificationsHelper,
  private val workManagerProxy: WorkManagerProxy,
  private val fileUtils: FileUtils,
  @param:ApplicationContext private val context: Context,
  suplaSchedulers: SuplaSchedulers
) : BaseViewModel<DeveloperInfoViewModelState, DeveloperInfoViewEvent>(DeveloperInfoViewModelState(), suplaSchedulers),
  DeveloperInfoScope {

  override fun onViewCreated() {
    super.onViewCreated()

    updateState {
      it.copy(
        state = it.state.copy(
          developerOptions = encryptedPreferences.devModeActive,
          rotationEnabled = applicationPreferences.rotationEnabled
        )
      )
    }
    setupLoggingCheckbox(encryptedPreferences.devLogActive)

    loadDatabaseDetailsUseCase(TableDetailType.SUPLA)
      .attach()
      .subscribeBy(
        onNext = this::handleSuplaData
      )
      .disposeBySelf()

    loadDatabaseDetailsUseCase(TableDetailType.MEASUREMENTS)
      .attach()
      .subscribeBy(
        onNext = this::handleMeasurementsData
      )
      .disposeBySelf()
  }

  private fun handleSuplaData(details: List<TableDetail>) {
    updateState {
      it.copy(state = it.state.copy(suplaTableDetails = details))
    }
  }

  private fun handleMeasurementsData(details: List<TableDetail>) {
    updateState {
      it.copy(state = it.state.copy(measurementTableDetails = details))
    }
  }

  override fun setDeveloperOptionEnabled(enabled: Boolean) {
    encryptedPreferences.devModeActive = enabled
    updateState { it.copy(state = it.state.copy(developerOptions = enabled)) }
  }

  override fun setRotationEnabled(enabled: Boolean) {
    applicationPreferences.rotationEnabled = enabled
    updateState { it.copy(state = it.state.copy(rotationEnabled = enabled)) }
    sendEvent(DeveloperInfoViewEvent.UpdateOrientationLock)
  }

  override fun setDebugLoggingEnabled(enabled: Boolean) {
    encryptedPreferences.devLogActive = enabled
    setupLoggingCheckbox(enabled)

    if (enabled) {
      Timber.plant(debugFileLoggingTree)
    } else {
      Timber.uproot(debugFileLoggingTree)
      debugFileLoggingTree.cleanup()
    }
  }

  override fun downloadLogFile() {
    sendEvent(DeveloperInfoViewEvent.ExportLogFile)
  }

  fun writeLogFile(uri: Uri?) {
    if (uri == null) {
      sendEvent(DeveloperInfoViewEvent.ExportCanceled)
      return
    }

    workManagerProxy.enqueueUniqueWork(
      ExportFileWorker.workId(ExportFileWorker.ExportType.LOG),
      ExistingWorkPolicy.KEEP,
      ExportFileWorker.build(debugFileLoggingTree.logFile, uri, ExportFileWorker.ExportType.LOG)
    )
  }

  override fun deleteLogFile() {
    if (debugFileLoggingTree.cleanup()) {
      sendEvent(DeveloperInfoViewEvent.LogFileRemoved)
      setupLoggingCheckbox(encryptedPreferences.devLogActive)
    } else {
      sendEvent(DeveloperInfoViewEvent.LogFileRemovalFailed)
    }
  }

  override fun refreshLogFileSize() {
    setupLoggingCheckbox(encryptedPreferences.devLogActive)
  }

  override fun sendTestNotification() {
    notificationsHelper.showNotification(context, "Test notification title", "Test notification message", "Test profile")
  }

  override fun exportSuplaDatabase() {
    viewModelScope.launch {
      val dbPrepared = withContext(Dispatchers.IO) { makeAnonymizedDatabaseCopyUseCase() }

      if (dbPrepared) {
        sendEvent(DeveloperInfoViewEvent.ExportSuplaDatabase)
      } else {
        sendEvent(DeveloperInfoViewEvent.SuplaExportNotPossible)
      }
    }
  }

  fun writeSuplaDatabaseFile(uri: Uri?) {
    if (uri == null) {
      sendEvent(DeveloperInfoViewEvent.ExportCanceled)
      return
    }

    val type = ExportFileWorker.ExportType.DATABASE
    workManagerProxy.enqueueUniqueWork(
      ExportFileWorker.workId(type),
      ExistingWorkPolicy.KEEP,
      ExportFileWorker.build(makeAnonymizedDatabaseCopyUseCase.file, uri, type)
    )
  }

  override fun exportMeasurementsDatabase() {
    sendEvent(DeveloperInfoViewEvent.ExportMeasurementsDatabase)
  }

  fun writeMeasurementsDatabaseFile(uri: Uri?) {
    if (uri == null) {
      sendEvent(DeveloperInfoViewEvent.ExportCanceled)
      return
    }

    val type = ExportFileWorker.ExportType.MEASUREMENTS_DATABASE
    workManagerProxy.enqueueUniqueWork(
      ExportFileWorker.workId(type),
      ExistingWorkPolicy.KEEP,
      ExportFileWorker.build(context.getDatabasePath(MeasurementsDatabase.NAME), uri, type)
    )
  }

  private fun setupLoggingCheckbox(enabled: Boolean) {
    if (enabled) {
      updateState {
        it.copy(
          state = it.state.copy(
            debugLoggingEnabled = true,
            debugLogSize = fileUtils.getFileSize(debugFileLoggingTree.logFile)
          )
        )
      }
    } else {
      updateState {
        it.copy(
          state = it.state.copy(
            debugLoggingEnabled = false,
            debugLogSize = null
          )
        )
      }
    }
  }
}

sealed class DeveloperInfoViewEvent : ViewEvent {
  data object UpdateOrientationLock : DeveloperInfoViewEvent()
  data object ExportSuplaDatabase : DeveloperInfoViewEvent()
  data object SuplaExportNotPossible : DeveloperInfoViewEvent()
  data object ExportMeasurementsDatabase : DeveloperInfoViewEvent()
  data object ExportLogFile : DeveloperInfoViewEvent()
  data object ExportCanceled : DeveloperInfoViewEvent()

  data object LogFileRemoved : DeveloperInfoViewEvent()
  data object LogFileRemovalFailed : DeveloperInfoViewEvent()
}

data class DeveloperInfoViewModelState(
  val state: DeveloperInfoViewState = DeveloperInfoViewState()
) : ViewState()
