package org.supla.android.profile

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
import android.util.Base64
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.Encryption
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.data.source.local.entity.ProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
ProfileMigrator is a utility class which only task is
to generate a profile (default) profile entry derived
from legacy settings (i.e. authentication settings
stored in application preferences) or for usage in
new app installations.
 */
@Singleton
class ProfileMigrator @Inject constructor(
  @ApplicationContext private val context: Context
) {

  companion object {
    private const val PREF_SERVER_ADDRESS = "pref_serveraddr"
    private const val PREF_ACCESS_ID = "pref_accessid"
    private const val PREF_ACCESS_ID_PASSWORD = "pref_accessidpwd"
    private const val PREF_EMAIL = "pref_email"
    private const val PREF_IS_ADVANCED = "pref_advanced"
    private const val PREF_PROTOCOL_VERSION = "pref_proto_ver"
    private const val PREF_GUID = "pref_guid"
  }

  private val defaultSharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  fun makeProfileUsingPreferences(): ProfileEntity? {
    val serverAddr = defaultSharedPreferences.getString(PREF_SERVER_ADDRESS, "") ?: ""
    val accessID = defaultSharedPreferences.getInt(PREF_ACCESS_ID, 0)
    val accessIDpwd = defaultSharedPreferences.getString(PREF_ACCESS_ID_PASSWORD, "") ?: ""
    val email = defaultSharedPreferences.getString(PREF_EMAIL, "") ?: ""
    val isAdvanced = defaultSharedPreferences.getBoolean(PREF_IS_ADVANCED, false)

    if (serverAddr.isEmpty() && accessID == 0 && accessIDpwd.isEmpty() && email.isEmpty()) {
      // nothing stored, no profile should be crated
      return null
    }

    val client = SuplaApp.getApp().getSuplaClient()
    val protoVer = defaultSharedPreferences.getInt(PREF_PROTOCOL_VERSION, client?.maxProtoVersion ?: 0)

    return ProfileEntity(
      id = null,
      name = context.getString(R.string.profile_default_name),
      email = email,
      serverForAccessId = if (isAdvanced) serverAddr else "",
      serverForEmail = if (isAdvanced) "" else serverAddr,
      serverAutoDetect = !isAdvanced,
      emailAuth = !isAdvanced,
      accessId = accessID,
      accessIdPassword = accessIDpwd,
      preferredProtocolVersion = protoVer,
      active = true,
      advancedMode = isAdvanced,
      guid = getClientGUID(),
      // because of a bug in old version we need to take guid as auth key
      // see for more: https://github.com/SUPLA/supla-android/commit/22a741cef3ae3805fcf8eca19c55aa6b160ccd4b
      authKey = getClientGUID()
    )
  }

  private fun getClientGUID(): ByteArray {
    val key = PREF_GUID
    var result: ByteArray = Base64.decode(defaultSharedPreferences.getString(key, ""), Base64.DEFAULT)

    if (!defaultSharedPreferences.getBoolean(key + "_encrypted_gcm", false)) {
      if (defaultSharedPreferences.getBoolean(key + "_encrypted", false)) {
        result = Encryption.decryptDataWithNullOnException(result, Preferences.getDeviceID(context), true)
      }
    } else {
      result = Encryption.decryptDataWithNullOnException(result, Preferences.getDeviceID(context))
    }

    return encrypted(result)
  }

  private fun encrypted(bytes: ByteArray): ByteArray {
    val key = Preferences.getDeviceID(SuplaApp.getApp())
    return Encryption.encryptDataWithNullOnException(bytes, key)
  }
}
