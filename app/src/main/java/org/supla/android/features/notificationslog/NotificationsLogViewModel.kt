package org.supla.android.features.notificationslog
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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import org.supla.android.R
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.NotificationEntity
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.notifications.DeleteNotificationUseCase
import org.supla.android.usecases.notifications.DeleteNotificationsUseCase
import org.supla.android.usecases.notifications.LoadAllNotificationsUseCase
import org.supla.core.shared.extensions.guardLet
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val DELETE_DELAY_SECS = 5L

@HiltViewModel
class NotificationsLogViewModel @Inject constructor(
  private val loadAllNotificationsUseCase: LoadAllNotificationsUseCase,
  private val deleteNotificationsUseCase: DeleteNotificationsUseCase,
  private val deleteNotificationUseCase: DeleteNotificationUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<NotificationsLogViewState, NotificationsLogViewEvent>(
  defaultState = NotificationsLogViewState(),
  schedulers = schedulers,
  titleRes = R.string.menu_notifications
),
  NotificationsLogViewScope {

  private val deletionDisposablesMap: MutableMap<Long, Disposable> = mutableMapOf()

  var filterString: String = ""
    private set

  override fun onViewCreated() {
    loadAll()
  }

  override fun delete(entity: NotificationEntity) {
    sendEvent(NotificationsLogViewEvent.ShowDeleteNotification(entity.id))
    deletionDisposablesMap[entity.id] = Completable.complete()
      .delay(DELETE_DELAY_SECS, TimeUnit.SECONDS)
      .andThen(deleteNotificationUseCase(entity.id))
      .attachSilent()
      .doOnTerminate { deletionDisposablesMap.remove(entity.id) }
      .subscribe()
    invalidateItems()
  }

  fun loadAll() {
    if (filterString.length > 1) {
      loadAllNotificationsUseCase(filterString)
    } else {
      loadAllNotificationsUseCase()
    }
      .attach()
      .subscribeBy(
        onNext = this::setItems
      )
      .disposeBySelf()
  }

  fun setFilterString(string: String) {
    filterString = string
    loadAll()
  }

  fun cancelDeletion(id: Long) {
    deletionDisposablesMap[id]?.dispose()
    deletionDisposablesMap.remove(id)
    invalidateItems()
  }

  fun askDeleteAll() {
    updateState { it.copy(showDeletionDialog = true, deleteAction = DeleteNotificationsUseCase.Action.ALL) }
  }

  fun askDeleteOlderThanMonth() {
    updateState { it.copy(showDeletionDialog = true, deleteAction = DeleteNotificationsUseCase.Action.OLDER_THAN_MONTH) }
  }

  override fun cancelDeleteAll() {
    updateState { it.copy(showDeletionDialog = false, deleteAction = null) }
  }

  override fun deleteAll() {
    val (action) = guardLet(currentState().deleteAction) { return }

    deleteNotificationsUseCase(action)
      .attach()
      .subscribeBy(
        onComplete = { updateState { it.copy(showDeletionDialog = false, deleteAction = null) } }
      )
      .disposeBySelf()
  }

  private fun setItems(items: List<NotificationEntity>) {
    updateState { state ->
      state.copy(items = items.map { NotificationItem(it, deletionDisposablesMap.containsKey(it.id)) })
    }
  }

  private fun invalidateItems() {
    updateState { state ->
      state.copy(items = state.items.map { it.copy(deleted = deletionDisposablesMap.containsKey(it.notificationEntity.id)) })
    }
  }

  override fun onCleared() {
    deletionDisposablesMap.clear()
  }
}

data class NotificationItem(
  val notificationEntity: NotificationEntity,
  val deleted: Boolean
)

sealed class NotificationsLogViewEvent : ViewEvent {
  data class ShowDeleteNotification(val id: Long) : NotificationsLogViewEvent()
}

data class NotificationsLogViewState(
  val items: List<NotificationItem> = emptyList(),
  val showDeletionDialog: Boolean = false,
  val deleteAction: DeleteNotificationsUseCase.Action? = null
) : ViewState()
