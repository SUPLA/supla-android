package org.supla.android.events
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
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class BaseConfigEventsManager<T : Any> {

  private val subjects: MutableMap<Int, PublishSubject<T>> = mutableMapOf()

  protected fun emitConfig(remoteId: Int, config: T) {
    getSubject(remoteId).run {
      onNext(config)
    }
  }

  fun observerConfig(remoteId: Int): Observable<T> = getSubject(remoteId).hide()

  @Synchronized
  protected fun getSubject(remoteId: Int): PublishSubject<T> {
    val subject = subjects[remoteId]
    if (subject != null) {
      return subject
    }

    return PublishSubject.create<T>().also {
      subjects[remoteId] = it
    }
  }
}
