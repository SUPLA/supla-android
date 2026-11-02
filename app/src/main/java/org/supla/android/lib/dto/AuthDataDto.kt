package org.supla.android.lib.dto
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
import org.supla.android.tools.UsedFromNativeCode

@UsedFromNativeCode
data class AuthDataDto(
  val emailAuth: Boolean,
  val emailAddress: String,
  val serverForEmail: String,
  val accessId: Int = 0,
  val accessIdPassword: String,
  val serverForAccessId: String,
  val preferredProtocolVersion: Int,
  val guid: ByteArray,
  val authKey: ByteArray
) {
  @UsedFromNativeCode
  fun getDecryptedGuid(context: Context): ByteArray {
    return guid
  }

  @UsedFromNativeCode
  fun getDecryptedAuthKey(context: Context): ByteArray {
    return authKey
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AuthDataDto

    if (emailAuth != other.emailAuth) return false
    if (accessId != other.accessId) return false
    if (preferredProtocolVersion != other.preferredProtocolVersion) return false
    if (emailAddress != other.emailAddress) return false
    if (serverForEmail != other.serverForEmail) return false
    if (accessIdPassword != other.accessIdPassword) return false
    if (serverForAccessId != other.serverForAccessId) return false
    if (!guid.contentEquals(other.guid)) return false
    if (!authKey.contentEquals(other.authKey)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = emailAuth.hashCode()
    result = 31 * result + accessId
    result = 31 * result + preferredProtocolVersion
    result = 31 * result + emailAddress.hashCode()
    result = 31 * result + serverForEmail.hashCode()
    result = 31 * result + accessIdPassword.hashCode()
    result = 31 * result + serverForAccessId.hashCode()
    result = 31 * result + guid.contentHashCode()
    result = 31 * result + authKey.contentHashCode()
    return result
  }
}
