package org.supla.android.core.notifications

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import javax.inject.Inject

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
        it.title
      }

      val text = if (it.bodyLocalizationKey != null) {
        getLocalizedString(it.bodyLocalizationKey!!, it.bodyLocalizationArgs)
      } else {
        it.body
      }

      if (title != null && text != null) {
        notificationsHelper.showNotification(baseContext, title, text)
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
