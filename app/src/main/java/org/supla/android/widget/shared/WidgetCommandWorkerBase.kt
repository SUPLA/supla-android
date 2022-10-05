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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.Trace
import org.supla.android.extensions.getSingleCallProvider
import org.supla.android.lib.SuplaConst.*
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.RgbwActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.android.lib.singlecall.ResultException
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.onoff.ARG_TURN_ON

abstract class WidgetCommandWorkerBase(
  appContext: Context,
  workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

  private val handler = Handler(Looper.getMainLooper())
  private val preferences = WidgetPreferences(appContext)
  private val singleCallProvider = getSingleCallProvider()

  override fun doWork(): Result {
    val widgetIds: IntArray? = inputData.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
    if (widgetIds == null || widgetIds.size != 1) {
      showToast(R.string.widget_command_error, Toast.LENGTH_LONG)
      return Result.failure()
    }
    val widgetId = widgetIds[0]

    if (!isNetworkAvailable()) {
      showToast(R.string.widget_command_no_connection, Toast.LENGTH_LONG)
      return Result.failure()
    }

    val configuration = preferences.getWidgetConfiguration(widgetId)
    if (configuration == null) {
      showToast(R.string.widget_command_error, Toast.LENGTH_LONG)
      return Result.failure()
    }

    SuplaApp.Vibrate(applicationContext)

    return perform(configuration)
  }

  protected open fun perform(
    configuration: WidgetConfiguration
  ): Result = performCommon(
    configuration,
    inputData.getBoolean(ARG_TURN_ON, false)
  )

  protected fun performCommon(
    configuration: WidgetConfiguration,
    turnOnOrClose: Boolean
  ): Result {
    when (configuration.itemFunction) {
      SUPLA_CHANNELFNC_LIGHTSWITCH,
      SUPLA_CHANNELFNC_POWERSWITCH ->
        callAction(configuration, if (turnOnOrClose) ActionId.TURN_ON else ActionId.TURN_OFF)
      SUPLA_CHANNELFNC_DIMMER,
      SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING,
      SUPLA_CHANNELFNC_RGBLIGHTING -> callRgbwAction(configuration, turnOnOrClose)
      SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
      SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW ->
        callAction(configuration, if (turnOnOrClose) ActionId.SHUT else ActionId.REVEAL)
    }
    return Result.success()
  }

  protected fun callAction(configuration: WidgetConfiguration, action: ActionId) {
    val type = if (configuration.itemType.isGroup()) SubjectType.GROUP else SubjectType.CHANNEL
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
        configuration.channelColor.toLong(),
        colorRandom = false,
        onOff = true
      )
    )
  }

  private fun callAction(configuration: WidgetConfiguration, parameters: ActionParameters) {
    try {
      singleCallProvider.provide(applicationContext, configuration.profileId)
        .executeAction(parameters)
      showToast(R.string.widget_command_started, Toast.LENGTH_SHORT)
    } catch (ex: ResultException) {
      Trace.e(WidgetCommandWorkerBase::javaClass.name, "Could not perform action", ex)
      when (ex.result) {
        SUPLA_RESULT_VERSION_ERROR,
        SUPLA_RESULTCODE_FALSE,
        SUPLA_RESULTCODE_GUID_ERROR,
        SUPLA_RESULTCODE_INCORRECT_PARAMETERS -> showToast(
          R.string.widget_command_error,
          Toast.LENGTH_SHORT
        )
        SUPLA_RESULT_HOST_NOT_FOUND,
        SUPLA_RESULT_CANT_CONNECT_TO_HOST,
        SUPLA_RESULT_RESPONSE_TIMEOUT -> showToast(
          R.string.widget_command_no_connection,
          Toast.LENGTH_LONG
        )
        SUPLA_RESULTCODE_CHANNEL_IS_OFFLINE -> showToast(
          R.string.widget_command_offline,
          Toast.LENGTH_LONG
        )
        SUPLA_RESULTCODE_CLIENT_NOT_EXISTS,
        SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE,
        SUPLA_RESULTCODE_SUBJECT_NOT_FOUND -> showToast(
          R.string.widget_command_not_available,
          Toast.LENGTH_LONG
        )
        SUPLA_RESULTCODE_AUTHKEY_ERROR,
        SUPLA_RESULTCODE_BAD_CREDENTIALS,
        SUPLA_RESULTCODE_CLIENT_DISABLED,
        SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED,
        SUPLA_RESULTCODE_ACCESSID_DISABLED,
        SUPLA_RESULTCODE_ACCESSID_INACTIVE -> showToast(
          R.string.widget_command_no_connection,
          Toast.LENGTH_LONG
        )
      }
    }
  }

  private fun showToast(stringId: Int, length: Int) {
    handler.post {
      Toast.makeText(applicationContext, stringId, length).show()
    }
  }

  private fun isNetworkAvailable(): Boolean {
    val connectivityManager =
      applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val nw = connectivityManager.activeNetwork ?: return false
      val actNw = connectivityManager.getNetworkCapabilities(nw)
        ?: return false
      return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        // for other device which are able to connect with Ethernet
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        // for check internet over Bluetooths
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
      }
    } else {
      @Suppress("DEPRECATION")
      val nwInfo = connectivityManager.activeNetworkInfo ?: return false
      @Suppress("DEPRECATION")
      return nwInfo.isConnected
    }
  }

  private fun getBrightness(turnOnOrClose: Boolean): Short =
    if (turnOnOrClose) {
      100
    } else {
      0
    }
}
