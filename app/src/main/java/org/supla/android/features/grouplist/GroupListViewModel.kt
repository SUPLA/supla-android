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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelGroup
import org.supla.android.db.Location
import org.supla.android.events.ListsEventsManager
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.BaseListViewModel
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.*
import org.supla.android.usecases.details.LegacyDetailType
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val createProfileGroupsListUseCase: CreateProfileGroupsListUseCase,
  private val groupActionUseCase: GroupActionUseCase,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val provideDetailTypeUseCase: ProvideDetailTypeUseCase,
  private val findGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  listsEventsManager: ListsEventsManager,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseListViewModel<GroupListViewState, GroupListViewEvent>(preferences, GroupListViewState(), schedulers) {

  override fun sendReassignEvent() = sendEvent(GroupListViewEvent.ReassignAdapter)

  override fun reloadList() = loadGroups()

  init {
    observeUpdates(listsEventsManager.observeGroupUpdates())
  }

  fun loadGroups() {
    createProfileGroupsListUseCase()
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(groups = it) } }
      )
      .disposeBySelf()
  }

  fun toggleLocationCollapsed(location: Location) {
    toggleLocationUseCase(location, CollapsedFlag.GROUP)
      .andThen(createProfileGroupsListUseCase())
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(groups = it) } }
      )
      .disposeBySelf()
  }

  fun swapItems(firstItem: ChannelBase?, secondItem: ChannelBase?) {
    if (firstItem == null || secondItem == null) {
      return // nothing to swap
    }

    channelRepository.reorderChannelGroups(firstItem.id, firstItem.locationId.toInt(), secondItem.id)
      .attach()
      .subscribeBy()
      .disposeBySelf()
  }

  fun performAction(channelId: Int, buttonType: ButtonType) {
    groupActionUseCase(channelId, buttonType)
      .attach()
      .subscribeBy(
        onError = { throwable ->
          when (throwable) {
            is ActionException.ChannelClosedManually -> sendEvent(GroupListViewEvent.ShowValveDialog(throwable.remoteId))
            is ActionException.ChannelExceedAmperage -> sendEvent(GroupListViewEvent.ShowAmperageExceededDialog(throwable.remoteId))
          }
        }
      )
      .disposeBySelf()
  }

  fun onListItemClick(channelGroup: ChannelGroup) {
    openDetailsByChannelFunction(channelGroup)
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onDataChanged -> updateGroup(message.channelGroupId)
    }
  }

  private fun updateGroup(remoteId: Int) {
    if (remoteId > 0) {
      findGroupByRemoteIdUseCase(remoteId = remoteId)
        .attachSilent()
        .subscribeBy(
          onSuccess = { channel ->
            currentState().groups
              .filterIsInstance(ListItem.ChannelItem::class.java)
              .first { it.channelBase.remoteId == channel.remoteId }
              .channelBase = channel
          }
        )
        .disposeBySelf()
    }
  }

  private fun openDetailsByChannelFunction(group: ChannelGroup) {
    if (group.onLine.not()) {
      return // do not open details for offline channels
    }

    if (group.func == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT) {
      sendEvent(GroupListViewEvent.OpenThermostatDetails)
      return
    }

    when (val detailType = provideDetailTypeUseCase(group)) {
      //StandardDetailType.SWITCH -> sendEvent(GroupListViewEvent.OpenSwitchDetails(channel.remoteId))
      is LegacyDetailType -> sendEvent(GroupListViewEvent.OpenLegacyDetails(group.groupId, detailType))
      else -> {} // no action
    }
  }
}

sealed class GroupListViewEvent : ViewEvent {
  data class ShowValveDialog(val remoteId: Int) : GroupListViewEvent()
  data class ShowAmperageExceededDialog(val remoteId: Int) : GroupListViewEvent()
  data class OpenLegacyDetails(val remoteId: Int, val type: LegacyDetailType) : GroupListViewEvent()
  object OpenThermostatDetails : GroupListViewEvent()
  object ReassignAdapter : GroupListViewEvent()
}

data class GroupListViewState(
  val groups: List<ListItem> = emptyList()
) : ViewState()
