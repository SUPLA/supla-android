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
import org.supla.android.extensions.getProfileManager
import org.supla.android.lib.SuplaClient
import org.supla.android.lib.SuplaConst
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.onoff.ARG_TURN_ON

private const val MAX_WAIT_TIME = 3000 // in ms

abstract class WidgetCommandWorkerBase(
        appContext: Context,
        workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    val handler = Handler(Looper.getMainLooper())
    val preferences = WidgetPreferences(appContext)
    val profileManager = getProfileManager()

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

        val suplaClient = getSuplaClient(configuration.profileId)
        if (suplaClient == null) {
            showToast(R.string.widget_command_error, Toast.LENGTH_LONG)
            return Result.failure()
        }

        showToast(R.string.widget_command_started, Toast.LENGTH_SHORT)
        SuplaApp.Vibrate(applicationContext)

        return perform(configuration, suplaClient)
    }

    protected open fun perform(
            configuration: WidgetConfiguration,
            suplaClient: SuplaClient
    ): Result = performCommon(
            configuration,
            suplaClient,
            inputData.getBoolean(ARG_TURN_ON, false)
    )

    protected fun performCommon(
            configuration: WidgetConfiguration,
            suplaClient: SuplaClient,
            turnOnOrClose: Boolean): Result {
        when (configuration.itemFunction) {
            SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
            SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH -> {
                val turnOnOff = if (turnOnOrClose) 1 else 0
                suplaClient.open(
                        configuration.itemId,
                        configuration.itemType.isGroup(),
                        turnOnOff)
            }
            SuplaConst.SUPLA_CHANNELFNC_DIMMER,
            SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING,
            SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING -> {
                val brightness = getBrightness(turnOnOrClose)
                suplaClient.setRGBW(
                        configuration.itemId,
                        configuration.itemType.isGroup(),
                        configuration.channelColor,
                        brightness,
                        brightness,
                        true)
            }
            SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
            SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW -> {
                suplaClient.open(
                        configuration.itemId,
                        configuration.itemType.isGroup(),
                        getOpenOrClose(turnOnOrClose))
            }
        }
        return Result.success()
    }

    private fun getSuplaClient(profileId: Long): SuplaClient? {
        val suplaApp = if (applicationContext is SuplaApp) {
            (applicationContext as SuplaApp)
        } else {
            SuplaApp.getApp()
        }

        // before change check if profile exist to avoid changing id to not existing one.
        profileManager.getAllProfiles().firstOrNull { it.id == profileId }
                ?: return null
        profileManager.activateProfile(profileId)

        if (suplaApp.suplaClient == null) {
            suplaApp.SuplaClientInitIfNeed(applicationContext)
        }

        val startTime = System.currentTimeMillis()
        while (!suplaApp.suplaClient.registered()) {
            Thread.sleep(100)

            if (System.currentTimeMillis() - startTime >= MAX_WAIT_TIME) {
                // Time for connection is up, give up and show error.
                return null
            }
        }
        return suplaApp.suplaClient
    }

    private fun showToast(stringId: Int, length: Int) {
        handler.post {
            Toast.makeText(applicationContext, stringId, length).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw)
                    ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true //for check internet over Bluetooth
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return nwInfo.isConnected
        }
    }

    private fun getBrightness(turnOnOrClose: Boolean): Int =
            if (turnOnOrClose) {
                100
            } else {
                0
            }

    private fun getOpenOrClose(turnOnOrClose: Boolean): Int =
            if (turnOnOrClose) {
                SuplaConst.SUPLA_CTR_ROLLER_SHUTTER_CLOSE
            } else {
                SuplaConst.SUPLA_CTR_ROLLER_SHUTTER_OPEN
            }
}