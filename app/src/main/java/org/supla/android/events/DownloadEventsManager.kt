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
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadEventsManager @Inject constructor() {

  private val subjects: MutableMap<Id, Subject<State>> = mutableMapOf()

  fun emitProgressState(remoteId: Int, state: State) {
    getSubjectForChannel(remoteId).onNext(state)
  }

  fun observeProgress(remoteId: Int): Observable<State> {
    return getSubjectForChannel(remoteId).hide()
  }

  private fun getSubjectForChannel(remoteId: Int): Subject<State> {
    return getSubject(remoteId, IdType.CHANNEL) {
      BehaviorSubject.create<State>().also { it.onNext(State.Idle) }
    }
  }

  @Synchronized
  private fun getSubject(id: Int, type: IdType, newSubjectProvider: () -> Subject<State>): Subject<State> {
    val subjectId = Id(type, id)
    val subject = subjects[subjectId]

    if (subject != null) {
      return subject
    }

    return newSubjectProvider().also {
      subjects[subjectId] = it
    }
  }

  sealed class State(val order: Int) {
    object Idle : State(0)
    object Started : State(1)
    data class InProgress(
      val progress: Float
    ) : State(2)
    object Failed : State(3)
    object Finished : State(4)
  }

  private enum class IdType { CHANNEL }
  private data class Id(val type: IdType, val id: Int)
}
