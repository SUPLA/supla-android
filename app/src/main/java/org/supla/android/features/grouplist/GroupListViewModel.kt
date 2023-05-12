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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelGroup
import org.supla.android.db.Location
import org.supla.android.events.ListsEventsManager
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.BaseListViewModel
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.GroupActionUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.details.DetailType
import org.supla.android.usecases.details.ProvideDetailTypeUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.location.isCollapsed
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val profileManager: ProfileManager,
  private val groupActionUseCase: GroupActionUseCase,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val provideDetailTypeUseCase: ProvideDetailTypeUseCase,
  listsEventsManager: ListsEventsManager,
  preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseListViewModel<GroupListViewState, GroupListViewEvent>(preferences, GroupListViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)

  override fun sendReassignEvent() = sendEvent(GroupListViewEvent.ReassignAdapter)

  override fun reloadList() = loadGroups()

  init {
    observeUpdates(listsEventsManager.observeGroupUpdates())
  }

  fun loadGroups() {
    reloadObservable()
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(groups = it) } }
      )
      .disposeBySelf()
  }

  fun toggleLocationCollapsed(location: Location) {
    toggleLocationUseCase(location, CollapsedFlag.GROUP)
      .andThen(reloadObservable())
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
      .attachSilent()
      .subscribeBy()
      .disposeBySelf()
  }

  fun performAction(channelId: Int, buttonType: ButtonType) {
    groupActionUseCase(channelId, buttonType)
      .attachSilent()
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

  fun onListItemClick(channelId: Int) {
    readChannelGroupByRemoteIdUseCase(channelId)
      .attach()
      .subscribeBy(
        onSuccess = { openDetailsByChannelFunction(it) }
      )
      .disposeBySelf()
  }

  private fun openDetailsByChannelFunction(group: ChannelGroup) {
    if (group.onLine.not()) {
      return // do not open details for offline channels
    }

    if (group.func == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT) {
      sendEvent(GroupListViewEvent.OpenThermostatDetails)
      return
    }

    val detailType = provideDetailTypeUseCase(group)
    if (detailType != null) {
      sendEvent(GroupListViewEvent.OpenLegacyDetails(group.groupId, detailType))
    }
  }

  private fun reloadObservable(): Observable<List<ListItem>> = Observable.fromCallable {
    channelRepository.getAllProfileChannelGroups(profileManager.getCurrentProfile().blockingGet()!!.id).use { cursor ->
      val channels = mutableListOf<ListItem>()

      var location: Location? = null
      if (cursor.moveToFirst()) {
        do {
          val group = ChannelGroup()
          group.AssignCursorData(cursor)

          if (location == null || location.locationId != group.locationId.toInt()) {
            location = channelRepository.getLocation(group.locationId.toInt())
            channels.add(ListItem.LocationItem(location))
          }

          if (location?.isCollapsed(CollapsedFlag.GROUP) == false) {
            channels.add(ListItem.ChannelItem(group))
          }
        } while (cursor.moveToNext())
      }

      return@use channels
    }
  }
}

sealed class GroupListViewEvent : ViewEvent {
  data class ShowValveDialog(val remoteId: Int) : GroupListViewEvent()
  data class ShowAmperageExceededDialog(val remoteId: Int) : GroupListViewEvent()
  data class OpenLegacyDetails(val remoteId: Int, val type: DetailType) : GroupListViewEvent()
  object OpenThermostatDetails : GroupListViewEvent()
  object ReassignAdapter : GroupListViewEvent()
}

data class GroupListViewState(
  override val loading: Boolean = false,
  val groups: List<ListItem> = emptyList()
) : ViewState(loading)
