package org.supla.android.features.webcontent

import androidx.annotation.CallSuper
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers

abstract class WebContentViewModel<S : ViewState, E : ViewEvent>(
  defaultState: S,
  schedulers: SuplaSchedulers
) : BaseViewModel<S, E>(defaultState, schedulers) {

  @CallSuper
  open fun urlLoaded(url: String?) {
    updateState { loadingState(false) }
  }
}