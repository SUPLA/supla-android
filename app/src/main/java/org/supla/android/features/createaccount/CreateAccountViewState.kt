package org.supla.android.features.createaccount

import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.ProfileEntity

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

  fun toProfileItem() = ProfileEntity(
    id = null,
    name = accountName.trim(),
    advancedMode = advancedMode,
    emailAuth = authorizeByEmail,
    serverForEmail = emailAddressServer,
    serverForAccessId = accessIdentifierServer,
    serverAutoDetect = autoServerAddress,
    email = emailAddress,
    accessId = accessIdentifierAsInt,
    accessIdPassword = accessIdentifierPassword,
    preferredProtocolVersion = 0,
    active = false,
    position = 0,
    guid = byteArrayOf(),
    authKey = byteArrayOf()
  )

  fun updateProfile(profile: ProfileEntity) =
    profile.copy(
      name = accountName,
      advancedMode = advancedMode,
      emailAuth = authorizeByEmail,
      serverAutoDetect = autoServerAddress,
      serverForEmail = emailAddressServer,
      serverForAccessId = accessIdentifierServer,
      email = emailAddress,
      accessId = accessIdentifierAsInt,
      accessIdPassword = accessIdentifierPassword
    )

  private val accessIdentifierAsInt: Int
    get() = accessIdentifier.run {
      try {
        accessIdentifier.toInt()
      } catch (ex: NumberFormatException) {
        0
      }
    }
}
