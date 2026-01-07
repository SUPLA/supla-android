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
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.supla.android.R
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.data.source.remote.gpm.toValueFormat
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_DISABLED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_INACTIVE
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_AUTHKEY_ERROR
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_BAD_CREDENTIALS
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CHANNEL_IS_OFFLINE
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CLIENT_DISABLED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CLIENT_NOT_EXISTS
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_FALSE
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_GUID_ERROR
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_INCORRECT_PARAMETERS
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_SUBJECT_NOT_FOUND
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_CANT_CONNECT_TO_HOST
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_HOST_NOT_FOUND
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_RESPONSE_TIMEOUT
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_VERSION_ERROR
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.IGNORE_CCT
import org.supla.android.lib.actions.RgbwActionParameters
import org.supla.android.lib.singlecall.ContainerLevel
import org.supla.android.lib.singlecall.DoubleValue
import org.supla.android.lib.singlecall.ResultException
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.android.tools.VibrationHelper
import org.supla.android.usecases.channel.valueprovider.GpmValueProvider
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ContainerValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.formatters.GpmValueFormatter
import timber.log.Timber

private const val INTERNAL_ERROR = -10

abstract class WidgetCommandWorkerBase(
  private val loadChannelConfigUseCase: LoadChannelConfigUseCase,
  private val notificationsHelper: NotificationsHelper,
  private val vibrationHelper: VibrationHelper,
  appPreferences: ApplicationPreferences,
  appContext: Context,
  workerParams: WorkerParameters
) : WidgetWorkerBase(appPreferences, appContext, workerParams) {

  private val handler = Handler(Looper.getMainLooper())
  private val formatter = GpmValueFormatter()

  protected abstract val notificationId: Int

  override fun doWork(): Result {
    val widgetIds: IntArray? = inputData.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
    if (widgetIds == null || widgetIds.size != 1) {
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

    val configuration = preferences.getWidgetConfiguration(widgetId)
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
      showToast(R.string.widget_command_no_connection, Toast.LENGTH_LONG)
      return Result.failure()
    }

    vibrationHelper.vibrate()

    return perform(widgetId, configuration)
  }

  protected abstract fun updateWidget(widgetId: Int)
  protected abstract fun valueWithUnit(): Boolean

  protected abstract fun perform(
    widgetId: Int,
    configuration: WidgetConfiguration
  ): Result

  protected fun performCommon(
    widgetId: Int,
    configuration: WidgetConfiguration,
    actionId: ActionId?
  ): Result {
    when (configuration.subjectFunction) {
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW ->
        actionId?.let { callAction(configuration, it) }

      SuplaFunction.DIMMER,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaFunction.RGB_LIGHTING -> callRgbwAction(configuration, actionId)

      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE ->
        return handleThermometerWidget(widgetId, configuration)

      SuplaFunction.GENERAL_PURPOSE_METER,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT ->
        return handleGpmWidget(widgetId, configuration)

      SuplaFunction.CONTAINER,
      SuplaFunction.WATER_TANK,
      SuplaFunction.SEPTIC_TANK ->
        return handleContainer(widgetId, configuration)

      else -> {}
    }
    return Result.success()
  }

  protected fun callAction(configuration: WidgetConfiguration, action: ActionId) {
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
        dimmerCct = IGNORE_CCT,
        colorRandom = false,
        onOff = true
      )
    )
  }

  private fun callAction(configuration: WidgetConfiguration, parameters: ActionParameters) {
    try {
      singleCallProvider.provide(configuration.profileId).executeAction(parameters)
      handler.post {
        val text = "${configuration.caption}: ${applicationContext.getString(R.string.widget_command_started)}"
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
      }
    } catch (ex: ResultException) {
      Timber.e(ex, "Could not perform action")
      when (ex.result) {
        SUPLA_RESULT_VERSION_ERROR,
        SUPLA_RESULTCODE_FALSE,
        SUPLA_RESULTCODE_GUID_ERROR,
        SUPLA_RESULTCODE_INCORRECT_PARAMETERS,
        SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE,
        SUPLA_RESULTCODE_AUTHKEY_ERROR -> showToast(
          applicationContext.resources.getString(R.string.widget_command_error, ex.result),
          Toast.LENGTH_SHORT
        )

        SUPLA_RESULT_HOST_NOT_FOUND,
        SUPLA_RESULT_CANT_CONNECT_TO_HOST,
        SUPLA_RESULT_RESPONSE_TIMEOUT -> showToast(
          applicationContext.resources.getString(
            R.string.widget_command_connection_failure,
            ex.result
          ),
          Toast.LENGTH_LONG
        )

        SUPLA_RESULTCODE_CHANNEL_IS_OFFLINE -> showOfflineToast(configuration)
        SUPLA_RESULTCODE_SUBJECT_NOT_FOUND -> showNotFoundToast(configuration)
        SUPLA_RESULTCODE_CLIENT_NOT_EXISTS,
        SUPLA_RESULTCODE_BAD_CREDENTIALS,
        SUPLA_RESULTCODE_CLIENT_DISABLED,
        SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED,
        SUPLA_RESULTCODE_ACCESSID_DISABLED,
        SUPLA_RESULTCODE_ACCESSID_INACTIVE -> showToast(
          applicationContext.resources.getString(R.string.widget_command_no_access, ex.result),
          Toast.LENGTH_LONG
        )

        else -> showToast(
          applicationContext.resources.getString(R.string.widget_command_error, ex.result),
          Toast.LENGTH_SHORT
        )
      }
    }
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

  private fun showToast(stringId: Int, length: Int) {
    handler.post {
      Toast.makeText(applicationContext, stringId, length).show()
    }
  }

  private fun showToast(message: String, length: Int) {
    handler.post {
      Toast.makeText(applicationContext, message, length).show()
    }
  }

  @Suppress("DEPRECATION")
  private fun isNetworkAvailable(): Boolean {
    val connectivityManager =
      applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.allNetworks.forEach {
      val actNw = connectivityManager.getNetworkCapabilities(it)
        ?: return@forEach

      val result = when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        // for other device which are able to connect with Ethernet
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        // for check internet over Bluetooths
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
      }

      if (result) {
        return true
      }
    }

    return false
  }

  private fun getBrightness(actionId: ActionId?): Short =
    if (actionId == ActionId.TURN_ON) {
      100
    } else {
      0
    }

  private fun handleThermometerWidget(widgetId: Int, configuration: WidgetConfiguration): Result {
    val temperature = loadTemperatureAndHumidity(
      { (loadValue(configuration) as TemperatureAndHumidity) },
      getTemperatureAndHumidityFormatter(configuration, valueWithUnit())
    )

    updateWidgetConfiguration(widgetId, configuration.copy(value = temperature))
    updateWidget(widgetId)
    return Result.success()
  }

  private fun handleGpmWidget(widgetId: Int, configuration: WidgetConfiguration): Result {
    val channelConfig = try {
      loadChannelConfigUseCase(configuration.itemId).blockingGet()
    } catch (_: Exception) {
      null
    }
    val doubleValue = try {
      (loadValue(configuration) as DoubleValue).value
    } catch (_: Exception) {
      null
    } ?: GpmValueProvider.UNKNOWN_VALUE

    val value = formatter.format(
      value = doubleValue,
      format = (channelConfig as? SuplaChannelGeneralPurposeBaseConfig).toValueFormat(valueWithUnit())
    )
    updateWidgetConfiguration(widgetId, configuration.copy(value = value))
    updateWidget(widgetId)
    return Result.success()
  }

  private fun handleContainer(widgetId: Int, configuration: WidgetConfiguration): Result {
    val level = try {
      (loadValue(configuration) as ContainerLevel)
    } catch (_: Exception) {
      null
    }

    val formatted = ContainerValueFormatter.format(level)
    updateWidgetConfiguration(widgetId, configuration.copy(value = formatted))
    updateWidget(widgetId)
    return Result.success()
  }

  private fun createForegroundInfo(widgetCaption: String?): ForegroundInfo {
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
}

internal fun loadTemperatureAndHumidity(
  remoteCall: () -> TemperatureAndHumidity,
  formatter: (channelValue: TemperatureAndHumidity?) -> String
): String {
  val rawValue: TemperatureAndHumidity? = try {
    remoteCall()
  } catch (_: Exception) {
    null
  }

  return formatter(rawValue)
}
