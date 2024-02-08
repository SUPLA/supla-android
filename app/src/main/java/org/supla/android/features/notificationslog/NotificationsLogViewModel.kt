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
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.NotificationEntity
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.notifications.DeleteAllNotificationsUseCase
import org.supla.android.usecases.notifications.DeleteNotificationUseCase
import org.supla.android.usecases.notifications.LoadAllNotificationsUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class NotificationsLogViewModel @Inject constructor(
  private val loadAllNotificationsUseCase: LoadAllNotificationsUseCase,
  private val deleteNotificationUseCase: DeleteNotificationUseCase,
  private val deleteAllNotificationsUseCase: DeleteAllNotificationsUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<NotificationsLogViewState, NotificationsLogViewEvent>(NotificationsLogViewState(), schedulers),
  NotificationsLogViewProxy {

  private val deletionDisposablesMap: MutableMap<Long, Disposable> = mutableMapOf()

  override fun onViewCreated() {
    loadAllNotificationsUseCase()
      .attach()
      .subscribeBy(
        onNext = { items ->
          updateState { it.copy(items = items) }
        }
      )
      .disposeBySelf()
  }

  override fun delete(entity: NotificationEntity) {
    deletionDisposablesMap[entity.id] = Completable.complete()
      .delay(5, TimeUnit.SECONDS)
      .andThen(deleteNotificationUseCase(entity.id))
      .attachSilent()
      .doOnTerminate { deletionDisposablesMap.remove(entity.id) }
      .subscribe()
  }

  override fun cancelDeletion(entity: NotificationEntity) {
    deletionDisposablesMap[entity.id]?.dispose()
  }

  override fun askDeleteAll() {
    updateState { it.copy(showDeletionDialog = true) }
  }

  override fun cancelDeleteAll() {
    updateState { it.copy(showDeletionDialog = false) }
  }

  override fun deleteAll() {
    deleteAllNotificationsUseCase()
      .attach()
      .subscribeBy(
        onComplete = { updateState { it.copy(showDeletionDialog = false) } }
      )
      .disposeBySelf()
  }
}

sealed class NotificationsLogViewEvent : ViewEvent

data class NotificationsLogViewState(
  val items: List<NotificationEntity> = emptyList(),
  val showDeletionDialog: Boolean = false
) : ViewState()
