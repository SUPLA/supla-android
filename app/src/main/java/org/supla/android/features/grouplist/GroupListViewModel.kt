package org.supla.android.features.grouplist
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

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.awaitFirst
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
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
import org.supla.android.usecases.channel.GroupActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideGroupDetailTypeUseCase
import org.supla.android.usecases.details.RgbwDetailType
import org.supla.android.usecases.details.StandardDetailType
import org.supla.android.usecases.details.ThermostatDetailType
import org.supla.android.usecases.group.CreateProfileGroupsListUseCase
import org.supla.android.usecases.group.GroupToListItemMapper
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.ReorderGroupsUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
  private val createProfileGroupsListUseCase: CreateProfileGroupsListUseCase,
  private val provideGroupDetailTypeUseCase: ProvideGroupDetailTypeUseCase,
  private val findGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val groupToListItemMapper: GroupToListItemMapper,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val reorderGroupsUseCase: ReorderGroupsUseCase,
  private val groupActionUseCase: GroupActionUseCase,
  loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase,
  updateEventsManager: UpdateEventsManager,
  dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseListViewModel<GroupListViewState, GroupListViewEvent>(
  dateProvider,
  schedulers,
  GroupListViewState(),
  loadActiveProfileUrlUseCase
),
  GroupListScope {

  override fun reloadList() = loadGroups()

  init {
    observeUpdates(updateEventsManager.observeGroupsUpdate())

    updateEventsManager.observeAllGroups()
      .attach()
      .flatMapMaybe { findGroupByRemoteIdUseCase(it) }
      .map { groupToListItemMapper(it) }
      .subscribeBy(
        onNext = { listItem ->
          updateState { it.copy(groups = it.groups?.replace(listItem)) }
        }
      )
      .disposeBySelf()
  }

  fun loadGroups() {
    createProfileGroupsListUseCase()
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(groups = it) } },
        onError = defaultErrorHandler("loadGroups()")
      )
      .disposeBySelf()
  }

  fun performAction(channelId: Int, buttonType: ButtonType) {
    groupActionUseCase(channelId, buttonType)
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
      findGroupByRemoteIdUseCase(remoteId)
        .attach()
        .subscribeBy(
          onSuccess = { openDetailsByChannelFunction(it) },
          onError = defaultErrorHandler("onListItemClick($remoteId)")
        )
        .disposeBySelf()
    }
  }

  override fun onAddGroupClick() {
    loadServerUrl {
      when (it) {
        is CloudUrl.DefaultCloud -> sendEvent(GroupListViewEvent.NavigateToSuplaCloud)
        is CloudUrl.ServerUri -> sendEvent(GroupListViewEvent.NavigateToPrivateCloud(it.url))
      }
    }
  }

  fun forceAction(remoteId: Int?, actionId: ActionId?) {
    updateState { it.copy(actionAlertDialogState = null) }

    if (remoteId != null && actionId != null) {
      executeSimpleActionUseCase.invoke(actionId, SubjectType.GROUP, remoteId)
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

  private fun openDetailsByChannelFunction(group: ChannelGroupDataEntity) {
    if (isAvailableInOffline(group).not() && group.status.offline) {
      return // do not open details for offline channels
    }

    when (val detailType = provideGroupDetailTypeUseCase(group)) {
      is LegacyDetailType -> sendEvent(GroupListViewEvent.OpenLegacyDetail(group.remoteId, detailType))
      is StandardDetailType -> sendEvent(GroupListViewEvent.OpenDetail(ItemBundle.from(group), detailType.pages))
      is RgbwDetailType -> sendEvent(GroupListViewEvent.OpenDetail(ItemBundle.from(group), detailType.pages))
      is ThermostatDetailType -> sendEvent(GroupListViewEvent.OpenDetail(ItemBundle.from(group), detailType.pages))
      else -> {} // no action
    }
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
      val groups = it.groups?.toMutableList() ?: return@updateState it
      val firstItem = groups.getOrNull(from) as? ListItem.DefaultItem ?: return@updateState it
      val secondItem = groups.getOrNull(to) as? ListItem.DefaultItem ?: return@updateState it

      if (firstItem.locationCaption == secondItem.locationCaption) {
        result = true
        groups.add(to, groups.removeAt(from))
        it.copy(groups = groups)
      } else {
        it
      }
    }

    return result
  }

  override fun onDragStopped(remoteId: Int) {
    val groups = currentState().groups ?: return
    viewModelScope.launch {
      val reorderedGroups = schedulers.io {
        reorderGroupsUseCase(groups, remoteId)
        createProfileGroupsListUseCase().awaitFirst()
      }

      updateState { it.copy(groups = reorderedGroups) }
    }
  }

  override fun onLocationClick(remoteId: Int) {
    toggleLocationUseCase(remoteId, CollapsedFlag.GROUP)
      .andThen(createProfileGroupsListUseCase())
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(groups = it) } },
        onError = defaultErrorHandler("onLocationClick($remoteId)")
      )
      .disposeBySelf()
  }

  override fun onItemClick(remoteId: Int) {
    if (isEventAllowed()) {
      findGroupByRemoteIdUseCase(remoteId)
        .attach()
        .subscribeBy(
          onSuccess = { openDetailsByChannelFunction(it) },
          onError = defaultErrorHandler("onItemClick($remoteId)")
        )
        .disposeBySelf()
    }
  }

  override fun onTitleLongClick(item: ListItem) {
    sendEvent(GroupListViewEvent.ShowGroupCaptionChangeDialog(item.remoteId, item.profileId, item.userCaption))
  }

  override fun onLocationLongClick(item: ListItem.LocationItem) {
    sendEvent(GroupListViewEvent.ShowLocationCaptionChangeDialog(item.remoteId, item.profileId, item.userCaption))
  }
}

sealed class GroupListViewEvent : ViewEvent {
  data class ShowLocationCaptionChangeDialog(val remoteId: Int, val profileId: Long, val caption: String) : GroupListViewEvent()
  data class ShowGroupCaptionChangeDialog(val remoteId: Int, val profileId: Long, val caption: String) : GroupListViewEvent()

  data object NavigateToSuplaCloud : GroupListViewEvent()
  data object NavigateToSuplaBetaCloud : GroupListViewEvent()
  data class NavigateToPrivateCloud(val url: Uri) : GroupListViewEvent()

  data class OpenLegacyDetail(val remoteId: Int, val type: LegacyDetailType) : GroupListViewEvent()
  data class OpenDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) : GroupListViewEvent()
}

data class GroupListViewState(
  val groups: List<ListItem>? = null,
  val actionAlertDialogState: ActionAlertDialogState? = null
) : ViewState()

private fun List<ListItem>.replace(item: ListItem): List<ListItem> =
  map {
    if (it is ListItem.GroupItem && it.remoteId == item.remoteId) {
      item
    } else {
      it
    }
  }
