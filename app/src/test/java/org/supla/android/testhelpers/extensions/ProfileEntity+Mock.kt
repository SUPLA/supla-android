package org.supla.android.testhelpers.extensions

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
