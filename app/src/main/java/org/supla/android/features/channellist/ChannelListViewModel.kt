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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.main.topbar.TopBarSearchData
import org.supla.android.main.topbar.TopBarSearchEvent
import org.supla.android.tools.SuplaThreading
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
import org.supla.android.usecases.list.TriggerLogHistoryDownloadUseCase
import org.supla.android.usecases.list.canMoveItemWithinSection
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ChannelListViewModel @Inject constructor(
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val createProfileChannelsListUseCase: CreateProfileChannelsListUseCase,
  private val triggerLogHistoryDownloadUseCase: TriggerLogHistoryDownloadUseCase,
  private val provideChannelDetailTypeUseCase: ProvideChannelDetailTypeUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val channelToListItemMapper: ChannelToListItemMapper,
  private val reorderChannelsUseCase: ReorderChannelsUseCase,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val channelActionUseCase: ChannelActionUseCase,
  vibrationHelper: VibrationHelper,
  dateProvider: DateProvider,
  threading: SuplaThreading
) : BaseListViewModel<ChannelListViewState, ChannelListViewEvent>(
  vibrationHelper,
  dateProvider,
  threading,
  ChannelListViewState()
),
  ChannelListScope {

  var searchData: TopBarSearchData = TopBarSearchData()
    private set

  private var downloadJob: Job? = null
  private val listReloadSubject = BehaviorSubject.createDefault("")

  init {
    observeListUpdates()
    observeProcessingUpdates()
  }

  fun handle(event: TopBarSearchEvent) {
    searchData = searchData.handle(event)
    listReloadSubject.onNext(searchData.query)
  }

  override fun onStart() {
    startLogHistoryDownload()
  }

  override fun onStop() {
    stopLogHistoryDownload()
  }

  fun startLogHistoryDownload() {
    if (downloadJob?.isActive == true) return

    downloadJob = viewModelScope.launch(threading.dispatchers.io) {
      delay(5.seconds)
      while (isActive) {
        triggerLogHistoryDownloadUseCase()
        delay(30.seconds)
      }
    }
  }

  fun stopLogHistoryDownload() {
    downloadJob?.cancel()
    downloadJob = null
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
    if (!list.canMoveItemWithinSection(from, to) { it is ListItem.DefaultItem }) {
      return false
    }

    listState.add(to, listState.removeAt(from))
    return true
  }

  override fun onDragStopped(remoteId: Int) {
    val items = list.toList()
    viewModelScope.launch {
      threading.io {
        reorderChannelsUseCase(items, remoteId)
      }
    }
  }

  override fun onLocationClick(remoteId: Int) {
    toggleLocationUseCase(remoteId, CollapsedFlag.CHANNEL)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("onLocationClick($remoteId)"))
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
      readChannelWithChildrenUseCase(remoteId).firstElement()
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

  private fun observeListUpdates() {
    listReloadSubject
      .debounce(250, TimeUnit.MILLISECONDS, threading.schedulers.computation)
      .distinctUntilChanged()
      .switchMap { filterString ->
        createProfileChannelsListUseCase(filterString)
          .attach()
          .doOnError(defaultErrorHandler("observeChannels()"))
          .onErrorResumeNext { _: Throwable -> Observable.empty() }
      }
      .subscribeBy(
        onNext = this::updateItemsAtomically
      )
      .disposeBySelf()
  }

  private fun observeProcessingUpdates() {
    downloadEventsManager.observeDefaultProgressUpdates()
      .attachSilent()
      .flatMapMaybe { remoteId ->
        readChannelWithChildrenUseCase(remoteId)
          .firstElement()
          .onErrorComplete { it is NoSuchElementException }
      }
      .map { channelToListItemMapper(it) }
      .subscribeBy(
        onNext = this::updateDefaultItem,
        onError = defaultErrorHandler("observeProcessingUpdates()")
      )
      .disposeBySelf()
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
