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
import org.supla.android.features.details.detailbase.StandardDetailFragment
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.details.impulsecounter.ImpulseCounterDetailFragment
import org.supla.android.features.details.thermostatdetail.ThermostatDetailFragment
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.ActionAlertDialogState
import org.supla.android.ui.dialogs.dialogState
import org.supla.android.ui.lists.BaseListViewModel
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.ChannelActionUseCase
import org.supla.android.usecases.channel.CreateProfileChannelsListUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.details.ContainerDetailType
import org.supla.android.usecases.details.EmDetailType
import org.supla.android.usecases.details.GateDetailType
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
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
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
            is ActionException.ValveClosedManually -> updateState { it.copy(actionAlertDialogState = throwable.dialogState) }
            is ActionException.ValveFloodingAlarm -> updateState { it.copy(actionAlertDialogState = throwable.dialogState) }
            is ActionException.ValveMotorProblemClosing -> updateState { it.copy(actionAlertDialogState = throwable.dialogState) }
            is ActionException.ValveMotorProblemOpening -> updateState { it.copy(actionAlertDialogState = throwable.dialogState) }
            is ActionException.ChannelExceedAmperage -> updateState { it.copy(actionAlertDialogState = throwable.dialogState) }
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

  fun forceAction(remoteId: Int?, actionId: ActionId?) {
    updateState { it.copy(actionAlertDialogState = null) }

    if (remoteId != null && actionId != null) {
      executeSimpleActionUseCase.invoke(actionId, SubjectType.CHANNEL, remoteId)
        .attachSilent()
        .subscribeBy(
          onError = defaultErrorHandler("forceAction")
        )
        .disposeBySelf()
    }
  }

  fun dismissActionDialog() {
    updateState { it.copy(actionAlertDialogState = null) }
  }

  fun showAlert(message: String) {
    updateState {
      it.copy(
        actionAlertDialogState = ActionAlertDialogState(
          messageString = message,
          positiveButtonRes = R.string.ok,
        )
      )
    }
  }

  private fun openDetailsByChannelFunction(data: ChannelWithChildren) {
    val channel = data.channel
    if (isAvailableInOffline(channel).not() && channel.status.offline) {
      return // do not open details for offline channels
    }

    when (val detailType = provideChannelDetailTypeUseCase(data)) {
      is ThermometerDetailType -> sendEvent(ChannelListViewEvent.OpenSingleHistoryDetail(ItemBundle.from(channel), detailType.pages))
      is GpmDetailType -> sendEvent(ChannelListViewEvent.OpenSingleHistoryDetail(ItemBundle.from(channel), detailType.pages))
      is HumidityDetailType -> sendEvent(ChannelListViewEvent.OpenSingleHistoryDetail(ItemBundle.from(channel), detailType.pages))

      is SwitchDetailType -> sendEvent(ChannelListViewEvent.OpenStandardDetail(ItemBundle.from(channel), detailType.pages))
      is WindowDetailType -> sendEvent(ChannelListViewEvent.OpenStandardDetail(ItemBundle.from(channel), detailType.pages))
      is EmDetailType -> sendEvent(ChannelListViewEvent.OpenStandardDetail(ItemBundle.from(channel), detailType.pages))
      is ContainerDetailType -> sendEvent(ChannelListViewEvent.OpenStandardDetail(ItemBundle.from(channel), detailType.pages))
      is ValveDetailType -> sendEvent(ChannelListViewEvent.OpenStandardDetail(ItemBundle.from(channel), detailType.pages))
      is GateDetailType -> sendEvent(ChannelListViewEvent.OpenStandardDetail(ItemBundle.from(channel), detailType.pages))

      is ThermostatDetailType -> sendEvent(ChannelListViewEvent.OpenThermostatDetail(ItemBundle.from(channel), detailType.pages))
      is IcDetailType -> sendEvent(ChannelListViewEvent.OpenIcDetail(ItemBundle.from(channel), detailType.pages))
      is LegacyDetailType -> sendEvent(ChannelListViewEvent.OpenLegacyDetails(channel.remoteId, detailType))
      null -> {} // no action
    }
  }
}

sealed class ChannelListViewEvent : ViewEvent {
  data class OpenLegacyDetails(val remoteId: Int, val type: LegacyDetailType) : ChannelListViewEvent()

  data class OpenThermostatDetail(private val itemBundle: ItemBundle, private val pages: List<DetailPage>) :
    BaseDetail(R.id.thermostat_detail_fragment, ThermostatDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenIcDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    BaseDetail(R.id.impulse_counter_detail_fragment, ImpulseCounterDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenStandardDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    BaseDetail(R.id.standard_detail_fragment, StandardDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenSingleHistoryDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    BaseDetail(R.id.single_history_detail_fragment, StandardDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data object ReassignAdapter : ChannelListViewEvent()

  abstract class BaseDetail(
    @param:IdRes val fragmentId: Int,
    val fragmentArguments: Bundle
  ) : ChannelListViewEvent()
}

data class ChannelListViewState(
  val channels: List<ListItem>? = null,
  val actionAlertDialogState: ActionAlertDialogState? = null
) : ViewState()
