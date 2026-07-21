package org.supla.android.features.details.detailbase.base
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
import kotlinx.coroutines.rx3.awaitFirst
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.shared.shareable
import org.supla.android.core.storage.RuntimeStateHolder
import org.supla.android.core.ui.EventBasedViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.tools.SuplaSchedulers
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
  suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper,
  private val channelGroupRepository: ChannelGroupRepository,
  private val runtimeStateHolder: RuntimeStateHolder,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val channelRepository: ChannelRepository,
  private val schedulers: SuplaSchedulers
) : EventBasedViewModel<DetailViewEvent>(manageScreenTitle = true) {

  init {
    setupSuplaClientMessageHandler(suplaClientMessageHandlerWrapper)
  }

  private lateinit var item: ItemBundle

  private var function: SuplaFunction? = null
  private var subfunction: ThermostatSubfunction? = null

  fun setup(item: ItemBundle) {
    this.item = item
    loadData()
  }

  fun firstPage(channelId: Int, pages: List<DetailPage>): DetailPage {
    val pageIdx = runtimeStateHolder.getDetailOpenedPage(channelId)

    return if (pageIdx >= 0 && pageIdx < pages.size) {
      pages[pageIdx]
    } else {
      pages.first()
    }
  }

  fun onPageChanged(newPage: DetailPage, pages: List<DetailPage>): DetailPage {
    runtimeStateHolder.setDetailOpenedPage(item.remoteId, pages.indexOf(newPage))
    return newPage
  }

  override fun handleSuplaMessage(message: SuplaClientMessage) {
    if (::item.isInitialized && message.isDataChange(item)) {
      loadData()
    }
  }

  private fun loadData() {
    viewModelScope.launch {
      val data = schedulers.io {
        runCatching {
          when (item.itemType) {
            ItemType.CHANNEL -> channelRepository.findChannelDataEntity(item.remoteId)
            ItemType.GROUP -> channelGroupRepository.findGroupDataEntity(item.remoteId)
          }.awaitFirst()
        }.getOrNull()
      }

      data?.let {
        if (function == null) {
          setScreenTitle(getCaptionUseCase(it.shareable))
          function = it.function
          subfunction = it.subfunction
        } else if (function != it.function || subfunction != it.subfunction) {
          sendEvent(DetailViewEvent.Close)
        }
      }
    }
  }
}

sealed interface DetailViewEvent : ViewEvent {
  data object Close : DetailViewEvent
}

private val SuplaFunction.isHvac: Boolean
  get() = when (this) {
    SuplaFunction.HVAC_THERMOSTAT,
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
    SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL -> true
    else -> false
  }

private val ChannelDataBase.subfunction: ThermostatSubfunction?
  get() {
    if (this is ChannelDataEntity && function.isHvac) {
      return channelValueEntity.asThermostatValue().subfunction
    }

    return null
  }

private fun SuplaClientMessage.isDataChange(item: ItemBundle): Boolean {
  return (item.itemType == ItemType.CHANNEL && this is SuplaClientMessage.ChannelDataChanged && channelId == item.remoteId) ||
    (item.itemType == ItemType.GROUP && this is SuplaClientMessage.GroupDataChanged && groupId == item.remoteId)
}
