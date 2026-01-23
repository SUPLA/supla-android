package org.supla.android.features.details.electricitymeterdetail.settings
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
import org.supla.android.R
import org.supla.android.core.shared.shareable
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.electricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.model.electricitymeter.ElectricityMeterSettings
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class ElectricityMeterSettingsViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val userStateHolder: UserStateHolder,
  schedulers: SuplaSchedulers
) : BaseViewModel<ElectricityMeterSettingsViewModelState, ElectricityMeterSettingsViewEvent>(
  ElectricityMeterSettingsViewModelState(),
  schedulers
) {

  fun loadData(remoteId: Int) {
    readChannelByRemoteIdUseCase(remoteId)
      .attach()
      .subscribeBy(
        onSuccess = this::handleChannel,
        onError = defaultErrorHandler("loadData")
      )
      .disposeBySelf()
  }

  private fun handleChannel(channelData: ChannelDataEntity) {
    val (measuredValues) = guardLet(channelData.Electricity.measuredTypes) { return }
    val settings = userStateHolder.getElectricityMeterSettings(channelData.profileId, channelData.remoteId)

    val hasForwardEnergy = channelData.Electricity.measuredTypes.contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY)
    val hasReverseEnergy = channelData.Electricity.measuredTypes.contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY)
    val balancingItems = if ((hasReverseEnergy && hasForwardEnergy) || channelData.Electricity.hasBalance) {
      ElectricityMeterSettings.balancingAllItems.filter {
        when (it) {
          ElectricityMeterBalanceType.VECTOR -> channelData.Electricity.hasBalance
          else -> true
        }
      }
    } else {
      null
    }
    val selectedBalance = balancingItems?.firstOrNull { it == settings.balancing } ?: balancingItems?.first()
    val balancingList = if (balancingItems != null && selectedBalance != null && balancingItems.size > 1) {
      SingleSelectionList(
        selected = selectedBalance,
        items = balancingItems,
        label = R.string.details_em_last_month_balancing
      )
    } else {
      null
    }

    updateState { state ->
      state.copy(
        remoteId = channelData.remoteId,
        profileId = channelData.profileId,
        viewState = ElectricityMeterSettingsViewState(
          channelName = getCaptionUseCase(channelData.shareable),
          onListOptions = SingleSelectionList(
            selected = settings.showOnListSafe,
            items = ElectricityMeterSettings.showOnListAllItems.filter { measuredValues.contains(it) },
            label = R.string.details_em_settings_list_item
          ),
          balancing = balancingList
        )
      )
    }
  }

  fun onListValueChange(item: SuplaElectricityMeasurementType) {
    updateState {
      val settings = userStateHolder.getElectricityMeterSettings(it.profileId, it.remoteId)
      userStateHolder.setElectricityMeterSettings(settings.copy(showOnList = item), it.profileId, it.remoteId)

      it.copy(viewState = it.viewState.copy(onListOptions = it.viewState.onListOptions?.copy(selected = item)))
    }
  }

  fun onBalanceValueChange(item: ElectricityMeterBalanceType) {
    updateState {
      val settings = userStateHolder.getElectricityMeterSettings(it.profileId, it.remoteId)
      userStateHolder.setElectricityMeterSettings(settings.copy(balancing = item), it.profileId, it.remoteId)

      it.copy(viewState = it.viewState.copy(balancing = it.viewState.balancing?.copy(selected = item)))
    }
  }
}

sealed class ElectricityMeterSettingsViewEvent : ViewEvent

data class ElectricityMeterSettingsViewModelState(
  val remoteId: Int = 0,
  val profileId: Long = 0,
  val viewState: ElectricityMeterSettingsViewState = ElectricityMeterSettingsViewState()
) : ViewState()
