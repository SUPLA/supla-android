package org.supla.android.features.details.gatedetail
/*
Copyright (C) AC SOFTWARE SP. Z O.O.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.Preferences
import org.supla.android.core.shared.shareable
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.base.BaseDetailViewEvent
import org.supla.android.features.details.detailbase.base.BaseDetailViewModel
import org.supla.android.features.details.detailbase.base.BaseDetailViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class GateDetailViewModel @Inject constructor(
  private val getCaptionUseCase: GetCaptionUseCase,
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  updateEventsManager: UpdateEventsManager,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseDetailViewModel<GateDetailViewState, GateDetailViewEvent>(
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  updateEventsManager,
  preferences,
  GateDetailViewState(),
  schedulers
) {

  override fun closeEvent() = GateDetailViewEvent.Close

  override fun updatedState(state: GateDetailViewState, channelDataBase: ChannelDataBase) =
    state.copy(caption = getCaptionUseCase(channelDataBase.shareable))
}

sealed interface GateDetailViewEvent : BaseDetailViewEvent {
  data object Close : GateDetailViewEvent
}

data class GateDetailViewState(
  override val caption: LocalizedString? = null
) : BaseDetailViewState(caption)
