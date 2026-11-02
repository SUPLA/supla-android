@file:Suppress("DEPRECATION")

package org.supla.android.core.storage.migration
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
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.supla.android.core.storage.DEV_FILTER_KEY
import org.supla.android.core.storage.DEV_LOG_KEY
import org.supla.android.core.storage.DEV_MODE_KEY
import org.supla.android.core.storage.FCM_TOKEN_KEY
import org.supla.android.core.storage.FCM_TOKEN_LAST_UPDATE_KEY
import org.supla.android.core.storage.LOCK_SCREEN_SETTING_KEY
import org.supla.android.core.storage.NOTIFICATIONS_LAST_ENABLED_KEY
import org.supla.android.core.storage.WIZARD_WIFI_NAME_KEY
import org.supla.android.core.storage.WIZARD_WIFI_PASSWORD_KEY
import java.io.File

private const val FCM_TOKEN_KEY_NAME = "FCM_TOKEN_KEY"
private const val FCM_TOKEN_LAST_UPDATE_KEY_NAME = "FCM_TOKEN_LAST_UPDATE_KEY"
private const val NOTIFICATIONS_LAST_ENABLED_KEY_NAME = "NOTIFICATIONS_LAST_ENABLED"
private const val FCM_PROFILE_TOKEN_KEY_PREFIX = "FCM_TOKEN_KEY_"
private const val LOCK_SCREEN_SETTING_KEY_NAME = "LOCK_SCREEN_SETTING_KEY"
private const val DEV_MODE_KEY_NAME = "DEV_MODE_KEY"
private const val DEV_LOG_KEY_NAME = "DEV_LOG_KEY"
private const val DEV_FILTER_KEY_NAME = "DEV_FILTER_KEY"
private const val WIZARD_WIFI_NAME_KEY_NAME = "WIZARD_WIFI_NAME"
private const val WIZARD_WIFI_PASSWORD_KEY_NAME = "WIZARD_WIFI_PASSWORD"

private const val LEGACY_SHARED_PREFERENCES_NAME = "secured_preferences"

class LegacyEncryptedPreferencesMigration(
  private val context: Context
) : DataMigration<Preferences> {

  override suspend fun shouldMigrate(currentData: Preferences): Boolean {
    return currentData.asMap().isEmpty() && legacyPreferencesFile(context).exists()
  }

  override suspend fun migrate(currentData: Preferences): Preferences {
    val legacyPreferences = createLegacyEncryptedPreferences(context)

    return currentData.copy { preferences ->
      legacyPreferences.getString(FCM_TOKEN_KEY_NAME, null)?.let {
        preferences[FCM_TOKEN_KEY] = it
      }

      legacyPreferences.getLong(FCM_TOKEN_LAST_UPDATE_KEY_NAME, -1L)
        .takeIf { it > 0 }
        ?.let { preferences[FCM_TOKEN_LAST_UPDATE_KEY] = it }

      preferences[NOTIFICATIONS_LAST_ENABLED_KEY] =
        legacyPreferences.getBoolean(NOTIFICATIONS_LAST_ENABLED_KEY_NAME, false)

      legacyPreferences.getString(LOCK_SCREEN_SETTING_KEY_NAME, null)?.let {
        preferences[LOCK_SCREEN_SETTING_KEY] = it
      }

      preferences[DEV_MODE_KEY] =
        legacyPreferences.getBoolean(DEV_MODE_KEY_NAME, false)
      preferences[DEV_LOG_KEY] =
        legacyPreferences.getBoolean(DEV_LOG_KEY_NAME, false)

      legacyPreferences.getString(DEV_FILTER_KEY_NAME, "")?.let {
        preferences[DEV_FILTER_KEY] = it
      }

      legacyPreferences.getString(WIZARD_WIFI_NAME_KEY_NAME, null)?.let {
        preferences[WIZARD_WIFI_NAME_KEY] = it
      }

      legacyPreferences.getString(WIZARD_WIFI_PASSWORD_KEY_NAME, null)?.let {
        preferences[WIZARD_WIFI_PASSWORD_KEY] = it
      }

      for (entry in legacyPreferences.all.entries) {
        val key = entry.key
        val value = entry.value
        if (key.startsWith(FCM_PROFILE_TOKEN_KEY_PREFIX) && value is String) {
          preferences[stringPreferencesKey(key)] = value
        }
      }
    }
  }

  override suspend fun cleanUp() {
    context.deleteSharedPreferences(LEGACY_SHARED_PREFERENCES_NAME)
  }
}

private fun createLegacyEncryptedPreferences(context: Context) = EncryptedSharedPreferences.create(
  context,
  LEGACY_SHARED_PREFERENCES_NAME,
  MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
  EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
  EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

private fun legacyPreferencesFile(context: Context): File {
  return File(context.applicationInfo.dataDir, "shared_prefs/${LEGACY_SHARED_PREFERENCES_NAME}.xml")
}
