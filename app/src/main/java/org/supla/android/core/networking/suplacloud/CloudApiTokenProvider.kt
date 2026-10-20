package org.supla.android.core.networking.suplacloud
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

import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.EncryptedPreferences
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

/** Holds the short-lived access token used by the public Cloud API client. */
@Singleton
class CloudApiTokenProvider @Inject constructor(
  private val encryptedPreferences: EncryptedPreferences,
  private val dateProvider: DateProvider
) {

  private lateinit var token: Optional<AccessToken>

  private val storedToken: AccessToken?
    get() = encryptedPreferences.getCloudApiToken()
      ?.let { (value, expiresAtMillis) -> AccessToken(value, expiresAtMillis) }

  @Synchronized
  fun provide(): String? {
    if (!this::token.isInitialized) {
      token = Optional.ofNullable(storedToken)
    }

    token.orElse(null)?.let {
      if (it.expiresAtMillis <= dateProvider.currentTimestamp()) {
        token = Optional.empty()
        encryptedPreferences.removeCloudApiToken()
        return null
      }

      return it.value
    }

    return null
  }

  @Synchronized
  fun update(token: String, expiresInSeconds: Long) {
    val expiresAtMillis = dateProvider.currentTimestamp() + expiresInSeconds * 1000
    encryptedPreferences.setCloudApiToken(token, expiresAtMillis)
    this.token = Optional.of(AccessToken(token, expiresAtMillis))
  }

  @Synchronized
  fun clean() {
    token = Optional.empty()
    encryptedPreferences.removeCloudApiToken()
  }

  private data class AccessToken(val value: String, val expiresAtMillis: Long)
}
