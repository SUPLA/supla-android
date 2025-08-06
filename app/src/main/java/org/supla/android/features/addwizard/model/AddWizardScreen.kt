package org.supla.android.features.addwizard.model
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
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

sealed class AddWizardScreen {
  data object Welcome : AddWizardScreen()
  data object NetworkSelection : AddWizardScreen()
  data object Configuration : AddWizardScreen()
  data object Success : AddWizardScreen()

  data class Message(
    val messages: List<LocalizedString>,
    val showRepeat: Boolean
  ) : AddWizardScreen() {

    companion object {
      operator fun invoke(message: LocalizedString, showRepeat: Boolean): Message =
        Message(listOf(message), showRepeat)

      val NoWifi = Message(localizedString(R.string.wizard_no_internetwifi), false)
      val WizardUnavailable = Message(localizedString(R.string.add_wizard_is_not_available), false)
      val LocationDisabled = Message(localizedString(R.string.wizard_location_error), false)
    }
  }
}
