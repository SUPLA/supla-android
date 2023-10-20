package org.supla.android.features.thermostatdetail
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
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.events.ListsEventsManager
import org.supla.android.extensions.isHvacThermostat
import org.supla.android.features.standarddetail.StandardDetailViewEvent
import org.supla.android.features.standarddetail.StandardDetailViewModel
import org.supla.android.features.standarddetail.StandardDetailViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import javax.inject.Inject

@HiltViewModel
class ThermostatDetailViewModel @Inject constructor(
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  listsEventsManager: ListsEventsManager,
  schedulers: SuplaSchedulers
) : StandardDetailViewModel<ThermostatDetailViewState, ThermostatDetailViewEvent>(
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  listsEventsManager,
  ThermostatDetailViewState(),
  schedulers
) {

  override fun closeEvent() = ThermostatDetailViewEvent.Close

  override fun updatedState(state: ThermostatDetailViewState, channelBase: ChannelBase) =
    state.copy(channelBase = channelBase)

  override fun shouldCloseDetail(channelBase: ChannelBase, initialFunction: Int) =
    when (channelBase) {
      is Channel -> {
        if (channelBase.isHvacThermostat()) {
          if (super.shouldCloseDetail(channelBase, initialFunction)) {
            true
          } else {
            val subfunction = channelBase.value.asThermostatValue().subfunction
            val currentSubfunction = (currentState().channelBase as? Channel)?.value?.asThermostatValue()?.subfunction
            currentSubfunction != null && currentSubfunction != subfunction
          }
        } else {
          super.shouldCloseDetail(channelBase, initialFunction)
        }
      }

      else -> super.shouldCloseDetail(channelBase, initialFunction)
    }
}

sealed interface ThermostatDetailViewEvent : StandardDetailViewEvent {
  object Close : ThermostatDetailViewEvent
}

data class ThermostatDetailViewState(
  val channelBase: ChannelBase? = null
) : StandardDetailViewState()
