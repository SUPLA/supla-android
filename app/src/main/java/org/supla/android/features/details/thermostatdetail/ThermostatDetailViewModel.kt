package org.supla.android.features.details.thermostatdetail
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
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.isHvacThermostat
import org.supla.android.data.source.remote.channel.SuplaChannelFunction
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailViewEvent
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailViewModel
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelCaptionUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import javax.inject.Inject

@HiltViewModel
class ThermostatDetailViewModel @Inject constructor(
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  updateEventsManager: UpdateEventsManager,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : StandardDetailViewModel<ThermostatDetailViewState, ThermostatDetailViewEvent>(
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  updateEventsManager,
  preferences,
  ThermostatDetailViewState(),
  schedulers
) {

  override fun closeEvent() = ThermostatDetailViewEvent.Close

  override fun updatedState(state: ThermostatDetailViewState, channelDataBase: ChannelDataBase) =
    state.copy(
      caption = getChannelCaptionUseCase(channelDataBase),
      subfunction = (channelDataBase as? ChannelDataEntity)?.channelValueEntity?.asThermostatValue()?.subfunction
    )

  override fun shouldCloseDetail(channelDataBase: ChannelDataBase, initialFunction: SuplaChannelFunction) =
    when (channelDataBase) {
      is ChannelDataEntity -> {
        if (channelDataBase.isHvacThermostat()) {
          if (super.shouldCloseDetail(channelDataBase, initialFunction)) {
            true
          } else {
            val subfunction = channelDataBase.channelValueEntity.asThermostatValue().subfunction
            val currentSubfunction = currentState().subfunction

            currentSubfunction != null && currentSubfunction != subfunction
          }
        } else {
          super.shouldCloseDetail(channelDataBase, initialFunction)
        }
      }

      else -> super.shouldCloseDetail(channelDataBase, initialFunction)
    }
}

sealed interface ThermostatDetailViewEvent : StandardDetailViewEvent {
  object Close : ThermostatDetailViewEvent
}

data class ThermostatDetailViewState(
  override val caption: StringProvider? = null,
  val subfunction: ThermostatSubfunction? = null
) : StandardDetailViewState(caption)
