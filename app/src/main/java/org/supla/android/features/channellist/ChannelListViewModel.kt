package org.supla.android.features.channellist
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
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.Location
import org.supla.android.events.ListsEventsManager
import org.supla.android.features.standarddetail.DetailPage
import org.supla.android.lib.SuplaChannelValue
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.BaseListViewModel
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.*
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.details.SwitchDetailType
import org.supla.android.usecases.details.ThermometerDetailType
import org.supla.android.usecases.details.ThermostatDetailType
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val createProfileChannelsListUseCase: CreateProfileChannelsListUseCase,
  private val channelActionUseCase: ChannelActionUseCase,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val provideDetailTypeUseCase: ProvideDetailTypeUseCase,
  private val findChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  listsEventsManager: ListsEventsManager,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseListViewModel<ChannelListViewState, ChannelListViewEvent>(preferences, ChannelListViewState(), schedulers) {

  override fun sendReassignEvent() = sendEvent(ChannelListViewEvent.ReassignAdapter)

  override fun reloadList() = loadChannels()

  init {
    observeUpdates(listsEventsManager.observeChannelUpdates())
  }

  fun loadChannels() {
    createProfileChannelsListUseCase()
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(channels = it) } },
        onError = defaultErrorHandler("loadChannels()")
      )
      .disposeBySelf()
  }

  fun toggleLocationCollapsed(location: Location) {
    toggleLocationUseCase(location, CollapsedFlag.CHANNEL)
      .andThen(createProfileChannelsListUseCase())
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(channels = it) } },
        onError = defaultErrorHandler("toggleLocationCollapsed($location)")
      )
      .disposeBySelf()
  }

  fun swapItems(firstItem: ChannelBase?, secondItem: ChannelBase?) {
    if (firstItem == null || secondItem == null) {
      return // nothing to swap
    }

    channelRepository.reorderChannels(firstItem.id, firstItem.locationId.toInt(), secondItem.id)
      .attach()
      .subscribeBy(
        onError = defaultErrorHandler("swapItems(..., ...)")
      )
      .disposeBySelf()
  }

  fun performAction(channelId: Int, buttonType: ButtonType) {
    channelActionUseCase(channelId, buttonType)
      .attach()
      .subscribeBy(
        onError = { throwable ->
          when (throwable) {
            is ActionException.ChannelClosedManually -> sendEvent(ChannelListViewEvent.ShowValveDialog(throwable.remoteId))
            is ActionException.ChannelExceedAmperage -> sendEvent(ChannelListViewEvent.ShowAmperageExceededDialog(throwable.remoteId))
            else -> defaultErrorHandler("performAction($channelId, $buttonType)")(throwable)
          }
        }
      )
      .disposeBySelf()
  }

  fun onListItemClick(channel: Channel) {
    openDetailsByChannelFunction(channel)
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onDataChanged -> updateChannel(message.channelId)
    }
  }

  private fun updateChannel(remoteId: Int) {
    if (remoteId > 0) {
      findChannelByRemoteIdUseCase(remoteId = remoteId)
        .attachSilent()
        .subscribeBy(
          onSuccess = { channel ->
            currentState().channels
              .filterIsInstance(ListItem.ChannelItem::class.java)
              .first { it.channelBase.remoteId == channel.remoteId }
              .channelBase = channel
          },
          onError = defaultErrorHandler("updateChannel($remoteId)")
        )
        .disposeBySelf()
    }
  }

  private fun openDetailsByChannelFunction(channel: Channel) {
    if (isAvailableInOffline(channel).not() && channel.onLine.not()) {
      return // do not open details for offline channels
    }

    if (channel.func == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT) {
      sendEvent(ChannelListViewEvent.OpenThermostatDetails)
      return
    }

    when (val detailType = provideDetailTypeUseCase(channel)) {
      is SwitchDetailType -> sendEvent(ChannelListViewEvent.OpenSwitchDetail(channel.remoteId, channel.func, detailType.pages))
      is ThermostatDetailType -> sendEvent(ChannelListViewEvent.OpenThermostatDetail(channel.remoteId, channel.func, detailType.pages))
      is ThermometerDetailType -> sendEvent(
        ChannelListViewEvent.OpenThermometerDetailType(channel.remoteId, channel.func, detailType.pages)
      )
      is LegacyDetailType -> sendEvent(ChannelListViewEvent.OpenLegacyDetails(channel.channelId, detailType))
      else -> {} // no action
    }
  }

  private fun isAvailableInOffline(channel: Channel) = when (channel.func) {
    SuplaConst.SUPLA_CHANNELFNC_THERMOMETER,
    SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE,
    SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
//    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO,
//    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_DIFFERENTIAL,
//    SuplaConst.SUPLA_CHANNELFNC_HVAC_FAN,
//    SuplaConst.SUPLA_CHANNELFNC_HVAC_DRYER,
    SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER,
    SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER -> true
    SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER -> {
      when (channel.value?.subValueType) {
        SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS.toShort(),
        SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort() -> true
        else -> false
      }
    }
    else -> false
  }
}

sealed class ChannelListViewEvent : ViewEvent {
  data class ShowValveDialog(val remoteId: Int) : ChannelListViewEvent()
  data class ShowAmperageExceededDialog(val remoteId: Int) : ChannelListViewEvent()
  data class OpenLegacyDetails(val remoteId: Int, val type: LegacyDetailType) : ChannelListViewEvent()
  data class OpenSwitchDetail(val remoteId: Int, val function: Int, val pages: List<DetailPage>) : ChannelListViewEvent()
  data class OpenThermostatDetail(val remoteId: Int, val function: Int, val pages: List<DetailPage>) : ChannelListViewEvent()
  data class OpenThermometerDetailType(var remoteId: Int, val function: Int, val pages: List<DetailPage>) : ChannelListViewEvent()
  object OpenThermostatDetails : ChannelListViewEvent()
  object ReassignAdapter : ChannelListViewEvent()
}

data class ChannelListViewState(
  val channels: List<ListItem> = emptyList()
) : ViewState()
