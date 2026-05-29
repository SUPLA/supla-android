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
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.supla.android.core.storage.migration.LegacyEncryptedPreferencesMigration
import org.supla.android.data.model.general.LockScreenSettings
import org.supla.android.data.model.settings.ProfileCredentials
import org.supla.android.di.RANDOM_GENERATOR
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

private const val DATASTORE_FILE_NAME = "secured_preferences.preferences_pb"
private const val TINK_KEYSET_SHARED_PREFERENCES_NAME = "secured_preferences_tink_keyset_prefs"
private const val TINK_KEYSET_NAME = "secured_preferences_tink_keyset"
private const val MASTER_KEY_URI = "android-keystore://secured_preferences_master_key"

private const val FCM_PROFILE_TOKEN_KEY_PREFIX = "FCM_PROFILE_TOKEN_KEY_"
private const val PROFILE_CREDENTIALS_PREFIX = "PROFILE_AUTHORIZATION_DATA_"

val FCM_TOKEN_KEY = stringPreferencesKey("FCM_TOKEN_KEY")
val FCM_TOKEN_LAST_UPDATE_KEY = longPreferencesKey("FCM_TOKEN_LAST_UPDATE_KEY")
val NOTIFICATIONS_LAST_ENABLED_KEY = booleanPreferencesKey("NOTIFICATIONS_LAST_ENABLED_KEY")
val LOCK_SCREEN_SETTING_KEY = stringPreferencesKey("LOCK_SCREEN_SETTING_KEY")
val DEV_MODE_KEY = booleanPreferencesKey("DEV_MODE_KEY")
val DEV_LOG_KEY = booleanPreferencesKey("DEV_LOG_KEY")
val DEV_FILTER_KEY = stringPreferencesKey("DEV_FILTER_KEY")
val WIZARD_WIFI_NAME_KEY = stringPreferencesKey("WIZARD_WIFI_NAME_KEY")
val WIZARD_WIFI_PASSWORD_KEY = stringPreferencesKey("WIZARD_WIFI_PASSWORD_KEY")

