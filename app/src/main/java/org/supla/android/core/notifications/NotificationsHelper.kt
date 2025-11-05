package org.supla.android.core.notifications

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.work.ExistingWorkPolicy
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.BuildConfig
import org.supla.android.MainActivity
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.source.NotificationRepository
import org.supla.android.features.updatetoken.UpdateTokenWorker
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

const val MAX_NOTIFICATION_ID = 1000
const val ON_OFF_WIDGET_NOTIFICATION_ID = 1010
const val SINGLE_WIDGET_NOTIFICATION_ID = 1011

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".main_channel"
private const val NOTIFICATION_BACKGROUND_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".background_channel"

@Singleton
class NotificationsHelper @Inject constructor(
  @param:ApplicationContext private val context: Context,
  private val encryptedPreferences: EncryptedPreferences,
  private val preferences: Preferences,
  private val notificationManager: NotificationManager,
  private val workManagerProxy: WorkManagerProxy,
  private val notificationRepository: NotificationRepository
) {

  private val notificationIdRandomizer = Random.Default

  fun setup(activity: Activity, askPermissionCallback: () -> Unit) {
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      setupNotificationPermission(activity, askPermissionCallback)
    } else if (VERSION.SDK_INT >= VERSION_CODES.O) {
      setupNotificationChannel(activity)
    }
  }

  fun setupNotificationChannel(context: Context) {
    if (VERSION.SDK_INT < VERSION_CODES.O) {
      return
    }

    if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
      val name = context.getString(R.string.app_name)
      val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, IMPORTANCE_HIGH)
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun setupBackgroundNotificationChannel(context: Context) {
    if (VERSION.SDK_INT < VERSION_CODES.O) {
      return
    }

    if (notificationManager.getNotificationChannel(NOTIFICATION_BACKGROUND_CHANNEL_ID) == null) {
      val name = context.getString(R.string.app_name)
      val channel = NotificationChannel(NOTIFICATION_BACKGROUND_CHANNEL_ID, name, IMPORTANCE_LOW)
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun registerForToken() {
    FirebaseMessaging.getInstance().token.addOnCompleteListener {
      if (!it.isSuccessful) {
        Timber.w("Could not fetch FCM Token")
        return@addOnCompleteListener
      }

      updateToken(it.result)
    }
  }

  fun updateToken(token: String = encryptedPreferences.fcmToken ?: "") {
    Timber.i("Updating FCM Token: $token")
    encryptedPreferences.fcmToken = token

    val workRequest = if (areNotificationsEnabled(notificationManager)) {
      UpdateTokenWorker.build(token)
    } else {
      UpdateTokenWorker.build("")
    }
    workManagerProxy.enqueueUniqueWork(UpdateTokenWorker.WORK_ID, ExistingWorkPolicy.KEEP, workRequest)
  }

  @RequiresApi(VERSION_CODES.TIRAMISU)
  private fun setupNotificationPermission(activity: Activity, askPermissionCallback: () -> Unit) {
    when {
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
        // do nothing, we have permission
      }

      activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
        // do nothing, user blocked notifications
      }

      else -> {
        if (preferences.isNotificationsPopupDisplayed.not()) {
          askPermissionCallback()
        }
      }
    }
  }

  fun showNotification(context: Context, title: String, text: String, profileName: String?) {
    if (!notificationManager.areNotificationsEnabled()) {
      return
    }
    // Channel is removed when user revoke notification permission and we don't know
    // when it may happen that's why we always try to create the channel
    setupNotificationChannel(context)

    notificationManager.notify(generateNotificationId(), buildNotification(title, text))
    notificationRepository.insert(title, text, profileName).blockingSubscribe()
  }

  fun showBackgroundNotification(context: Context, title: String, text: String? = null, notificationId: Int? = null): Int? {
    if (!notificationManager.areNotificationsEnabled()) {
      return null
    }

    val id = notificationId ?: generateNotificationId()
    notificationManager.notify(id, createBackgroundNotification(context, title, text))
    return id
  }

  fun createBackgroundNotification(context: Context, title: String? = null, text: String? = null): Notification {
    setupBackgroundNotificationChannel(context)
    return buildNotification(
      title = title ?: context.getString(R.string.widget_processing_notification_title),
      text = text ?: context.getString(R.string.widget_processing_notification_text),
      channel = NOTIFICATION_BACKGROUND_CHANNEL_ID
    )
  }

  private fun buildNotification(title: String, text: String, channel: String = NOTIFICATION_CHANNEL_ID): Notification {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    return NotificationCompat.Builder(context, channel)
      .setSmallIcon(R.drawable.logo)
      .setContentTitle(title)
      .setContentText(text)
      .setColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))
      .setContentIntent(PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE))
      .setAutoCancel(true)
      .setStyle(NotificationCompat.BigTextStyle().bigText(text))
      .build()
  }

  /* First 100 IDs are reserved for dedicated notifications */
  private fun generateNotificationId(): Int = (notificationIdRandomizer.nextInt() % MAX_NOTIFICATION_ID) + 100

  companion object {
    fun areNotificationsEnabled(notificationManager: NotificationManager): Boolean {
      return notificationManager.areNotificationsEnabled()
    }
  }
}
