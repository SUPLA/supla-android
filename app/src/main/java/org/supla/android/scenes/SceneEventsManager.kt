package org.supla.android.scenes

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

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


@Singleton
class SceneEventsManager @Inject constructor() {

  private val subjects: MutableMap<Int, Subject<SceneState>> = mutableMapOf()

  fun cleanup() {
    subjects.clear()
  }

  fun emitStateChange(sceneId: Int, state: SceneState) {
    val subject =
      subjects[sceneId] ?: BehaviorSubject.create<SceneState>().also { subjects[sceneId] = it }
    subject.onNext(state)
  }

  fun observerScene(sceneId: Int): Observable<SceneState> {
    return subjects[sceneId]?.hide()
      ?.distinctUntilChanged { stateA, stateB -> stateA.executing == stateB.executing }
      ?: Observable.just(SceneState(false))
  }

  data class SceneState(
    val executing: Boolean,
    val endTime: Date? = null
  )
}