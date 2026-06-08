package org.supla.android.features.details.impulsecounter.settings
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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.list.RefreshImpulseCounterAggregatedValueUseCase
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class ImpulseCounterSettingsViewModel @Inject constructor(
  private val refreshImpulseCounterAggregatedValueUseCase: RefreshImpulseCounterAggregatedValueUseCase,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val userStateHolder: UserStateHolder,
  schedulers: SuplaSchedulers
) : BaseViewModel<ImpulseCounterSettingsViewState, ImpulseCounterSettingsViewEvent>(ImpulseCounterSettingsViewState(), schedulers),
  ImpulseCounterSettingsViewScope {

  private lateinit var item: ItemBundle

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
    item = ItemBundle.from(channelData)
    val settings = userStateHolder.getImpulseCounterSettings(item.profileId, item.remoteId)

    updateState {
      it.copy(
        title = localizedString(R.string.details_settings_title, getCaptionUseCase.invoke(channelData.shareable)),
        listValueAggregationOptions = it.listValueAggregationOptions.copy(selected = settings.showOnList),
      )
    }
  }

  override fun onListValueChanged(type: ListValueAggregation) {
    updateSettings(listValueAggregation = type)

    updateState {
      it.copy(
        listValueAggregationOptions = it.listValueAggregationOptions.copy(selected = type)
      )
    }
  }

  private fun updateSettings(listValueAggregation: ListValueAggregation? = null) {
    viewModelScope.launch {
      schedulers.io {
        val settings = userStateHolder.getImpulseCounterSettings(item.profileId, item.remoteId)

        userStateHolder.setImpulseCounterSettings(
          settings = settings.copy(
            showOnList = listValueAggregation ?: settings.showOnList,
          ),
          profileId = item.profileId,
          remoteId = item.remoteId
        )

        refreshImpulseCounterAggregatedValueUseCase(item.profileId, item.remoteId)
      }
    }
  }
}

sealed class ImpulseCounterSettingsViewEvent : ViewEvent
