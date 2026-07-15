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
import org.supla.android.main.topbar.TopBarSearchData
import org.supla.android.main.topbar.TopBarSearchEvent
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
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
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideChannelDetailTypeUseCase
import org.supla.android.usecases.details.StandardDetailType
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
  vibrationHelper: VibrationHelper,
  dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseListViewModel<ChannelListViewState, ChannelListViewEvent>(
  vibrationHelper,
  dateProvider,
  schedulers,
  ChannelListViewState()
),
  ChannelListScope {

  override fun reloadList() = loadChannels()

  var searchData: TopBarSearchData = TopBarSearchData()
    private set

  init {
    observeUpdates(updateEventsManager.observeChannelsUpdate())

    updateEventsManager.observeAllChannels()
      .attachSilent()
      .flatMapMaybe { readChannelWithChildrenUseCase(it) }
      .map { channelToListItemMapper(it) }
      .subscribeBy(
        onNext = { updateItem(it) },
        onError = defaultErrorHandler("init()")
      )
      .disposeBySelf()
  }

  fun handle(event: TopBarSearchEvent) {
    searchData = searchData.handle(event)
    loadChannels()
  }

  override fun onStart() {
    loadChannels()
  }

  fun loadChannels() {
    createProfileChannelsListUseCase(filterString = searchData.query)
      .attach()
      .subscribeBy(
        onNext = { updateItems(it) },
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
      is StandardDetailType -> sendEvent(ChannelListViewEvent.OpenDetail(ItemBundle.from(channel), detailType.pages))
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

  override fun moveItems(from: Int, to: Int): Boolean {
    val firstItem = list.getOrNull(from) as? ListItem.DefaultItem ?: return false
    val secondItem = list.getOrNull(to) as? ListItem.DefaultItem ?: return false

    if (firstItem.locationCaption != secondItem.locationCaption) {
      return false
    }

    listState.add(to, listState.removeAt(from))
    return true
  }

  override fun onDragStopped(remoteId: Int) {
    viewModelScope.launch {
      val reorderedChannels = schedulers.io {
        reorderChannelsUseCase(list, remoteId)
        createProfileChannelsListUseCase().awaitFirst()
      }

      updateItems(reorderedChannels)
    }
  }

  override fun onLocationClick(remoteId: Int) {
    toggleLocationUseCase(remoteId, CollapsedFlag.CHANNEL)
      .andThen(createProfileChannelsListUseCase(filterString = searchData.query))
      .attach()
      .subscribeBy(
        onNext = { updateItems(it) },
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
  val actionAlertDialogState: ActionAlertDialogState? = null
) : ViewState()
