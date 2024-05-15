package org.supla.android.features.deleteaccountweb
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
class DeleteAccountWebViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : WebContentViewModel<DeleteAccountWebViewState, DeleteAccountWebViewEvent>(DeleteAccountWebViewState(), schedulers) {

  override fun urlLoaded(url: String?) {
    super.urlLoaded(url)

    if (url?.endsWith(REMOVAL_FINISHED_SUFIX) == true) {
      sendEvent(DeleteAccountWebViewEvent.CloseClicked)
    }
  }

  override fun loadingState(loading: Boolean) = currentState().copy(loading = loading)

  fun getUrl(internationalizedUrl: String, serverAddress: String?): String =
    if (serverAddress == null || serverAddress.isEmpty()) {
      internationalizedUrl.replace("{SERVER_ADDRESS}", "cloud.supla.org")
    } else {
      internationalizedUrl.replace("{SERVER_ADDRESS}", serverAddress)
    }

  companion object {
    private const val REMOVAL_FINISHED_SUFIX = "ack=true"
  }
}

sealed class DeleteAccountWebViewEvent : ViewEvent {
  object CloseClicked : DeleteAccountWebViewEvent()
}

data class DeleteAccountWebViewState(override val loading: Boolean = true) : WebContentViewState(loading)