@Singleton
class EncryptedPreferences @Inject constructor(
  @param:ApplicationContext private val context: Context,
  @param:Named(RANDOM_GENERATOR) private val randomGenerator: Random
) {

  private val dataStoreFile = File(context.filesDir, "datastore/$DATASTORE_FILE_NAME").apply {
    parentFile?.mkdirs()
  }

  private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val gson = GsonBuilder().create()

  private val preferences: DataStore<Preferences> by lazy {
    DataStoreFactory.create(
      serializer = AeadSerializer(
        aead = createAead(context),
        wrappedSerializer = PreferencesFileSerializer,
        associatedData = DATASTORE_FILE_NAME.encodeToByteArray()
      ),
      corruptionHandler = ReplaceFileCorruptionHandler { PreferencesFileSerializer.defaultValue },
      migrations = listOf(LegacyEncryptedPreferencesMigration(context)),
      scope = ioScope,
      produceFile = { dataStoreFile }
    )
  }

  var fcmToken: String?
    get() = readValue { it[FCM_TOKEN_KEY] }
    set(value) = writeValue {
      if (value == null) {
        remove(FCM_TOKEN_KEY)
      } else {
        this[FCM_TOKEN_KEY] = value
      }
    }

  var fcmTokenLastUpdate: Date?
    get() = readValue { it[FCM_TOKEN_LAST_UPDATE_KEY] }
      ?.takeIf { it > 0 }
      ?.let { Date(it) }
    set(value) = writeValue {
      if (value == null) {
        remove(FCM_TOKEN_LAST_UPDATE_KEY)
      } else {
        this[FCM_TOKEN_LAST_UPDATE_KEY] = value.time
      }
    }

  var notificationsLastEnabled: Boolean
    get() = readValue { it[NOTIFICATIONS_LAST_ENABLED_KEY] } ?: false
    set(value) = writeValue {
      this[NOTIFICATIONS_LAST_ENABLED_KEY] = value
    }

  var lockScreenSettings: LockScreenSettings
    get() = LockScreenSettings.from(readValue { it[LOCK_SCREEN_SETTING_KEY] })
    set(value) = writeValue {
      this[LOCK_SCREEN_SETTING_KEY] = value.asString()
    }

  var devModeActive: Boolean
    get() = readValue { it[DEV_MODE_KEY] } ?: false
    set(value) = writeValue {
      this[DEV_MODE_KEY] = value
    }

  var devLogActive: Boolean
    get() = readValue { it[DEV_LOG_KEY] } ?: false
    set(value) = writeValue {
      this[DEV_LOG_KEY] = value
    }

  var devLogFilteringString: String?
    get() = readValue { it[DEV_FILTER_KEY] } ?: ""
    set(value) = writeValue {
      if (value == null) {
        remove(DEV_FILTER_KEY)
      } else {
        this[DEV_FILTER_KEY] = value
      }
    }

  var wizardWifiName: String?
    get() = readValue { it[WIZARD_WIFI_NAME_KEY] }
    set(value) = writeValue {
      if (value == null) {
        remove(WIZARD_WIFI_NAME_KEY)
      } else {
        this[WIZARD_WIFI_NAME_KEY] = value
      }
    }

  var wizardWifiPassword: String?
    get() = readValue { it[WIZARD_WIFI_PASSWORD_KEY] }
    set(value) = writeValue {
      if (value == null) {
        remove(WIZARD_WIFI_PASSWORD_KEY)
      } else {
        this[WIZARD_WIFI_PASSWORD_KEY] = value
      }
    }

  fun getFcmProfileToken(profileId: Long): String? {
    return readValue { it[fcmProfileTokenKey(profileId)] }
  }

  fun setFcmProfileToken(profileId: Long, token: String) {
    writeValue {
      this[fcmProfileTokenKey(profileId)] = token
    }
  }

  fun getProfileCredentialsBlocking(profileId: Long): ProfileCredentials =
    runBlocking {
      getProfileCredentials(profileId)
    }

  suspend fun getProfileCredentials(profileId: Long): ProfileCredentials {
    val key = profileCredentialsKey(profileId)
    val data = runCatching { gson.fromJson(preferences.data.first()[key], ProfileCredentials::class.java) }.getOrNull()
    if (data != null) {
      return data
    }

    val createdData = ProfileCredentials.create(randomGenerator)
    preferences.edit { it[key] = gson.toJson(data) }
    return createdData
  }

  suspend fun setProfileCredentials(profileId: Long, data: ProfileCredentials) {
    preferences.edit {
      it[profileCredentialsKey(profileId)] = gson.toJson(data)
    }
  }

  suspend fun removeProfileCredentials(profileId: Long) {
    preferences.edit {
      it.remove(profileCredentialsKey(profileId))
    }
  }

  private fun <T> readValue(block: (Preferences) -> T): T {
    return runBlocking(Dispatchers.IO) {
      block(preferences.data.first())
    }
  }

  private fun writeValue(block: MutablePreferences.() -> Unit) {
    runBlocking(Dispatchers.IO) {
      preferences.edit(block)
    }
  }
}

private fun createAead(context: Context): Aead {
  AeadConfig.register()

  val keysetHandle = AndroidKeysetManager.Builder()
    .withSharedPref(
      context,
      TINK_KEYSET_NAME,
      TINK_KEYSET_SHARED_PREFERENCES_NAME
    )
    .withKeyTemplate(KeyTemplate.createFrom(PredefinedAeadParameters.AES256_GCM))
    .withMasterKeyUri(MASTER_KEY_URI)
    .build()
    .keysetHandle

  return keysetHandle.getPrimitive(
    RegistryConfiguration.get(),
    Aead::class.java
  )
}

private fun fcmProfileTokenKey(profileId: Long) = stringPreferencesKey("$FCM_PROFILE_TOKEN_KEY_PREFIX$profileId")
private fun profileCredentialsKey(profileId: Long) = stringPreferencesKey("$PROFILE_CREDENTIALS_PREFIX$profileId")
