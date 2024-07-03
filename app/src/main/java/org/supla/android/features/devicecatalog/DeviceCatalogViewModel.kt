package org.supla.android.features.devicecatalog
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

import android.net.Uri
import android.webkit.WebResourceRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.features.webcontent.WebContentViewModel
import org.supla.android.features.webcontent.WebContentViewState
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class DeviceCatalogViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : WebContentViewModel<DeviceCatalogViewState, DeviceCatalogViewEvent>(DeviceCatalogViewState(), schedulers) {

  override fun loadingState(loading: Boolean) = currentState().copy(loading = loading)

  override fun allowRequest(request: WebResourceRequest?): Boolean {
    request?.let {
      sendEvent(DeviceCatalogViewEvent.OpenUrl(it.url))
    }
    return false
  }

  override fun urlLoaded(url: String?) {
    super.urlLoaded(url)
    if (url == currentState().urlRoot) {
      sendEvent(DeviceCatalogViewEvent.ApplyStyling)
    }
  }

  override fun handleError(requestUrl: String, statusCode: Int) {
    if (requestUrl == currentState().urlRoot) {
      updateState { it.copy(showError = true) }
    }
  }

  fun setUrl(url: String) {
    updateState { it.copy(urlRoot = url) }
  }

  fun onTryAgainClick() {
    updateState { it.copy(showError = false, loading = true) }
    sendEvent(DeviceCatalogViewEvent.Reload)
  }
}

sealed class DeviceCatalogViewEvent : ViewEvent {
  data class OpenUrl(val url: Uri) : DeviceCatalogViewEvent()
  data object ApplyStyling : DeviceCatalogViewEvent()
  data object Reload : DeviceCatalogViewEvent()
}

data class DeviceCatalogViewState(
  override val loading: Boolean = true,
  val showError: Boolean = false,
  val urlRoot: String = ""
) : WebContentViewState(loading)
