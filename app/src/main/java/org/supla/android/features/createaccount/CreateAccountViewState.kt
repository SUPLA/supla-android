package org.supla.android.features.createaccount

import org.supla.android.core.ui.ViewState
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.AuthInfo

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
) : ViewState() {

  fun toProfileItem() = AuthProfileItem(
    name = accountName,
    authInfo = AuthInfo(
      emailAuth = authorizeByEmail,
      serverAutoDetect = autoServerAddress,
      serverForEmail = emailAddressServer,
      serverForAccessID = accessIdentifierServer,
      emailAddress = emailAddress,
      accessID = accessIdentifierAsInt,
      accessIDpwd = accessIdentifierPassword
    ),
    advancedAuthSetup = advancedMode,
    isActive = false
  )

  fun updateProfile(profile: AuthProfileItem): AuthProfileItem {
    profile.name = accountName
    profile.advancedAuthSetup = advancedMode
    profile.authInfo.emailAuth = authorizeByEmail
    profile.authInfo.serverAutoDetect = autoServerAddress
    profile.authInfo.serverForEmail = emailAddressServer
    profile.authInfo.serverForAccessID = accessIdentifierServer
    profile.authInfo.emailAddress = emailAddress
    profile.authInfo.accessID = accessIdentifierAsInt
    profile.authInfo.accessIDpwd = accessIdentifierPassword

    return profile
  }

  private val accessIdentifierAsInt: Int
    get() = accessIdentifier.run {
      try {
        accessIdentifier.toInt()
      } catch (ex: NumberFormatException) {
        0
      }
    }
}
