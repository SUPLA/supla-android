package org.supla.android.features.details.switchdetail

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.Preferences
import org.supla.android.db.ChannelBase
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailViewEvent
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailViewModel
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import javax.inject.Inject

@HiltViewModel
class SwitchDetailViewModel @Inject constructor(
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  updateEventsManager: UpdateEventsManager,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : StandardDetailViewModel<SwitchDetailViewState, SwitchDetailViewEvent>(
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  updateEventsManager,
  preferences,
  SwitchDetailViewState(),
  schedulers
) {

  override fun closeEvent() = SwitchDetailViewEvent.Close

  override fun updatedState(state: SwitchDetailViewState, channelBase: ChannelBase) =
    state.copy(channelBase = channelBase)
}

sealed interface SwitchDetailViewEvent : StandardDetailViewEvent {
  object Close : SwitchDetailViewEvent
}

data class SwitchDetailViewState(
  val channelBase: ChannelBase? = null
) : StandardDetailViewState()
