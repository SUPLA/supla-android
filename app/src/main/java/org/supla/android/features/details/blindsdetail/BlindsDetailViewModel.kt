package org.supla.android.features.details.blindsdetail

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
class BlindDetailViewModel @Inject constructor(
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  updateEventsManager: UpdateEventsManager,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : StandardDetailViewModel<BlindsDetailViewState, BlindsDetailViewEvent>(
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  updateEventsManager,
  preferences,
  BlindsDetailViewState(),
  schedulers
) {
  override fun closeEvent(): BlindsDetailViewEvent = BlindsDetailViewEvent.Close

  override fun updatedState(state: BlindsDetailViewState, channelDataBase: ChannelDataBase) =
    state.copy(caption = getChannelCaptionUseCase(channelDataBase))
}

sealed interface BlindsDetailViewEvent : StandardDetailViewEvent {
  object Close : BlindsDetailViewEvent
}

data class BlindsDetailViewState(
  override val caption: StringProvider? = null
) : StandardDetailViewState(caption)