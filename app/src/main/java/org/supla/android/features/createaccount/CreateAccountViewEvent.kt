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

import org.supla.android.core.ui.ViewEvent
import org.supla.android.features.deleteaccountweb.DeleteAccountWebFragment

sealed class CreateAccountViewEvent : ViewEvent {
  data object Close : CreateAccountViewEvent()
  data object RestartFlow : CreateAccountViewEvent()
  data object ConfirmDelete : CreateAccountViewEvent()
  data object NavigateToCreateAccount : CreateAccountViewEvent()
  data object ShowBasicModeUnavailableDialog : CreateAccountViewEvent()
  data object ShowEmptyNameDialog : CreateAccountViewEvent()
  data object ShowDuplicatedNameDialog : CreateAccountViewEvent()
  data object ShowRequiredDataMissingDialog : CreateAccountViewEvent()
  data object ShowUnknownErrorDialog : CreateAccountViewEvent()
  data object ShowRemovalFailureDialog : CreateAccountViewEvent()
  data class NavigateToWebRemoval(
    val serverAddress: String?,
    val destination: DeleteAccountWebFragment.EndDestination
  ) : CreateAccountViewEvent()
}
