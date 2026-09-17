package org.supla.android.features.lockscreen
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

import org.supla.android.R
import java.io.Serializable

@kotlinx.serialization.Serializable
sealed class UnlockAction(val messageId: Int? = null, val showLogo: Boolean = false) : Serializable {

  @kotlinx.serialization.Serializable
  data object AuthorizeApplication : UnlockAction(R.string.lock_screen_hello, showLogo = true) {
    private fun readResolve(): Any = AuthorizeApplication
  }

  @kotlinx.serialization.Serializable
  data object AuthorizeAccountsCreate : UnlockAction() {
    private fun readResolve(): Any = AuthorizeAccountsCreate
  }

  @kotlinx.serialization.Serializable
  data class AuthorizeAccountsEdit(val profileId: Long) : UnlockAction()
  data object TurnOffPin : UnlockAction(R.string.lock_screen_remove_pin) {
    private fun readResolve(): Any = TurnOffPin
  }

  @kotlinx.serialization.Serializable
  data object ConfirmAuthorizeApplication : UnlockAction(R.string.lock_screen_confirm_authorize_range_change) {
    private fun readResolve(): Any = ConfirmAuthorizeApplication
  }

  @kotlinx.serialization.Serializable
  data object ConfirmAuthorizeAccounts : UnlockAction(R.string.lock_screen_confirm_authorize_range_change) {
    private fun readResolve(): Any = ConfirmAuthorizeAccounts
  }
}
