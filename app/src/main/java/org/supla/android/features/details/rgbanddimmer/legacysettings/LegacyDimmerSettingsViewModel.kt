package org.supla.android.features.details.rgbanddimmer.legacysettings
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
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class LegacyDimmerSettingsViewModel @Inject constructor(
  private val channelRepository: RoomChannelRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<LegacyDimmerSettingsViewState, LegacyDimmerSettingsViewEvent>(LegacyDimmerSettingsViewState(), schedulers) {

  fun loadData(remoteId: Int) {
    channelRepository.findChannelDataEntity(remoteId)
      .firstElement()
      .attach()
      .subscribeBy(
        onSuccess = { sendEvent(LegacyDimmerSettingsViewEvent.LoadView(it)) },
        onError = defaultErrorHandler("loadData($remoteId)")
      )
  }
}

sealed class LegacyDimmerSettingsViewEvent : ViewEvent {
  data class LoadView(val channel: ChannelDataEntity) : LegacyDimmerSettingsViewEvent()
}

class LegacyDimmerSettingsViewState : ViewState()
