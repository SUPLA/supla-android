package org.supla.android.widget.onoff
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
import org.supla.android.lib.SuplaClient
import org.supla.android.lib.SuplaConst
import org.supla.android.widget.INVALID_PROFILE_ID
import org.supla.android.widget.WidgetPreferences


const val ARG_TURN_ON = "ARG_TURN_ON"
private const val MAX_WAIT_TIME = 3000 // in ms

/**
 * Worker which is implemented for turning on/off switches. It supports following channel functions:
 * light switch [SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH],
 * dimmer [SuplaConst.SUPLA_CHANNELFNC_DIMMER],
 * RGB lightning [SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING],
 * dimmer with RGB lightning [SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING]
 * power switch [SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH]
 */
class OnOffWidgetCommandWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    val handler = Handler(Looper.getMainLooper())
    val preferences = WidgetPreferences(appContext)

    override fun doWork(): Result {
        val widgetIds: IntArray? = inputData.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
        if (widgetIds == null || widgetIds.size != 1) {
            showToast(R.string.on_off_widget_command_error, Toast.LENGTH_LONG)
            return Result.failure()
        }
        val widgetId = widgetIds[0]

        if (!isNetworkAvailable()) {
            showToast(R.string.on_off_widget_command_no_connection, Toast.LENGTH_LONG)
            return Result.failure()
        }

        val configuration = preferences.getWidgetConfiguration(widgetId)
        if (configuration == null) {
            showToast(R.string.on_off_widget_command_error, Toast.LENGTH_LONG)
            return Result.failure()
        }

        val suplaClient = getSuplaClient(configuration.profileId)
        if (suplaClient == null) {
            showToast(R.string.on_off_widget_command_error, Toast.LENGTH_LONG)
            return Result.failure()
        }

        showToast(R.string.on_off_widget_command_started, Toast.LENGTH_SHORT)
        SuplaApp.Vibrate(applicationContext)

        if (configuration.channelFunction == SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
                || configuration.channelFunction == SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH) {
            suplaClient.turnOnOff(applicationContext, inputData.getBoolean(ARG_TURN_ON, false),
                    configuration.channelId, false, configuration.channelFunction, false)
        } else if (configuration.channelFunction == SuplaConst.SUPLA_CHANNELFNC_DIMMER
                || configuration.channelFunction == SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
                || configuration.channelFunction == SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING) {
            val brightness = getBrightness()
            suplaClient.setRGBW(configuration.channelId, configuration.channelColor, brightness, brightness, true)
        }

        return Result.success()
    }

    private fun getSuplaClient(profileId: Long): SuplaClient? {
        val suplaApp = if (applicationContext is SuplaApp) {
            (applicationContext as SuplaApp)
        } else {
            SuplaApp.getApp()
        }

        if (profileId != INVALID_PROFILE_ID) {
            val profileManager = suplaApp.getProfileManager(applicationContext)
            profileManager.activateProfile(profileId)
        }

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

    private fun getBrightness(): Int {
        return if (inputData.getBoolean(ARG_TURN_ON, false)) {
            100
        } else {
            0
        }
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
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true //for check internet over Bluetooth
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val nwInfo = connectivityManager.activeNetworkInfo
                    ?: return false
            @Suppress("DEPRECATION") return nwInfo.isConnected
        }
    }
}