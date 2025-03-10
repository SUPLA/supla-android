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
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.db.Channel
import org.supla.android.db.ChannelGroup
import org.supla.android.usecases.channel.ChannelToRootRelationHolderUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateEventsManager @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val sceneRepository: RoomSceneRepository,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val channelToRootRelationHolderUseCase: ChannelToRootRelationHolderUseCase
) {

  private val subjects: MutableMap<Id, Subject<State>> = mutableMapOf()

  private val channelUpdatesSubject: BehaviorSubject<Any> = BehaviorSubject.create()
  private val groupUpdatesSubject: BehaviorSubject<Any> = BehaviorSubject.create()
  private val sceneUpdatesSubject: BehaviorSubject<Any> = BehaviorSubject.create()

  @Synchronized
  fun cleanup() {
    subjects.clear()
  }

  fun emitSceneUpdate(sceneId: Int) {
    getSubjectForScene(sceneId).onNext(State.Scene)
  }

  fun emitChannelUpdate(channelId: Int) {
    getSubjectForChannel(channelId).onNext(State.Channel)

    channelToRootRelationHolderUseCase.getParents(channelId)?.let { parents ->
      parents.forEach {
        getSubjectForChannel(it).onNext(State.Channel)
      }
    }
  }

  fun emitGroupUpdate(groupId: Int) {
    getSubjectForChannelGroup(groupId).onNext(State.Group)
  }

  fun emitChannelsUpdate() {
    channelUpdatesSubject.onNext(Any())
  }

  fun emitGroupsUpdate() {
    groupUpdatesSubject.onNext(Any())
  }

  fun emitScenesUpdate() {
    sceneUpdatesSubject.onNext(Any())
  }

  fun observerScene(sceneId: Int): Observable<SceneEntity> {
    return getSubjectForScene(sceneId).hide()
      .observeOn(Schedulers.io())
      .flatMap { sceneRepository.findByRemoteId(sceneId).toObservable() }
  }

  fun observeChannel(channelId: Int): Observable<Channel> {
    return getSubjectForChannel(channelId).hide()
      .map { channelRepository.getChannel(channelId) }
  }

  fun observeChannelWithChildren(channelId: Int): Observable<ChannelWithChildren> {
    return getSubjectForChannel(channelId).hide()
      .flatMap { readChannelWithChildrenUseCase(channelId).toObservable() }
  }

  fun observeChannelEvents(channelId: Int): Observable<State> = getSubjectForChannel(channelId).hide()

  fun observeGroup(groupId: Int): Observable<ChannelGroup> {
    return getSubjectForChannelGroup(groupId).hide()
      .map { channelRepository.getChannelGroup(groupId) }
  }

  fun observeGroupEvents(groupId: Int): Observable<State> = getSubjectForChannelGroup(groupId).hide()

  fun observeChannelsUpdate(): Observable<Any> = channelUpdatesSubject.hide().debounce(200, TimeUnit.MILLISECONDS)
  fun observeGroupsUpdate(): Observable<Any> = groupUpdatesSubject.hide().debounce(200, TimeUnit.MILLISECONDS)
  fun observeScenesUpdate(): Observable<Any> = sceneUpdatesSubject.hide().debounce(200, TimeUnit.MILLISECONDS)

  private fun getSubjectForScene(sceneId: Int): Subject<State> {
    return getSubject(sceneId, IdType.SCENE) {
      BehaviorSubject.create<State>().also { it.onNext(State.Scene) }
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
    data object Scene : State()
    data object Channel : State()
    data object Group : State()
  }

  private enum class IdType { SCENE, CHANNEL, GROUP }
  private data class Id(val type: IdType, val id: Int)
}
