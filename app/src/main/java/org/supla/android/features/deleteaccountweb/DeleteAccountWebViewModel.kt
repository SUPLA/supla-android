package org.supla.android.features.deleteaccountweb

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.features.webcontent.WebContentViewModel
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

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)

  fun getUrl(serverAddress: String?): String =
    if (serverAddress == null || serverAddress.isEmpty()) {
      "https://cloud.supla.org/db99845855b2ecbfecca9a095062b96c3e27703f"
    } else {
      "https://$serverAddress/db99845855b2ecbfecca9a095062b96c3e27703f"
    }

  companion object {
    private const val REMOVAL_FINISHED_SUFIX = "/db99845855b2ecbfecca9a095062b96c3e27703f?ack=true"
  }
}

sealed class DeleteAccountWebViewEvent : ViewEvent {
  object CloseClicked : DeleteAccountWebViewEvent()
}

data class DeleteAccountWebViewState(override val loading: Boolean = true) : ViewState(loading)