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

import android.os.Bundle
import androidx.annotation.IdRes
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.standarddetail.DetailPage
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.features.details.electricitymeterdetail.ElectricityMeterDetailFragment
import org.supla.android.features.details.gpmdetail.GpmDetailFragment
import org.supla.android.features.details.humiditydetail.HumidityDetailFragment
import org.supla.android.features.details.impulsecounter.ImpulseCounterDetailFragment
import org.supla.android.features.details.switchdetail.SwitchDetailFragment
import org.supla.android.features.details.thermometerdetail.ThermometerDetailFragment
import org.supla.android.features.details.thermostatdetail.ThermostatDetailFragment
import org.supla.android.features.details.valveDetail.ValveDetailFragment
import org.supla.android.features.details.windowdetail.WindowDetailFragment
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.BaseListViewModel
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.ChannelActionUseCase
import org.supla.android.usecases.channel.CreateProfileChannelsListUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.details.ContainerDetailType
import org.supla.android.usecases.details.EmDetailType
import org.supla.android.usecases.details.GpmDetailType
import org.supla.android.usecases.details.HumidityDetailType
import org.supla.android.usecases.details.IcDetailType
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideChannelDetailTypeUseCase
import org.supla.android.usecases.details.SwitchDetailType
import org.supla.android.usecases.details.ThermometerDetailType
import org.supla.android.usecases.details.ThermostatDetailType
import org.supla.android.usecases.details.ValveDetailType
import org.supla.android.usecases.details.WindowDetailType
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
  private val createProfileChannelsListUseCase: CreateProfileChannelsListUseCase,
  private val provideChannelDetailTypeUseCase: ProvideChannelDetailTypeUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val findChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val channelActionUseCase: ChannelActionUseCase,
  private val channelRepository: ChannelRepository,
  updateEventsManager: UpdateEventsManager,
  dateProvider: DateProvider,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseListViewModel<ChannelListViewState, ChannelListViewEvent>(
  preferences,
  dateProvider,
  schedulers,
  ChannelListViewState()
) {

  override fun sendReassignEvent() = sendEvent(ChannelListViewEvent.ReassignAdapter)

  override fun reloadList() = loadChannels()

  init {
    observeUpdates(updateEventsManager.observeChannelsUpdate())
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

  fun toggleLocationCollapsed(location: LocationEntity) {
    toggleLocationUseCase(location, CollapsedFlag.CHANNEL)
      .andThen(createProfileChannelsListUseCase())
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(channels = it) } },
        onError = defaultErrorHandler("toggleLocationCollapsed($location)")
      )
      .disposeBySelf()
  }

  fun swapItems(firstItem: ChannelDataBase?, secondItem: ChannelDataBase?) {
    if (firstItem == null || secondItem == null) {
      return // nothing to swap
    }

    channelRepository.reorderChannels(firstItem.id, firstItem.locationId, secondItem.id)
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
            is ActionException.ValveClosedManually -> sendEvent(ChannelListViewEvent.ShowValveClosedManuallyDialog(throwable.remoteId))
            is ActionException.ValveFloodingAlarm -> sendEvent(ChannelListViewEvent.ShowValveFloodingDialog(throwable.remoteId))
            is ActionException.ValveMotorProblemClosing ->
              sendEvent(ChannelListViewEvent.ShowValveMotorProblemDialog(throwable.remoteId, ActionId.CLOSE))

            is ActionException.ValveMotorProblemOpening ->
              sendEvent(ChannelListViewEvent.ShowValveMotorProblemDialog(throwable.remoteId, ActionId.OPEN))

            is ActionException.ChannelExceedAmperage -> sendEvent(ChannelListViewEvent.ShowAmperageExceededDialog(throwable.remoteId))
            else -> defaultErrorHandler("performAction($channelId, $buttonType)")(throwable)
          }
        }
      )
      .disposeBySelf()
  }

  fun onListItemClick(remoteId: Int) {
    if (isEventAllowed()) {
      readChannelWithChildrenUseCase(remoteId)
        .attach()
        .subscribeBy(
          onSuccess = { openDetailsByChannelFunction(it) },
          onError = defaultErrorHandler("onListItemClick($remoteId)")
        )
        .disposeBySelf()
    }
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
              ?.filterIsInstance<ListItem.ChannelItem>()
              ?.first { it.channelBase.remoteId == channel.remoteId }
              ?.channelBase = channel
          },
          onError = defaultErrorHandler("updateChannel($remoteId)")
        )
        .disposeBySelf()
    }
  }

  private fun openDetailsByChannelFunction(data: ChannelWithChildren) {
    val channel = data.channel
    if (isAvailableInOffline(channel, data.children).not() && channel.status.offline) {
      return // do not open details for offline channels
    }

    when (val detailType = provideChannelDetailTypeUseCase(data)) {
      is SwitchDetailType -> sendEvent(ChannelListViewEvent.OpenSwitchDetail(ItemBundle.from(channel), detailType.pages))
      is ThermostatDetailType -> sendEvent(ChannelListViewEvent.OpenThermostatDetail(ItemBundle.from(channel), detailType.pages))
      is ThermometerDetailType -> sendEvent(ChannelListViewEvent.OpenThermometerDetail(ItemBundle.from(channel), detailType.pages))
      is GpmDetailType -> sendEvent(ChannelListViewEvent.OpenGpmDetail(ItemBundle.from(channel), detailType.pages))
      is WindowDetailType -> sendEvent(ChannelListViewEvent.OpenWindowDetail(ItemBundle.from(channel), detailType.pages))
      is EmDetailType -> sendEvent(ChannelListViewEvent.OpenEmDetail(ItemBundle.from(channel), detailType.pages))
      is IcDetailType -> sendEvent(ChannelListViewEvent.OpenIcDetail(ItemBundle.from(channel), detailType.pages))
      is ContainerDetailType -> sendEvent(ChannelListViewEvent.OpenContainerDetail(ItemBundle.from(channel), detailType.pages))
      is HumidityDetailType -> sendEvent(ChannelListViewEvent.OpenHumidityDetail(ItemBundle.from(channel), detailType.pages))
      is ValveDetailType -> sendEvent(ChannelListViewEvent.OpenValveDetail(ItemBundle.from(channel), detailType.pages))
      is LegacyDetailType -> sendEvent(ChannelListViewEvent.OpenLegacyDetails(channel.remoteId, detailType))
      null -> {} // no action
    }
  }
}

