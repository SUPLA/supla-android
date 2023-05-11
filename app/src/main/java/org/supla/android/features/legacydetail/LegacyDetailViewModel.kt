package org.supla.android.features.legacydetail

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class LegacyDetailViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : BaseViewModel<LegacyDetailViewState, LegacyDetailViewEvent>(LegacyDetailViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)
}

sealed class LegacyDetailViewEvent : ViewEvent

data class LegacyDetailViewState(override val loading: Boolean = false) : ViewState(loading)