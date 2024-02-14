package org.supla.android.core.notifications
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import javax.inject.Inject

private const val DATA_KEY_PROFILE_NAME = "profileName"
private const val DATA_KEY_TITLE = "title"
private const val DATA_KEY_TITLE_LOCALIZED = "title_loc_key"
private const val DATA_KEY_TITLE_ARGS = "title_loc_arg"
private const val DATA_KEY_BODY = "body"
private const val DATA_KEY_BODY_LOCALIZED = "body_loc_key"
private const val DATA_KEY_BODY_ARGS = "body_loc_arg"

@AndroidEntryPoint
class NotificationMessagingService : FirebaseMessagingService() {

  @Inject
  lateinit var notificationsHelper: NotificationsHelper

  override fun onMessageReceived(message: RemoteMessage) {
    Trace.d(TAG, "Got FCM message")

    message.data.let { data ->
      val title = data[DATA_KEY_TITLE_LOCALIZED]?.let {
        getLocalizedString(it, getLocalizationParameters(data, DATA_KEY_TITLE_ARGS))
      } ?: data[DATA_KEY_TITLE] ?: baseContext.getString(R.string.app_name)
      val body = data[DATA_KEY_BODY_LOCALIZED]?.let {
        getLocalizedString(it, getLocalizationParameters(data, DATA_KEY_BODY_ARGS))
      } ?: data[DATA_KEY_BODY]

      if (body != null) {
        Trace.d(TAG, "Notification shown from data")
        notificationsHelper.showNotification(baseContext, title, body, message.data[DATA_KEY_PROFILE_NAME])
        return
      }
    }

    message.notification?.let {
      val title = if (it.titleLocalizationKey != null) {
        getLocalizedString(it.titleLocalizationKey!!, it.titleLocalizationArgs)
      } else {
        it.title ?: baseContext.getString(R.string.app_name)
      }

      val text = if (it.bodyLocalizationKey != null) {
        getLocalizedString(it.bodyLocalizationKey!!, it.bodyLocalizationArgs)
      } else {
        it.body
      }

      if (text != null) {
        Trace.d(TAG, "Notification shown from notification")
        notificationsHelper.showNotification(baseContext, title, text, message.data[DATA_KEY_PROFILE_NAME])
      }
    }
  }

  override fun onNewToken(token: String) {
    notificationsHelper.updateToken(token)
  }

  @SuppressLint("DiscouragedApi")
  private fun getLocalizedString(key: String, args: Array<String>?): String {
    val id = baseContext.resources.getIdentifier(key, "string", packageName)
    return if (id != 0) {
      when (args?.size) {
        1 -> baseContext.resources.getString(id, args[0])
        2 -> baseContext.resources.getString(id, args[0], args[1])
        3 -> baseContext.resources.getString(id, args[0], args[1], args[2])
        else -> baseContext.resources.getString(id)
      }
    } else {
      "Key '$key' with args '$args' could not be found!"
    }
  }

  private fun getLocalizationParameters(data: Map<String, String>, prefix: String): Array<String> {
    val result = mutableListOf<String>()
    for (i in 1..3) {
      val key = "${prefix}$i"
      if (data.containsKey(key)) {
        data[key]?.let { result.add(it) }
      }
    }

    return result.toTypedArray()
  }
}