sealed class ChannelListViewEvent : ViewEvent {
  data class ShowValveClosedManuallyDialog(val remoteId: Int) : ChannelListViewEvent()
  data class ShowValveFloodingDialog(val remoteId: Int) : ChannelListViewEvent()
  data class ShowValveMotorProblemDialog(val remoteId: Int, val action: ActionId) : ChannelListViewEvent()
  data class ShowAmperageExceededDialog(val remoteId: Int) : ChannelListViewEvent()
  data class OpenLegacyDetails(val remoteId: Int, val type: LegacyDetailType) : ChannelListViewEvent()
  data class OpenSwitchDetail(private val itemBundle: ItemBundle, private val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.switch_detail_fragment, SwitchDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenThermostatDetail(private val itemBundle: ItemBundle, private val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.thermostat_detail_fragment, ThermostatDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenThermometerDetail(private val itemBundle: ItemBundle, private val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.thermometer_detail_fragment, ThermometerDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenHumidityDetail(private val itemBundle: ItemBundle, private val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.humidity_detail_fragment, HumidityDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenGpmDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.gpm_detail_fragment, GpmDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenWindowDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.window_detail_fragment, WindowDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenEmDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.electricity_meter_detail_fragment, ElectricityMeterDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenIcDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.impulse_counter_detail_fragment, ImpulseCounterDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenContainerDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.container_detail_fragment, ImpulseCounterDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenValveDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.container_detail_fragment, ValveDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data object ReassignAdapter : ChannelListViewEvent()

  abstract class OpenStandardDetail(
    @IdRes val fragmentId: Int,
    val fragmentArguments: Bundle
  ) : ChannelListViewEvent()
}

data class ChannelListViewState(
  val channels: List<ListItem>? = null
) : ViewState()
