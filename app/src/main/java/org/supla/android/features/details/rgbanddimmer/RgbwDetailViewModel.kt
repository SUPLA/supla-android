package org.supla.android.features.details.rgbanddimmer
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
import org.supla.android.core.shared.shareable
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.base.BaseDetailViewEvent
import org.supla.android.features.details.detailbase.base.BaseDetailViewModel
import org.supla.android.features.details.detailbase.base.BaseDetailViewState
import org.supla.android.lib.SuplaConst
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class RgbwDetailViewModel @Inject constructor(
  private val getCaptionUseCase: GetCaptionUseCase,
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  updateEventsManager: UpdateEventsManager,
  preferences: ApplicationPreferences,
  schedulers: SuplaSchedulers
) : BaseDetailViewModel<RgbwDetailViewState, RgbwDetailViewEvent>(
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  updateEventsManager,
  preferences,
  RgbwDetailViewState(),
  schedulers
) {
  override fun closeEvent() = RgbwDetailViewEvent.Close

  override fun updatedState(state: RgbwDetailViewState, channelDataBase: ChannelDataBase) =
    state.copy(caption = getCaptionUseCase(channelDataBase.shareable))

  override fun handleChannelBase(channelDataBase: ChannelDataBase, initialFunction: SuplaFunction) {
    super.handleChannelBase(channelDataBase, initialFunction)

    (channelDataBase as? ChannelDataEntity)?.let { channelData ->
      updateState {
        it.copy(
          hasSettings = shouldShowRgbSettings(
            manufacturerId = channelData.channelEntity.manufacturerId.toInt(),
            productId = channelData.channelEntity.productId.toInt()
          )
        )
      }
    }
  }

  private fun shouldShowRgbSettings(manufacturerId: Int?, productId: Int?): Boolean =
    (manufacturerId == SuplaConst.SUPLA_MFR_DOYLETRATT && productId == 1) ||
      (manufacturerId == SuplaConst.SUPLA_MFR_ZAMEL && productId == SuplaConst.ZAM_PRODID_DIW_01) ||
      (manufacturerId == SuplaConst.SUPLA_MFR_COMELIT && productId == SuplaConst.COM_PRODID_WDIM100)
}

sealed interface RgbwDetailViewEvent : BaseDetailViewEvent {
  data object Close : RgbwDetailViewEvent
}

data class RgbwDetailViewState(
  override val caption: LocalizedString? = null,
  val hasSettings: Boolean = false
) : BaseDetailViewState(caption)
