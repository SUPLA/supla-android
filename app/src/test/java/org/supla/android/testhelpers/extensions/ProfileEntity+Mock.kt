package org.supla.android.testhelpers.extensions
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

import org.supla.android.data.source.local.entity.ProfileEntity

fun ProfileEntity.Companion.mock(
  id: Long = 0,
  name: String = "",
  email: String = "",
  serverForAccessId: String = "",
  serverForEmail: String = "",
  serverAutoDetect: Boolean = false,
  emailAuth: Boolean = false,
  accessId: Int = 0,
  preferredProtocolVersion: Int = 0,
  active: Boolean = false,
  advancedMode: Boolean = false,
  position: Int = 0,
): ProfileEntity =
  ProfileEntity(
    id = id,
    name = name,
    email = email,
    serverForAccessId = serverForAccessId,
    serverForEmail = serverForEmail,
    serverAutoDetect = serverAutoDetect,
    emailAuth = emailAuth,
    accessId = accessId,
    preferredProtocolVersion = preferredProtocolVersion,
    active = active,
    advancedMode = advancedMode,
    position = position
  )

fun ProfileEntity.Companion.mockWithEmail(
  id: Long = 0,
  name: String = "test name",
  email: String = "test@supla.org",
  active: Boolean = false,
  serverAutoDetect: Boolean = true,
  serverForEmail: String = ""
): ProfileEntity =
  mock(
    id = id,
    name = name,
    email = email,
    active = active,
    emailAuth = true,
    serverAutoDetect = serverAutoDetect,
    serverForEmail = serverForEmail
  )
