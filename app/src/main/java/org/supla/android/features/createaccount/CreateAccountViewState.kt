package org.supla.android.features.createaccount
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

import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.usecases.profile.ProfileDto

data class CreateAccountViewState(
  val advancedMode: Boolean = false,
  val accountName: String = "",
  val emailAddress: String = "",
  val authorizeByEmail: Boolean = true,

  //
  val autoServerAddress: Boolean = true,
  val emailAddressServer: String = "",

  //
  val accessIdentifier: String = "",
  val accessIdentifierPassword: String = "",
  val accessIdentifierServer: String = "",

  //
  val profileNameVisible: Boolean = false,
  val deleteButtonVisible: Boolean = false,

  //
  val loading: Boolean = false
) : ViewState() {

  fun toProfileDto() = ProfileDto(
    id = 0,
    name = accountName.trim(),
    advancedMode = advancedMode,
    emailAuth = authorizeByEmail,
    serverForEmail = emailAddressServer,
    serverForAccessId = accessIdentifierServer,
    serverAutoDetect = autoServerAddress,
    email = emailAddress,
    accessId = accessIdentifierAsInt,
    accessIdPassword = accessIdentifierPassword,
    active = false,
    position = 0
  )

  fun profileDtoFrom(profile: ProfileEntity) =
    ProfileDto(
      id = profile.id,
      name = accountName,
      advancedMode = advancedMode,
      emailAuth = authorizeByEmail,
      serverAutoDetect = autoServerAddress,
      serverForEmail = emailAddressServer,
      serverForAccessId = accessIdentifierServer,
      email = emailAddress,
      accessId = accessIdentifierAsInt,
      accessIdPassword = accessIdentifierPassword,
      active = profile.active,
      position = profile.position
    )

  private val accessIdentifierAsInt: Int
    get() = accessIdentifier.run {
      try {
        accessIdentifier.toInt()
      } catch (_: NumberFormatException) {
        0
      }
    }
}
