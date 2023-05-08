package org.supla.android.features.channellist

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : BaseViewModel<ChannelListViewState, ChannelListViewEvent>(ChannelListViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)
}

sealed class ChannelListViewEvent : ViewEvent

data class ChannelListViewState(
  override val loading: Boolean = false
) : ViewState(loading)