package org.supla.android.widget.shared
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

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.supla.android.R
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.IGNORE_CCT
import org.supla.android.lib.actions.RgbwActionParameters
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.tools.VibrationHelper
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetPreferences
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import timber.log.Timber

private const val INTERNAL_ERROR = -10
private const val ARG_WIDGET_ACTION = "ARG_WIDGET_ACTION"

abstract class WidgetCommandWorkerBase(
  private val widgetConfigurationUpdater: WidgetConfigurationUpdater,
  private val notificationsHelper: NotificationsHelper,
  private val singleCallProvider: SingleCall.Provider,
  private val widgetPreferences: WidgetPreferences,
  private val vibrationHelper: VibrationHelper,
  appContext: Context,
  workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

  private val handler = Handler(Looper.getMainLooper())

  protected abstract val notificationId: Int

  override fun doWork(): Result {
    Timber.i("Widget worker started")

    val widgetIds: IntArray? = inputData.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
    if (widgetIds == null) {
      Timber.e("No widget ids to update!")
      return Result.failure()
    }

    val widgetAction: WidgetAction? = WidgetAction.from(inputData.getString(ARG_WIDGET_ACTION))
    if (widgetAction == null) {
      Timber.e("No widget action!")
      return Result.failure()
    }

    return if (widgetAction.isUpdate) {
      Timber.i("Performing update for widgets with ids: ${widgetIds.toReadableString()}")
      performUpdate(widgetIds, widgetAction == WidgetAction.MANUAL_UPDATE)
    } else {
      Timber.i("Performing widget action `$widgetAction` for ids: ${widgetIds.toReadableString()}")
      performAction(widgetIds, widgetAction)
    }
  }

  protected abstract fun sendWidgetRedrawAction(widgetId: Int)
  protected abstract fun valueWithUnit(): Boolean

  private fun performUpdate(widgetIds: IntArray, isManualUpdate: Boolean): Result {
    if (!isNetworkAvailable()) {
      Timber.d("Widget update skipped because of unavailable network (widgetIds: $widgetIds)")
      if (isManualUpdate) {
        showToastLong(R.string.widget_command_no_connection)
      }
      return Result.failure()
    }

    createForegroundInfo()

    var result: WorkResult = WorkResult.Success
    for (widgetId in widgetIds) {
      val configuration = widgetPreferences.getWidgetConfiguration(widgetId)
      if (configuration == null) {
        Timber.w("Trying to refresh widget without configuration!")
        continue
      }
      if (!configuration.subjectFunction.isValueWidget) {
        continue // Skip widgets where no update is needed
      }

      Timber.i("Performing widget configuration update for id: $widgetId")
      val updateResult = widgetConfigurationUpdater.update(configuration, valueWithUnit())

      updateResult.whenFailure { cleanConfiguration, errorResult ->
        Timber.w("Widget refresh failed with error: $updateResult")
        isManualUpdate.ifTrue { handleUpdateResult(errorResult.result, configuration) }
        cleanConfiguration.ifTrue {
          Timber.w("Cleaning widget configuration")
          updateWidgetConfiguration(widgetId, configuration.copy(value = NO_VALUE_TEXT))
        }
      }
      updateResult.whenSuccess {
        Timber.w("Widget refreshed successfully")
        updateWidgetConfiguration(widgetId, it)
      }

      result = result.accumulate(updateResult.toWorkResult)
      sendWidgetRedrawAction(widgetId)
    }

    if (result is WorkResult.Success && isManualUpdate) {
      vibrationHelper.vibrate()
    }

    Timber.i("Widget update finished with result: $result")
    return result.asWorkerResult
  }

  private fun performAction(widgetIds: IntArray, widgetAction: WidgetAction): Result {
    if (widgetIds.size != 1) {
      showToast(
        applicationContext.resources.getString(
          R.string.widget_command_error,
          INTERNAL_ERROR
        ),
        Toast.LENGTH_LONG
      )
      return Result.failure()
    }
    val widgetId = widgetIds[0]

    val configuration = widgetPreferences.getWidgetConfiguration(widgetId)
    if (configuration == null) {
      showToast(
        applicationContext.resources.getString(
          R.string.widget_command_error,
          INTERNAL_ERROR
        ),
        Toast.LENGTH_LONG
      )
      return Result.failure()
    }
    setForegroundAsync(createForegroundInfo(configuration.caption))

    if (!isNetworkAvailable()) {
      showToastLong(R.string.widget_command_no_connection)
      return Result.failure()
    }

    vibrationHelper.vibrate()

    return performAction(widgetAction, configuration)
  }

  private fun performAction(
    widgetAction: WidgetAction,
    configuration: WidgetConfiguration,
  ): Result {
    val actionId: ActionId? = when (widgetAction) {
      WidgetAction.MANUAL_UPDATE, WidgetAction.REDRAW, WidgetAction.AUTOMATIC_UPDATE -> null
      WidgetAction.BUTTON_PRESSED -> configuration.actionId
      WidgetAction.LEFT_BUTTON_PRESSED,
      WidgetAction.RIGHT_BUTTON_PRESSED -> widgetAction.getActionId(configuration.subjectFunction)
    }

    actionId?.let {
      when (configuration.subjectFunction) {
        SuplaFunction.LIGHTSWITCH,
        SuplaFunction.POWER_SWITCH,
        SuplaFunction.STAIRCASE_TIMER,
        SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
        SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
        SuplaFunction.CONTROLLING_THE_GATE,
        SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
        SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
        SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
        SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
        SuplaFunction.TERRACE_AWNING,
        SuplaFunction.PROJECTOR_SCREEN,
        SuplaFunction.CURTAIN,
        SuplaFunction.VERTICAL_BLIND,
        SuplaFunction.ROLLER_GARAGE_DOOR -> callAction(configuration, it)

        SuplaFunction.DIMMER,
        SuplaFunction.DIMMER_AND_RGB_LIGHTING,
        SuplaFunction.RGB_LIGHTING -> callRgbwAction(configuration, it)

        else -> {}
      }
    }

    return Result.success()
  }

  private fun callAction(configuration: WidgetConfiguration, action: ActionId) {
    callAction(configuration, ActionParameters(action, configuration.subjectType, configuration.itemId))
  }

  private fun callRgbwAction(configuration: WidgetConfiguration, actionId: ActionId?) {
    val brightness = getBrightness(actionId)
    callAction(
      configuration,
      RgbwActionParameters(
        ActionId.SET_RGBW_PARAMETERS,
        configuration.subjectType,
        configuration.itemId,
        brightness,
        brightness,
        configuration.value!!.toLong(),
        whiteTemperature = IGNORE_CCT,
        colorRandom = false,
        onOff = true
      )
    )
  }

  private fun callAction(configuration: WidgetConfiguration, parameters: ActionParameters) {
    val result = singleCallProvider.provide(configuration.profileId).executeAction(parameters)
    if (result is SingleCall.Result.Success) {
      showDoneToast(configuration)
    } else {
      handleUpdateResult(result, configuration)
    }
  }

  private fun showDoneToast(configuration: WidgetConfiguration) {
    handler.post {
      val text = "${configuration.caption}: ${applicationContext.getString(R.string.widget_command_started)}"
      Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
  }

  private fun handleUpdateResult(result: SingleCall.Result, configuration: WidgetConfiguration) =
    when (result) {
      is SingleCall.Result.AccessError ->
        showToast(
          applicationContext.resources.getString(R.string.widget_command_no_access, result.code),
          Toast.LENGTH_LONG
        )

      is SingleCall.Result.CommandError ->
        showToast(
          applicationContext.resources.getString(R.string.widget_command_error, result.code),
          Toast.LENGTH_SHORT
        )

      is SingleCall.Result.ConnectionError ->
        showToast(
          applicationContext.resources.getString(R.string.widget_command_connection_failure, result.code),
          Toast.LENGTH_LONG
        )

      SingleCall.Result.NotFound -> showNotFoundToast(configuration)
      SingleCall.Result.Offline -> showOfflineToast(configuration)
      SingleCall.Result.Inactive,
      SingleCall.Result.UnknownError ->
        showToast(
          applicationContext.resources.getString(R.string.widget_command_error, INTERNAL_ERROR),
          Toast.LENGTH_SHORT
        )

      SingleCall.Result.NoSuchProfile,
      SingleCall.Result.Success -> {
      } // nothing to do
    }

  private fun showNotFoundToast(configuration: WidgetConfiguration) {
    val message = applicationContext.resources.getString(
      R.string.widget_command_subject_unavailable,
      getItemTypeText(configuration).replaceFirstChar { it.uppercase() }
    )

    showToast(message, Toast.LENGTH_LONG)
  }

  private fun showOfflineToast(configuration: WidgetConfiguration) {
    val message = applicationContext.resources.getString(
      R.string.widget_command_subject_offline,
      getItemTypeText(configuration).replaceFirstChar { it.uppercase() }
    )

    showToast(message, Toast.LENGTH_LONG)
  }

  private fun getItemTypeText(configuration: WidgetConfiguration): String =
    applicationContext.resources.getString(configuration.subjectType.nameRes)

  private fun showToastLong(stringId: Int) {
    handler.post {
      Toast.makeText(applicationContext, stringId, Toast.LENGTH_LONG).show()
    }
  }

  private fun showToast(message: String, length: Int) {
    handler.post {
      Toast.makeText(applicationContext, message, length).show()
    }
  }

  private fun isNetworkAvailable(): Boolean {
    val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
  }

  private fun getBrightness(actionId: ActionId?): Short =
    if (actionId == ActionId.TURN_ON) {
      100
    } else {
      0
    }

  private fun createForegroundInfo(widgetCaption: String? = null): ForegroundInfo {
    notificationsHelper.setupBackgroundNotificationChannel(applicationContext)
    val notificationText = applicationContext.getString(R.string.widget_processing_notification_text)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      ForegroundInfo(
        notificationId,
        notificationsHelper.createBackgroundNotification(applicationContext, widgetCaption, notificationText),
        ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
      )
    } else {
      ForegroundInfo(notificationId, notificationsHelper.createBackgroundNotification(applicationContext, widgetCaption, notificationText))
    }
  }

  private fun updateWidgetConfiguration(widgetId: Int, configuration: WidgetConfiguration) =
    widgetPreferences.setWidgetConfiguration(widgetId, configuration)

  companion object {
    fun buildInputData(widgetIds: IntArray, widgetAction: WidgetAction) =
      Data.Builder()
        .putString(ARG_WIDGET_ACTION, widgetAction.string)
        .putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        .build()
  }
}

private sealed interface WorkResult {
  data object Success : WorkResult
  data object Retry : WorkResult
  data object Failure : WorkResult

  val asWorkerResult: ListenableWorker.Result
    get() = when (this) {
      Success -> ListenableWorker.Result.success()
      Retry -> ListenableWorker.Result.retry()
      Failure -> ListenableWorker.Result.failure()
    }

  fun accumulate(other: WorkResult): WorkResult =
    if (this is Failure || this is Retry) {
      this
    } else {
      other
    }
}

private val UpdateResult.toWorkResult: WorkResult
  get() = when (this) {
    is UpdateResult.Success -> WorkResult.Success
    is UpdateResult.Error -> when (result) {
      is SingleCall.Result.ConnectionError -> WorkResult.Retry
      else -> WorkResult.Failure
    }

    UpdateResult.Empty -> WorkResult.Failure
  }
