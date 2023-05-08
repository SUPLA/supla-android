package org.supla.android.features.grouplist

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : BaseViewModel<GroupListViewState, GroupListViewEvent>(GroupListViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)
}

sealed class GroupListViewEvent : ViewEvent

data class GroupListViewState(override val loading: Boolean = false) : ViewState(loading)