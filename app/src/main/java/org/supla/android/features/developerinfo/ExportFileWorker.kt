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
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.supla.android.core.infrastructure.storage.FileExporter
import org.supla.android.core.notifications.NotificationId
import org.supla.android.core.notifications.NotificationsHelper
import timber.log.Timber
import java.io.File

private const val KEY_SOURCE_FILE_PATH = "KEY_SOURCE_FILE_PATH"
private const val KEY_DESTINATION_FILE_URI = "KEY_DESTINATION_FILE_URI"
private const val KEY_EXPORT_TYPE = "KEY_EXPORT_TYPE"

@HiltWorker
class ExportFileWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  private val notificationsHelper: NotificationsHelper
) : CoroutineWorker(appContext, workerParameters) {
  override suspend fun doWork(): Result {
    val sourceFile = inputData.getString(KEY_SOURCE_FILE_PATH)
    val destinationUri = inputData.getString(KEY_DESTINATION_FILE_URI)
    val exportType = inputData.getString(KEY_EXPORT_TYPE)

    if (sourceFile == null || destinationUri == null || exportType == null) {
      Timber.w("Invalid input data")
      return Result.failure()
    }

    val file = File(sourceFile)
    val uri = Uri.parse(destinationUri)
    val type = ExportType.valueOf(exportType)

    showNotification(type)

    val result = withContext(Dispatchers.IO) {
      FileExporter.copyFileToUri(applicationContext, file, uri)
    }

    if (result) {
      showNotification(type, "Finished successfully")
    } else {
      showNotification(type, "Export failed!")
    }

    return Result.success()
  }

  private fun getNotificationTitle(type: ExportType) =
    when (type) {
      ExportType.DATABASE -> "Supla Database Export"
      ExportType.MEASUREMENTS_DATABASE -> "Measurements Database Export"
      ExportType.LOG -> "Log file export"
    }

  private fun showNotification(type: ExportType, text: String? = null) {
    notificationsHelper.showBackgroundNotification(applicationContext, getNotificationTitle(type), text, type.notificationId.value)
  }

  companion object {
    private val WORK_ID: String = ExportFileWorker::class.java.simpleName

    fun workId(type: ExportType): String =
      "$WORK_ID: ${type.name}"

    fun build(sourceFile: File, destinationUri: Uri, type: ExportType): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<ExportFileWorker>()
        .setInputData(
          workDataOf(
            KEY_SOURCE_FILE_PATH to sourceFile.absolutePath,
            KEY_DESTINATION_FILE_URI to destinationUri.toString(),
            KEY_EXPORT_TYPE to type.toString()
          )
        )
        .build()
  }

  enum class ExportType(val notificationId: NotificationId) {
    DATABASE(NotificationId.EXPORT_SUPLA_DATABASE),
    MEASUREMENTS_DATABASE(NotificationId.EXPORT_MEASUREMENTS_DATABASE),
    LOG(NotificationId.EXPORT_LOG_FILE)
  }
}
