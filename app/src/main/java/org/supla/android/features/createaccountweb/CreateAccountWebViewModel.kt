package org.supla.android.features.createaccountweb

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.features.webcontent.WebContentViewModel
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class CreateAccountWebViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : WebContentViewModel<CreateAccountWebViewState, CreateAccountWebViewEvent>(CreateAccountWebViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)

  override fun urlLoaded(url: String?) {
    super.urlLoaded(url)

    if (url?.startsWith("https://cloud.supla.org/register?lang=") == true) {
      sendEvent(CreateAccountWebViewEvent.LoadRegistrationScript)
    }
  }
}

sealed class CreateAccountWebViewEvent : ViewEvent {
  object LoadRegistrationScript: CreateAccountWebViewEvent()
}

data class CreateAccountWebViewState(override val loading: Boolean = true) : ViewState(loading)