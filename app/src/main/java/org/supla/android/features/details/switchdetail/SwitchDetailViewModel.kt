package org.supla.android.features.details.switchdetail

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.Preferences
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailViewEvent
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailViewModel
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelCaptionUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import javax.inject.Inject

@HiltViewModel
class SwitchDetailViewModel @Inject constructor(
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
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

  override fun updatedState(state: SwitchDetailViewState, channelDataBase: ChannelDataBase) =
    state.copy(caption = getChannelCaptionUseCase(channelDataBase))
}

sealed interface SwitchDetailViewEvent : StandardDetailViewEvent {
  object Close : SwitchDetailViewEvent
}

data class SwitchDetailViewState(
  override val caption: StringProvider? = null
) : StandardDetailViewState(caption)
