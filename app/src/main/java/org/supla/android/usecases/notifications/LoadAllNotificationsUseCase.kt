package org.supla.android.usecases.notifications
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

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.supla.android.data.source.NotificationRepository
import org.supla.android.data.source.local.entity.NotificationEntity
import org.supla.android.main.topbar.searchable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadAllNotificationsUseCase @Inject constructor(
  private val notificationRepository: NotificationRepository
) {

  private val filterSubject = BehaviorSubject.create<String>().apply { onNext("") }

  fun observe(): Observable<List<NotificationEntity>> =
    Observable.combineLatest(
      notificationRepository.loadAllNotifications(),
      filterSubject
    ) { notifications, filter ->
      if (filter.searchable) {
        notifications.filter {
          it.title.contains(filter, ignoreCase = true) ||
            it.message.contains(filter, ignoreCase = true) ||
            it.profileName?.contains(filter, ignoreCase = true) == true
        }
      } else {
        notifications
      }
    }
      .doOnTerminate { filterSubject.onNext("") }

  fun filter(string: String) {
    filterSubject.onNext(string)
  }
}
