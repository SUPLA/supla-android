package org.supla.android.features.createaccountweb
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

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.features.webcontent.WebContentViewModel
import org.supla.android.features.webcontent.WebContentViewState
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class CreateAccountWebViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : WebContentViewModel<CreateAccountWebViewState, CreateAccountWebViewEvent>(CreateAccountWebViewState(), schedulers) {

  override fun loadingState(loading: Boolean) = currentState().copy(loading = loading)

  override fun urlLoaded(url: String?) {
    super.urlLoaded(url)

    if (url?.startsWith("https://cloud.supla.org/register") == true) {
      sendEvent(CreateAccountWebViewEvent.LoadRegistrationScript)
    }
  }
}

sealed class CreateAccountWebViewEvent : ViewEvent {
  data object LoadRegistrationScript : CreateAccountWebViewEvent()
}

data class CreateAccountWebViewState(override val loading: Boolean = true) : WebContentViewState(loading)
