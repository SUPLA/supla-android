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
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.detailbase.standarddetail.DetailPage
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.features.details.windowdetail.WindowDetailFragment
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
import org.supla.android.usecases.details.ThermostatDetailType
import org.supla.android.usecases.details.WindowDetailType
import org.supla.android.usecases.group.CreateProfileGroupsListUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
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
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val groupActionUseCase: GroupActionUseCase,
  private val channelRepository: ChannelRepository,
  loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase,
  updateEventsManager: UpdateEventsManager,
  dateProvider: DateProvider,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseListViewModel<GroupListViewState, GroupListViewEvent>(
  preferences,
  dateProvider,
  schedulers,
  GroupListViewState(),
  loadActiveProfileUrlUseCase
) {

  override fun sendReassignEvent() = sendEvent(GroupListViewEvent.ReassignAdapter)

  override fun reloadList() = loadGroups()

  init {
    observeUpdates(updateEventsManager.observeGroupsUpdate())
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

  fun toggleLocationCollapsed(location: LocationEntity) {
    toggleLocationUseCase(location, CollapsedFlag.GROUP)
      .andThen(createProfileGroupsListUseCase())
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(groups = it) } },
        onError = defaultErrorHandler("toggleLocationCollapsed($location)")
      )
      .disposeBySelf()
  }

  fun swapItems(firstItem: ChannelDataBase?, secondItem: ChannelDataBase?) {
    if (firstItem == null || secondItem == null) {
      return // nothing to swap
    }

    channelRepository.reorderChannelGroups(firstItem.id, firstItem.locationId, secondItem.id)
      .attach()
      .subscribeBy(
        onError = defaultErrorHandler("swapItems(..., ...)")
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

  fun onAddGroupClick() {
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

  fun updateGroup(remoteId: Int) {
    if (remoteId > 0) {
      findGroupByRemoteIdUseCase(remoteId = remoteId)
        .attachSilent()
        .subscribeBy(
          onSuccess = { channel ->
            currentState().groups
              ?.filterIsInstance<ListItem.ChannelItem>()
              ?.firstOrNull { it.channelBase.remoteId == channel.remoteId }
              ?.channelBase = channel
          },
          onError = defaultErrorHandler("updateGroup($remoteId)")
        )
        .disposeBySelf()
    }
  }

  private fun openDetailsByChannelFunction(group: ChannelGroupDataEntity) {
    if (isAvailableInOffline(group).not() && group.status.offline) {
      return // do not open details for offline channels
    }

    when (val detailType = provideGroupDetailTypeUseCase(group)) {
      is LegacyDetailType -> sendEvent(GroupListViewEvent.OpenLegacyDetails(group.remoteId, detailType))
      is WindowDetailType -> sendEvent(GroupListViewEvent.OpenRollerShutterDetail(ItemBundle.from(group), detailType.pages))
      is ThermostatDetailType -> sendEvent(GroupListViewEvent.OpenHeatpolThermostatDetail(ItemBundle.from(group), detailType.pages))
      else -> {} // no action
    }
  }
}

sealed class GroupListViewEvent : ViewEvent {
  data class OpenLegacyDetails(val remoteId: Int, val type: LegacyDetailType) : GroupListViewEvent()
  data object ReassignAdapter : GroupListViewEvent()
  data object NavigateToSuplaCloud : GroupListViewEvent()
  data object NavigateToSuplaBetaCloud : GroupListViewEvent()
  data class NavigateToPrivateCloud(val url: Uri) : GroupListViewEvent()

  data class OpenRollerShutterDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.window_detail_fragment, WindowDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  data class OpenHeatpolThermostatDetail(val itemBundle: ItemBundle, val pages: List<DetailPage>) :
    OpenStandardDetail(R.id.window_detail_fragment, WindowDetailFragment.bundle(itemBundle, pages.toTypedArray()))

  abstract class OpenStandardDetail(
    @IdRes val fragmentId: Int,
    val fragmentArguments: Bundle
  ) : GroupListViewEvent()
}

data class GroupListViewState(
  val groups: List<ListItem>? = null,
  val actionAlertDialogState: ActionAlertDialogState? = null
) : ViewState()
