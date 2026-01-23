package org.supla.android.features.androidauto.add
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
import io.reactivex.rxjava3.core.Single
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.data.model.spinner.SubjectItemConversionScope
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity
import org.supla.android.extensions.subscribeBy
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.usecase.GetCaptionUseCase
import java.util.Objects
import javax.inject.Inject

@HiltViewModel
class AddAndroidAutoItemViewModel @Inject constructor(
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val androidAutoItemRepository: AndroidAutoItemRepository,
  private val readAllProfilesUseCase: ReadAllProfilesUseCase,
  private val channelGroupRepository: ChannelGroupRepository,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getSceneIconUseCase: GetSceneIconUseCase,
  private val channelRepository: RoomChannelRepository,
  override val getCaptionUseCase: GetCaptionUseCase,
  private val sceneRepository: RoomSceneRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<AddAndroidAutoItemViewModelState, AddAndroidAutoItemViewEvent>(AddAndroidAutoItemViewModelState(), schedulers),
  AddAndroidAutoItemScope,
  SubjectItemConversionScope {

  fun onViewCreated(itemId: Long?) {
    if (itemId == null) {
      loadForAdd()
    } else {
      loadForEdit(itemId)
    }
  }

  private fun loadForAdd() {
    readAllProfilesUseCase()
      .flatMapSingle { profiles ->
        profiles.firstOrNull { it.active == true }?.id?.let { profileId ->
          return@flatMapSingle getSubjectsSource(profileId, SubjectType.CHANNEL).map { Pair(profiles, it) }
        }
        Single.just(Pair(profiles, emptyList()))
      }
      .attach()
      .subscribeBy(
        onNext = { (profiles, subjects) ->
          val activeProfile = profiles.first { it.active == true }
          updateState { state ->
            state.copy(
              viewState = state.viewState.copy(
                profiles = profiles.asSingleSelectionList(activeProfile.id),
                subjects = subjects.asSingleSelectionList(SubjectType.CHANNEL),
                caption = null,
                actions = subjects.firstOrNull { it.isLocation.not() }?.actionsList()
              )
            )
          }
        }
      )
      .disposeBySelf()
  }

  private fun loadForEdit(itemId: Long) {
    androidAutoItemRepository.findById(itemId)
      .flatMap { item -> readAllProfilesUseCase().map { profiles -> Pair(item, profiles) } }
      .flatMapSingle { (item, profiles) -> getSubjectsSource(item.profileId, item.subjectType).map { Triple(item, profiles, it) } }
      .attach()
      .subscribeBy(
        onNext = { (item, profiles, subjects) ->
          updateState { state ->
            state.copy(
              viewState = state.viewState.copy(
                profiles = profiles.asSingleSelectionList(item.profileId),
                subjects = subjects.asSingleSelectionList(item.subjectType, item.subjectId),
                subjectType = item.subjectType,
                caption = item.caption,
                actions = subjects.firstOrNull { it.id == item.subjectId }?.actionsList(item.action),
                showDelete = true
              ),
              id = itemId,
              orderNo = item.order
            )
          }
        }
      )
      .disposeBySelf()
  }

  override fun onProfileSelected(profileItem: ProfileItem) {
    val subjectType = currentState().lastSubjectType(profileItem.id) ?: currentState().viewState.subjectType
    getSubjectsSource(profileItem.id, subjectType)
      .attach()
      .subscribeBy(
        onSuccess = { subjects ->
          updateState { state ->
            val lastSubjectId = state.lastSubjectId(profileItem.id, subjectType)
            val lastCaption = state.lastCaption(profileItem.id, subjectType, lastSubjectId)
            val lastActionId = state.lastActionId(profileItem.id, subjectType, lastSubjectId)
            state.copy(
              viewState = state.viewState.copy(
                profiles = state.viewState.profiles?.copy(selected = profileItem),
                subjectType = subjectType,
                subjects = subjects.asSingleSelectionList(subjectType, lastSubjectId),
                caption = lastCaption,
                actions = subjects.firstOrNull { it.isLocation.not() }?.actionsList(lastActionId)
              ),
            )
          }
        }
      )
      .disposeBySelf()
  }

  override fun onSubjectTypeSelected(subjectType: SubjectType) {
    val (profile) = guardLet(currentState().viewState.profiles?.selected) { return }
    getSubjectsSource(profile.id, subjectType)
      .attach()
      .subscribeBy(
        onSuccess = { subjects ->
          updateState { state ->
            val lastSubjectId = state.lastSubjectId(profile.id, subjectType)
            val lastCaption = state.lastCaption(profile.id, subjectType, lastSubjectId)
            val lastActionId = state.lastActionId(profile.id, subjectType, lastSubjectId)
            state.copy(
              viewState = state.viewState.copy(
                subjectType = subjectType,
                subjects = subjects.asSingleSelectionList(subjectType, lastSubjectId),
                caption = lastCaption,
                actions = subjects.firstOrNull { it.isLocation.not() }?.actionsList(lastActionId)
              ),
            )
          }
        }
      )
      .disposeBySelf()
  }

  override fun onSubjectSelected(subjectItem: SubjectItem) {
    updateState { state ->
      val profileId = state.viewState.profiles?.selected?.id
      val lastCaption = state.lastCaption(profileId, state.viewState.subjectType, subjectItem.id)
      val lastActionId = state.lastActionId(profileId, state.viewState.subjectType, subjectItem.id)
      state.copy(
        viewState = state.viewState.copy(
          subjects = state.viewState.subjects?.copy(selected = subjectItem),
          caption = lastCaption,
          actions = subjectItem.actionsList(lastActionId)
        )
      )
    }
  }

  override fun onCaptionChange(caption: String) {
    updateState { state ->
      state.copy(
        viewState = state.viewState.copy(
          caption = caption,
          saveEnabled = caption.isNotEmpty()
        ),
        selections = state.updateSelections(caption)
      )
    }
  }

  override fun onActionChange(actionId: ActionId) {
    updateState { state ->
      state.copy(
        viewState = state.viewState.copy(
          actions = state.viewState.actions?.copy(selected = actionId)
        ),
        selections = state.updateSelections(actionId)
      )
    }
  }

  override fun onSave() {
    val state = currentState()
    val (profileId) = guardLet(state.viewState.profiles?.selected?.id) { return }
    val (subjectId) = guardLet(state.viewState.subjects?.selected?.id) { return }
    val (caption) = guardLet(state.viewState.caption) { return }
    val (action) = guardLet(state.viewState.actions?.selected) { return }

    val positionFetcher = if (state.orderNo == null) {
      androidAutoItemRepository.lastOrderNo().map { it + 1 }
    } else {
      Single.just(state.orderNo)
    }

    positionFetcher
      .map { position ->
        AndroidAutoItemEntity(
          id = state.id ?: 0,
          subjectId = subjectId,
          subjectType = state.viewState.subjectType,
          caption = caption,
          action = action,
          profileId = profileId,
          order = position
        )
      }.flatMapCompletable { androidAutoItemRepository.insert(it) }
      .attach()
      .subscribeBy(
        onComplete = {
          sendEvent(AddAndroidAutoItemViewEvent.Close)
        }
      )
      .disposeBySelf()
  }

  override fun onDelete() {
    updateState { it.copy(showDeletePopup = true) }
  }

  fun onDeleteConfirmed() {
    val (id) = guardLet(currentState().id) { return }

    androidAutoItemRepository.delete(id)
      .attach()
      .subscribeBy(
        onComplete = {
          sendEvent(AddAndroidAutoItemViewEvent.Close)
        }
      )
      .disposeBySelf()
  }

  fun onDeleteCanceled() {
    updateState { it.copy(showDeletePopup = false) }
  }

  private fun getSubjectsSource(profileId: Long, subjectType: SubjectType): Single<List<SubjectItem>> =
    when (subjectType) {
      SubjectType.CHANNEL ->
        channelRepository.findProfileChannels(profileId)
          .map { channels -> channelsSubjectItems(channels.filter { it.function.actions.isNotEmpty() }, getChannelValueStringUseCase) }

      SubjectType.GROUP ->
        channelGroupRepository.findProfileGroups(profileId)
          .map { groups -> groupsSubjectItems(groups.filter { it.function.actions.isNotEmpty() }) }

      SubjectType.SCENE ->
        sceneRepository.findProfileScenes(profileId)
          .map { scenes -> scenesSubjectItems(scenes) }
    }
}

sealed class AddAndroidAutoItemViewEvent : ViewEvent {
  data object Close : AddAndroidAutoItemViewEvent()
}

data class AddAndroidAutoItemViewModelState(
  val id: Long? = null,
  val orderNo: Int? = null,
  val viewState: AddAndroidAutoItemViewState = AddAndroidAutoItemViewState(),
  val selections: Set<Selection> = emptySet(),
  val showDeletePopup: Boolean = false
) : ViewState() {

  fun updateSelections(actionId: ActionId): Set<Selection> =
    mutableSetOf<Selection>().apply {
      addAll(selections)
      val profileId = viewState.profiles?.selected?.id
      val subjectId = viewState.subjects?.selected?.id
      val caption = viewState.caption

      if (profileId != null && subjectId != null && caption != null) {
        addOrReplace(Selection(profileId, viewState.subjectType, subjectId, caption, actionId))
      }
    }

  fun updateSelections(caption: String): Set<Selection> =
    mutableSetOf<Selection>().apply {
      addAll(selections)
      val profileId = viewState.profiles?.selected?.id
      val subjectId = viewState.subjects?.selected?.id
      val actionId = viewState.actions?.selected

      if (profileId != null && subjectId != null && actionId != null) {
        addOrReplace(Selection(profileId, viewState.subjectType, subjectId, caption, actionId))
      }
    }

  fun lastSubjectType(profileId: Long?): SubjectType? =
    selections.lastOrNull { it.profileId == profileId }?.subjectType

  fun lastSubjectId(profileId: Long?, subjectType: SubjectType): Int? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType }?.subjectId

  fun lastCaption(profileId: Long?, subjectType: SubjectType, subjectId: Int?): String? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType && it.subjectId == subjectId }?.caption

  fun lastActionId(profileId: Long?, subjectType: SubjectType, subjectId: Int?): ActionId? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType && it.subjectId == subjectId }?.action

  private fun MutableSet<Selection>.addOrReplace(selection: Selection) {
    if (contains(selection)) {
      remove(selection)
    }

    add(selection)
  }
}

data class Selection(
  val profileId: Long,
  val subjectType: SubjectType,
  val subjectId: Int,
  val caption: String,
  val action: ActionId
) {
  override fun hashCode(): Int {
    return Objects.hash(profileId, subjectType, subjectId)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Selection

    if (profileId != other.profileId) return false
    if (subjectId != other.subjectId) return false
    if (subjectType != other.subjectType) return false

    return true
  }
}
