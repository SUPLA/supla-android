package org.supla.android.scenes

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.ChannelGroup
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
class ListsEventsManager @Inject constructor(
  private val channelRepository: ChannelRepository
) {

  private val subjects: MutableMap<Id, Subject<State>> = mutableMapOf()

  fun cleanup() {
    subjects.clear()
  }

  fun emitSceneChange(sceneId: Int, state: State.Scene) {
    getSubjectForScene(sceneId).onNext(state)
  }

  fun emitChannelChange(channelId: Int) {
    getSubjectForChannel(channelId).onNext(State.Channel(channelId))
  }

  fun emitGroupChange(groupId: Int) {
    getSubjectForChannelGroup(groupId).onNext(State.Group(groupId))
  }

  fun observerScene(sceneId: Int): Observable<State.Scene> {
    return getSubjectForScene(sceneId).hide()
      .map { item -> item as State.Scene }
      .distinctUntilChanged { stateA, stateB -> stateA.executing == stateB.executing }
  }

  fun observeChannel(channelId: Int): Observable<State.Channel> {
    return getSubjectForChannel(channelId).hide()
      .map { item -> item as State.Channel }
      .map { state -> State.Channel(state.channelId, channelRepository.getChannel(state.channelId)) }
  }

  fun observeGroup(channelId: Int): Observable<State.Group> {
    return getSubjectForChannelGroup(channelId).hide()
      .map { item -> item as State.Group }
      .map { state -> State.Group(state.groupId, channelRepository.getChannelGroup(state.groupId)) }
  }

  private fun getSubjectForScene(sceneId: Int): Subject<State> {
    return getSubject(sceneId, IdType.SCENE) {
      BehaviorSubject.create<State>().also {
        it.onNext(State.Scene(false))
      }
    }
  }

  private fun getSubjectForChannel(remoteId: Int): Subject<State> {
    return getSubject(remoteId, IdType.CHANNEL) {
      BehaviorSubject.create()
    }
  }

  private fun getSubjectForChannelGroup(remoteId: Int): Subject<State> {
    return getSubject(remoteId, IdType.GROUP) {
      BehaviorSubject.create()
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

  sealed class State {
    data class Scene(val executing: Boolean, val endTime: Date? = null) : State()
    data class Channel(val channelId: Int, val channel: org.supla.android.db.Channel? = null) : State()
    data class Group(val groupId: Int, val group: ChannelGroup? = null) : State()
  }

  enum class IdType { SCENE, CHANNEL, GROUP }
  data class Id(val type: IdType, val id: Int)
}