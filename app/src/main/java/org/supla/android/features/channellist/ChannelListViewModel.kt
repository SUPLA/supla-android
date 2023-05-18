package org.supla.android.features.channellist

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
import org.supla.android.lib.SuplaChannelValue
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.BaseListViewModel
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.*
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.details.StandardDetailType
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
        onNext = { updateState { state -> state.copy(channels = it) } }
      )
      .disposeBySelf()
  }

  fun toggleLocationCollapsed(location: Location) {
    toggleLocationUseCase(location, CollapsedFlag.CHANNEL)
      .andThen(createProfileChannelsListUseCase())
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(channels = it) } }
      )
      .disposeBySelf()
  }

  fun swapItems(firstItem: ChannelBase?, secondItem: ChannelBase?) {
    if (firstItem == null || secondItem == null) {
      return // nothing to swap
    }

    channelRepository.reorderChannels(firstItem.id, firstItem.locationId.toInt(), secondItem.id)
      .attach()
      .subscribeBy()
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
          }
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
      StandardDetailType.SWITCH -> sendEvent(ChannelListViewEvent.OpenSwitchDetails(channel.remoteId))
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
  data class OpenSwitchDetails(val remoteId: Int) : ChannelListViewEvent()
  object OpenThermostatDetails : ChannelListViewEvent()
  object ReassignAdapter : ChannelListViewEvent()
  data class UpdateChannel(val channel: Channel) : ChannelListViewEvent()
}

data class ChannelListViewState(
  val channels: List<ListItem> = emptyList()
) : ViewState()
