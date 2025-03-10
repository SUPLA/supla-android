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
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.Trace
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
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
import org.supla.android.lib.actions.RgbwActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.android.lib.singlecall.DoubleValue
import org.supla.android.lib.singlecall.ResultException
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.android.usecases.channel.valueformatter.GpmValueFormatter
import org.supla.android.usecases.channel.valueprovider.GpmValueProvider
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.onoff.ARG_TURN_ON
import org.supla.android.widget.shared.configuration.ItemType

private const val INTERNAL_ERROR = -10

abstract class WidgetCommandWorkerBase(
  private val notificationsHelper: NotificationsHelper,
  private val loadChannelConfigUseCase: LoadChannelConfigUseCase,
  appPreferences: Preferences,
  appContext: Context,
  workerParams: WorkerParameters
) : WidgetWorkerBase(appPreferences, appContext, workerParams) {

  private val handler = Handler(Looper.getMainLooper())

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
    setForegroundAsync(createForegroundInfo(configuration.itemCaption))

    if (!isNetworkAvailable()) {
      showToast(R.string.widget_command_no_connection, Toast.LENGTH_LONG)
      return Result.failure()
    }

    SuplaApp.Vibrate(applicationContext)

    return perform(widgetId, configuration)
  }

  protected abstract fun updateWidget(widgetId: Int)
  protected abstract fun valueWithUnit(): Boolean

  protected open fun perform(
    widgetId: Int,
    configuration: WidgetConfiguration
  ): Result = performCommon(
    widgetId,
    configuration,
    inputData.getBoolean(ARG_TURN_ON, false)
  )

  protected fun performCommon(
    widgetId: Int,
    configuration: WidgetConfiguration,
    turnOnOrClose: Boolean
  ): Result {
    when (configuration.itemFunction) {
      SUPLA_CHANNELFNC_LIGHTSWITCH,
      SUPLA_CHANNELFNC_POWERSWITCH,
      SUPLA_CHANNELFNC_STAIRCASETIMER ->
        callAction(configuration, if (turnOnOrClose) ActionId.TURN_ON else ActionId.TURN_OFF)

      SUPLA_CHANNELFNC_DIMMER,
      SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING,
      SUPLA_CHANNELFNC_RGBLIGHTING -> callRgbwAction(configuration, turnOnOrClose)

      SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
      SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW ->
        callAction(configuration, if (turnOnOrClose) ActionId.SHUT else ActionId.REVEAL)

      SUPLA_CHANNELFNC_THERMOMETER,
      SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
        return handleThermometerWidget(widgetId, configuration)

      SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER,
      SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT ->
        return handleGpmWidget(widgetId, configuration)
    }
    return Result.success()
  }

  protected fun callAction(configuration: WidgetConfiguration, action: ActionId) {
    val type = when (configuration.itemType) {
      ItemType.CHANNEL -> SubjectType.CHANNEL
      ItemType.GROUP -> SubjectType.GROUP
      ItemType.SCENE -> SubjectType.SCENE
    }
    callAction(configuration, ActionParameters(action, type, configuration.itemId))
  }

  private fun callRgbwAction(configuration: WidgetConfiguration, turnOnOrClose: Boolean) {
    val brightness = getBrightness(turnOnOrClose)
    callAction(
      configuration,
      RgbwActionParameters(
        ActionId.SET_RGBW_PARAMETERS,
        if (configuration.itemType.isGroup()) SubjectType.GROUP else SubjectType.CHANNEL,
        configuration.itemId,
        brightness,
        brightness,
        configuration.value!!.toLong(),
        colorRandom = false,
        onOff = true
      )
    )
  }

  private fun callAction(configuration: WidgetConfiguration, parameters: ActionParameters) {
    try {
      singleCallProvider.provide(configuration.profileId)
        .executeAction(parameters)
      showToast(R.string.widget_command_started, Toast.LENGTH_SHORT)
    } catch (ex: ResultException) {
      Trace.e(WidgetCommandWorkerBase::javaClass.name, "Could not perform action", ex)
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

  private fun getItemTypeText(configuration: WidgetConfiguration): String {
    return when (configuration.itemType) {
      ItemType.CHANNEL -> applicationContext.resources.getString(R.string.widget_channel)
      ItemType.GROUP -> applicationContext.resources.getString(R.string.widget_group)
      ItemType.SCENE -> applicationContext.resources.getString(R.string.widget_scene)
    }
  }

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

  private fun getBrightness(turnOnOrClose: Boolean): Short =
    if (turnOnOrClose) {
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
    } catch (ex: Exception) {
      null
    }
    val doubleValue = try {
      (loadValue(configuration) as DoubleValue).value
    } catch (ex: Exception) {
      null
    } ?: GpmValueProvider.UNKNOWN_VALUE

    val formatter = GpmValueFormatter(channelConfig as? SuplaChannelGeneralPurposeBaseConfig)
    updateWidgetConfiguration(widgetId, configuration.copy(value = formatter.format(doubleValue, valueWithUnit())))
    updateWidget(widgetId)
    return Result.success()
  }

  private fun createForegroundInfo(widgetCaption: String?): ForegroundInfo {
    notificationsHelper.setupBackgroundNotificationChannel(applicationContext)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      ForegroundInfo(
        notificationId,
        notificationsHelper.createBackgroundNotification(applicationContext, widgetCaption),
        ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
      )
    } else {
      ForegroundInfo(notificationId, notificationsHelper.createBackgroundNotification(applicationContext, widgetCaption))
    }
  }
}

internal fun loadTemperatureAndHumidity(
  remoteCall: () -> TemperatureAndHumidity,
  formatter: (channelValue: TemperatureAndHumidity?) -> String
): String {
  val rawValue: TemperatureAndHumidity? = try {
    remoteCall()
  } catch (ex: Exception) {
    null
  }

  return formatter(rawValue)
}
