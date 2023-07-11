package org.supla.android.core.notifications

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
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
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.BuildConfig
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.StartActivity
import org.supla.android.Trace
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.extensions.TAG
import org.supla.android.features.updatetoken.UpdateTokenWorker
import org.supla.android.lib.SuplaClient
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".main_channel"

@Singleton
class NotificationsHelper @Inject constructor(
  @ApplicationContext private val context: Context,
  private val encryptedPreferences: EncryptedPreferences,
  private val preferences: Preferences,
  private val notificationManager: NotificationManager,
  private val suplaClientProvider: SuplaClientProvider
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

  fun registerForToken() {
    FirebaseMessaging.getInstance().token.addOnCompleteListener {
      if (!it.isSuccessful) {
        Trace.w(TAG, "Could not fetch FCM Token")
        return@addOnCompleteListener
      }

      updateToken(it.result)
    }
  }

  fun updateToken(token: String) {
    if (token == encryptedPreferences.fcmToken && tokenUpdateNotNeeded()) {
      Trace.d(TAG, "Token update skipped. Tokens are equal")
      return
    }
    Trace.i(TAG, "Updating FCM Token: $token")
    encryptedPreferences.fcmToken = token

    var currentProfileUpdated = false
    suplaClientProvider.provide()?.let {
      if (it.registered()) {
        currentProfileUpdated = it.registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, token)
      }
    }

    val workRequest = UpdateTokenWorker.build(token, currentProfileUpdated.not())
    WorkManager.getInstance(context).enqueueUniqueWork(UpdateTokenWorker.WORK_ID, ExistingWorkPolicy.KEEP, workRequest)
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

  fun showNotification(context: Context, title: String, text: String) {
    if (VERSION.SDK_INT >= VERSION_CODES.N && !notificationManager.areNotificationsEnabled()) {
      return
    }
    // Channel is removed when user revoke notification permission and we don't know
    // when it may happen that's why we always try to create the channel
    setupNotificationChannel(context)

    val intent = Intent(context, StartActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val flag = if (VERSION.SDK_INT >= VERSION_CODES.M) FLAG_IMMUTABLE else 0

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
      .setSmallIcon(R.drawable.logo)
      .setContentTitle(title)
      .setContentText(text)
      .setColor(ResourcesCompat.getColor(context.resources, R.color.supla, null))
      .setContentIntent(PendingIntent.getActivity(context, 0, intent, flag))
      .setAutoCancel(true)
      .build()

    notificationManager.notify(notificationIdRandomizer.nextInt(), notification)
  }

  private fun tokenUpdateNotNeeded(): Boolean {
    return encryptedPreferences.fcmTokenLastUpdate?.let {
      val pauseTimeInMillis = UpdateTokenWorker.UPDATE_PAUSE_IN_DAYS.times(ONE_DAY_MILLIS)
      it.time.plus(pauseTimeInMillis) > Date().time
    } ?: false
  }
}

private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000
