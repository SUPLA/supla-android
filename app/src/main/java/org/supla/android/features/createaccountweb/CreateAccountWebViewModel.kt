package org.supla.android.features.createaccountweb

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
  object LoadRegistrationScript : CreateAccountWebViewEvent()
}

data class CreateAccountWebViewState(override val loading: Boolean = true) : WebContentViewState(loading)
