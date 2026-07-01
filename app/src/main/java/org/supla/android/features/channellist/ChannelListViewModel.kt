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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.awaitFirst
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.events.UpdateEventsManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
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
import org.supla.android.usecases.channel.ChannelToListItemMapper
import org.supla.android.usecases.channel.CreateProfileChannelsListUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.channel.ReorderChannelsUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.details.GpmDetailType
import org.supla.android.usecases.details.HumidityDetailType
import org.supla.android.usecases.details.IcDetailType
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideChannelDetailTypeUseCase
import org.supla.android.usecases.details.RgbwDetailType
import org.supla.android.usecases.details.StandardDetailType
import org.supla.android.usecases.details.ThermometerDetailType
import org.supla.android.usecases.details.ThermostatDetailType
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
  private val createProfileChannelsListUseCase: CreateProfileChannelsListUseCase,
  private val provideChannelDetailTypeUseCase: ProvideChannelDetailTypeUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val channelToListItemMapper: ChannelToListItemMapper,
  private val reorderChannelsUseCase: ReorderChannelsUseCase,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val channelActionUseCase: ChannelActionUseCase,
  updateEventsManager: UpdateEventsManager,
  dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseListViewModel<ChannelListViewState, ChannelListViewEvent>(
  dateProvider,
  schedulers,
  ChannelListViewState()
),
  ChannelListScope {

  override fun reloadList() = loadChannels()

  init {
    observeUpdates(updateEventsManager.observeChannelsUpdate())

    updateEventsManager.observeAllChannels()
      .attach()
      .flatMapMaybe { readChannelWithChildrenUseCase(it) }
      .map { channelToListItemMapper(it) }
      .subscribeBy(
        onNext = { listItem ->
          updateState { it.copy(channels = it.channels?.replace(listItem)) }
        }
      )
      .disposeBySelf()
  }

  override fun onStart() {
    loadChannels()
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

  private fun openDetailsByChannelFunction(data: ChannelWithChildren) {
    val channel = data.channel
    if (isAvailableInOffline(channel).not() && channel.status.offline) {
      return // do not open details for offline channels
    }

    when (val detailType = provideChannelDetailTypeUseCase(data)) {
      is ThermometerDetailType -> sendEvent(ChannelListViewEvent.OpenDetail(ItemBundle.from(channel), detailType.pages))
      is GpmDetailType -> sendEvent(ChannelListViewEvent.OpenDetail(ItemBundle.from(channel), detailType.pages))
      is HumidityDetailType -> sendEvent(ChannelListViewEvent.OpenDetail(ItemBundle.from(channel), detailType.pages))
      is StandardDetailType -> sendEvent(ChannelListViewEvent.OpenDetail(ItemBundle.from(channel), detailType.pages))
      is ThermostatDetailType -> sendEvent(ChannelListViewEvent.OpenDetail(ItemBundle.from(channel), detailType.pages))
      is IcDetailType -> sendEvent(ChannelListViewEvent.OpenDetail(ItemBundle.from(channel), detailType.pages))
      is RgbwDetailType -> sendEvent(ChannelListViewEvent.OpenDetail(ItemBundle.from(channel), detailType.pages))
      is LegacyDetailType -> sendEvent(ChannelListViewEvent.OpenLegacyDetail(channel.remoteId, detailType))
      null -> {} // no action
    }
  }

  override fun onDeviceCatalogClick() {
    sendEvent(ChannelListViewEvent.NavigateToDeviceCatalog)
  }

  override fun onAddDeviceClick() {
    sendEvent(ChannelListViewEvent.NavigateToAddDevice)
  }

  override fun onLeftButtonClick(remoteId: Int) {
    performAction(remoteId, ButtonType.LEFT)
  }

  override fun onRightButtonClick(remoteId: Int) {
    performAction(remoteId, ButtonType.RIGHT)
  }

  override fun swapItems(from: Int, to: Int): Boolean {
    var result = false
    updateState {
      val channels = it.channels?.toMutableList() ?: return@updateState it
      val firstItem = channels.getOrNull(from) as? ListItem.DefaultItem ?: return@updateState it
      val secondItem = channels.getOrNull(to) as? ListItem.DefaultItem ?: return@updateState it

      if (firstItem.locationCaption == secondItem.locationCaption) {
        result = true
        channels.add(to, channels.removeAt(from))
        it.copy(channels = channels)
      } else {
        it
      }
    }

    return result
  }

  override fun onDragStopped(remoteId: Int) {
    val channels = currentState().channels ?: return
    viewModelScope.launch {
      val reorderedChannels = schedulers.io {
        reorderChannelsUseCase(channels, remoteId)
        createProfileChannelsListUseCase().awaitFirst()
      }

      updateState { it.copy(channels = reorderedChannels) }
    }
  }

  override fun onLocationClick(remoteId: Int) {
    toggleLocationUseCase(remoteId, CollapsedFlag.CHANNEL)
      .andThen(createProfileChannelsListUseCase())
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(channels = it) } },
        onError = defaultErrorHandler("onLocationClick($remoteId)")
      )
      .disposeBySelf()
  }

  override fun onInfoClick(remoteId: Int) {
    sendEvent(ChannelListViewEvent.ShowInfoDialog(remoteId))
  }

  override fun onIssueClick(message: String) {
    updateState {
      it.copy(
        actionAlertDialogState = ActionAlertDialogState(
          messageString = message,
          positiveButtonRes = R.string.ok,
        )
      )
    }
  }

  override fun onItemClick(remoteId: Int) {
    if (isEventAllowed()) {
      readChannelWithChildrenUseCase(remoteId)
        .attach()
        .subscribeBy(
          onSuccess = { openDetailsByChannelFunction(it) },
          onError = defaultErrorHandler("onItemClick($remoteId)")
        )
        .disposeBySelf()
    }
  }

  override fun onTitleLongClick(item: ListItem) {
    sendEvent(ChannelListViewEvent.ShowChannelCaptionChangeDialog(item.remoteId, item.profileId, item.userCaption))
  }

  override fun onLocationLongClick(item: ListItem.LocationItem) {
    sendEvent(ChannelListViewEvent.ShowLocationCaptionChangeDialog(item.remoteId, item.profileId, item.userCaption))
  }
}

sealed class ChannelListViewEvent : ViewEvent {
  data object NavigateToDeviceCatalog : ChannelListViewEvent()
  data object NavigateToAddDevice : ChannelListViewEvent()
  data class ShowLocationCaptionChangeDialog(val remoteId: Int, val profileId: Long, val caption: String) : ChannelListViewEvent()
  data class ShowChannelCaptionChangeDialog(val remoteId: Int, val profileId: Long, val caption: String) : ChannelListViewEvent()
  data class ShowInfoDialog(val remoteId: Int) : ChannelListViewEvent()
  data class OpenLegacyDetail(val remoteId: Int, val type: LegacyDetailType) : ChannelListViewEvent()
  data class OpenDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) : ChannelListViewEvent()
}

data class ChannelListViewState(
  val channels: List<ListItem>? = null,
  val actionAlertDialogState: ActionAlertDialogState? = null
) : ViewState()

private fun List<ListItem>.replace(item: ListItem): List<ListItem> =
  map {
    if (it is ListItem.DefaultItem && it.remoteId == item.remoteId) {
      item
    } else {
      it
    }
  }
