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

@AndroidEntryPoint
class NotificationMessagingService : FirebaseMessagingService() {

  @Inject
  lateinit var notificationsHelper: NotificationsHelper

  override fun onMessageReceived(message: RemoteMessage) {
    Trace.d(TAG, "Got FCM message")

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
}
