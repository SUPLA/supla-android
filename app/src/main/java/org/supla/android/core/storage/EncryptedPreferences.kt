package org.supla.android.core.storage
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

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val FCM_TOKEN_KEY = "FCM_TOKEN_KEY"
private const val FCM_TOKEN_LAST_UPDATE_KEY = "FCM_TOKEN_LAST_UPDATE_KEY"
private const val NOTIFICATIONS_LAST_ENABLED = "NOTIFICATIONS_LAST_ENABLED"

@Singleton
class EncryptedPreferences @Inject constructor(
  @ApplicationContext private val context: Context
) {

  private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
    context,
    "secured_preferences",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
  )

  var fcmToken: String?
    get() = preferences.getString(FCM_TOKEN_KEY, null)
    set(value) = with(preferences.edit()) {
      putString(FCM_TOKEN_KEY, value)
      apply()
    }

  var fcmTokenLastUpdate: Date?
    get() = with(preferences.getLong(FCM_TOKEN_LAST_UPDATE_KEY, -1)) {
      if (this > 0) {
        Date(this)
      } else {
        null
      }
    }
    set(value) = with(preferences.edit()) {
      putLong(FCM_TOKEN_LAST_UPDATE_KEY, value!!.time)
      apply()
    }

  var notificationsLastEnabled: Boolean
    get() = preferences.getBoolean(NOTIFICATIONS_LAST_ENABLED, false)
    set(value) = with(preferences.edit()) {
      putBoolean(NOTIFICATIONS_LAST_ENABLED, value)
      apply()
    }
}
