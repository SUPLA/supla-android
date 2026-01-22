package org.supla.android.features.nfc.edit
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
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.launch
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.data.model.spinner.SubjectItemConversionScope
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.usecase.GetCaptionUseCase
import timber.log.Timber
import java.util.Objects
import javax.inject.Inject

@HiltViewModel
class EditNfcTagViewModel @Inject constructor(
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val readAllProfilesUseCase: ReadAllProfilesUseCase,
  private val channelGroupRepository: ChannelGroupRepository,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getSceneIconUseCase: GetSceneIconUseCase,
  private val channelRepository: RoomChannelRepository,
  override val getCaptionUseCase: GetCaptionUseCase,
  private val sceneRepository: RoomSceneRepository,
  private val nfcTagRepository: NfcTagRepository,
  private val schedulers: SuplaSchedulers
) : BaseViewModel<EditNfcTagViewModelState, EditNfcTagViewEvent>(EditNfcTagViewModelState(), schedulers),
  EditNfcTagViewScope,
  SubjectItemConversionScope {

  fun onViewCreated(id: Long) {
    viewModelScope.launch {
      val tag = nfcTagRepository.findById(id)
      if (tag == null) {
        Timber.w("Tag with id `$id` not found")
        sendEvent(EditNfcTagViewEvent.Close)
        return@launch
      }

      val profiles = schedulers.io { runCatching { readAllProfilesUseCase().blockingFirst() }.getOrNull() }
      if (profiles == null) {
        Timber.w("Something wrong - no profiles not found")
        sendEvent(EditNfcTagViewEvent.Close)
        return@launch
      }
      val profileId = tag.profileId ?: profiles.firstOrNull { it.active == true }?.id
      val subjectType = tag.subjectType ?: SubjectType.CHANNEL

      val subjects = schedulers.io {
        profileId?.let { runCatching { getSubjectsSource(it, subjectType).blockingGet() }.getOrNull() } ?: emptyList()
      }
      val subjectForAction = subjects.firstOrNull { it.id == tag.subjectId } ?: subjects.firstOrNull()

      updateState { state ->
        state.copy(
          viewState = state.viewState.copy(
            tagName = tag.name,
            profiles = profiles.asSingleSelectionList(tag.profileId),
            subjects = subjects.asSingleSelectionList(subjectType, tag.subjectId),
            subjectType = subjectType,
            actions = subjectForAction?.actionsList(tag.actionId),
          ),
          itemId = id,
        )
      }
    }
  }

  override fun onSave() {
    val state = currentState()
    viewModelScope.launch {
      val tag = nfcTagRepository.findById(state.itemId) ?: return@launch
      nfcTagRepository.save(
        entity = tag.copy(
          name = state.viewState.tagName,
          profileId = state.viewState.profiles?.selected?.id,
          subjectType = state.viewState.subjectType,
          subjectId = state.viewState.subjects?.selected?.id,
          actionId = state.viewState.actions?.selected
        )
      )
      sendEvent(EditNfcTagViewEvent.Close)
    }
  }

  override fun onDelete() {
    viewModelScope.launch {
      nfcTagRepository.delete(currentState().itemId)
      sendEvent(EditNfcTagViewEvent.Close)
    }
  }

  override fun onProfileSelected(profileItem: ProfileItem) {
    val subjectType = currentState().lastSubjectType(profileItem.id) ?: currentState().viewState.subjectType
    getSubjectsSource(profileItem.id, subjectType)
      .attach()
      .subscribeBy(
        onSuccess = { subjects ->
          updateState { state ->
            val lastSubjectId = state.lastSubjectId(profileItem.id, subjectType)
            val lastActionId = state.lastActionId(profileItem.id, subjectType, lastSubjectId)
            state.copy(
              viewState = state.viewState.copy(
                profiles = state.viewState.profiles?.copy(selected = profileItem),
                subjectType = subjectType,
                subjects = subjects.asSingleSelectionList(subjectType, lastSubjectId),
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
            val lastActionId = state.lastActionId(profile.id, subjectType, lastSubjectId)
            state.copy(
              viewState = state.viewState.copy(
                subjectType = subjectType,
                subjects = subjects.asSingleSelectionList(subjectType, lastSubjectId),
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
      val lastActionId = state.lastActionId(profileId, state.viewState.subjectType, subjectItem.id)
      state.copy(
        viewState = state.viewState.copy(
          subjects = state.viewState.subjects?.copy(selected = subjectItem),
          actions = subjectItem.actionsList(lastActionId)
        )
      )
    }
  }

  override fun onCaptionChange(caption: String) {
    updateState { it.copy(viewState = it.viewState.copy(tagName = caption)) }
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

sealed interface EditNfcTagViewEvent : ViewEvent {
  data object Close : EditNfcTagViewEvent
}

data class EditNfcTagViewModelState(
  val itemId: Long = 0,
  val viewState: EditNfcTagViewState = EditNfcTagViewState(),
  val selections: Set<Selection> = emptySet()
) : ViewState() {

  fun updateSelections(actionId: ActionId): Set<Selection> =
    mutableSetOf<Selection>().apply {
      addAll(selections)
      val profileId = viewState.profiles?.selected?.id
      val subjectId = viewState.subjects?.selected?.id

      if (profileId != null && subjectId != null) {
        addOrReplace(Selection(profileId, viewState.subjectType, subjectId, actionId))
      }
    }

  fun lastSubjectType(profileId: Long?): SubjectType? =
    selections.lastOrNull { it.profileId == profileId }?.subjectType

  fun lastSubjectId(profileId: Long?, subjectType: SubjectType): Int? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType }?.subjectId

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
