package org.supla.android.features.webcontent

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class WebContentViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : BaseViewModel<WebContentViewState, WebContentViewEvent>(WebContentViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)

  fun urlLoaded(url: String?) {
    if (url?.startsWith("https://cloud.supla.org/register?lang=") == true) {
      sendEvent(WebContentViewEvent.LoadRegistrationScript)
    }
  }
}