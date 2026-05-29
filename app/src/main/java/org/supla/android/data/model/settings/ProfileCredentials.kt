package org.supla.android.data.model.settings
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

import org.supla.android.lib.SuplaConst
import kotlin.random.Random

data class ProfileCredentials(
  val accessIdPassword: String,
  val guid: ByteArray,
  val authKey: ByteArray
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ProfileCredentials

    if (accessIdPassword != other.accessIdPassword) return false
    if (!guid.contentEquals(other.guid)) return false
    if (!authKey.contentEquals(other.authKey)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = accessIdPassword.hashCode()
    result = 31 * result + guid.contentHashCode()
    result = 31 * result + authKey.contentHashCode()
    return result
  }

  companion object {
    fun create(randomGenerator: Random): ProfileCredentials =
      ProfileCredentials(
        accessIdPassword = "",
        guid = randomGenerator.nextBytes(SuplaConst.SUPLA_GUID_SIZE),
        authKey = randomGenerator.nextBytes(SuplaConst.SUPLA_AUTHKEY_SIZE)
      )
  }
}
