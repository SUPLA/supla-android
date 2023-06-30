package org.supla.android.features.main

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : BaseViewModel<MainViewState, MainViewEvent>(MainViewState(), schedulers) {
}

sealed class MainViewEvent : ViewEvent

class MainViewState: ViewState()
