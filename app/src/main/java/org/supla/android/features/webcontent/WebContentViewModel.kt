package org.supla.android.features.webcontent

import androidx.annotation.CallSuper
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers

abstract class WebContentViewModel<S : WebContentViewState, E : ViewEvent>(
  defaultState: S,
  schedulers: SuplaSchedulers
) : BaseViewModel<S, E>(defaultState, schedulers) {

  abstract fun loadingState(loading: Boolean): S

  @CallSuper
  open fun urlLoaded(url: String?) {
    updateState { loadingState(false) }
  }
}

open class WebContentViewState(open val loading: Boolean = true) : ViewState()