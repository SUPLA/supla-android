package org.supla.android.features.standarddetail.scheduledetail

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class ScheduleDetailViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : BaseViewModel<ScheduleDetailViewState, ScheduleDetailViewEvent>(ScheduleDetailViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)

}

sealed class ScheduleDetailViewEvent : ViewEvent {
}

data class ScheduleDetailViewState(
  override val loading: Boolean = false
) : ViewState(loading)
