package org.supla.android.features.nfc.shared.edit
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
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.data.model.spinner.SubjectItemConversionScope
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.NfcCallRepository
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.local.entity.NfcCallResult
import org.supla.android.data.source.local.entity.NfcTagEntity
import org.supla.android.extensions.subscribeBy
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ReadAllChannelsWithChildrenUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.profile.ReadAllProfilesUseCase
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.usecase.GetCaptionUseCase
import timber.log.Timber
import java.util.Objects

open class BaseEditNfcTagViewModel(
  private val readAllChannelsWithChildrenUseCase: ReadAllChannelsWithChildrenUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val readAllProfilesUseCase: ReadAllProfilesUseCase,
  private val channelGroupRepository: ChannelGroupRepository,
  private val sceneRepository: SceneRepository,
  private val nfcCallRepository: NfcCallRepository,
  private val nfcTagRepository: NfcTagRepository,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getSceneIconUseCase: GetSceneIconUseCase,
  override val getCaptionUseCase: GetCaptionUseCase,
  schedulers: SuplaSchedulers
) :
  BaseViewModel<EditNfcTagViewState, EditNfcTagViewEvent>(
    defaultState = EditNfcTagViewState(),
    schedulers = schedulers,
    manageScreenTitle = true
  ),
  SubjectItemConversionScope {

  private var mode: Mode = Mode.Unknown
  private var selections: Set<Selection> = emptySet()

  fun onViewCreated(id: Long) {
    viewModelScope.launch {
      val tag = nfcTagRepository.findById(id)
      if (tag == null) {
        Timber.w("Tag with id `$id` not found")
        sendEvent(EditNfcTagViewEvent.Close)
        return@launch
      }

      load(
        uuid = tag.uuid,
        readOnly = false, // Not relevant here.
        profileId = tag.profileId,
        subjectType = tag.subjectType,
        subjectId = tag.subjectId,
        actionId = tag.actionId,
        name = tag.name,
        id = tag.id
      )
    }
  }

  fun onViewCreated(uuid: String, readOnly: Boolean) {
    viewModelScope.launch { load(uuid = uuid, readOnly = readOnly) }
  }

  fun onSave() {
    val state = currentState()
    val tagName = state.tagName

    if (tagName.trim().isEmpty()) {
      updateState { it.copy(isError = true) }
      return
    }

    when (val mode = mode) {
      is Mode.Edit -> edit(state, mode.id)
      is Mode.Insert -> insert(state, mode.uuid, mode.readOnly)
      Mode.Unknown -> sendEvent(EditNfcTagViewEvent.Close)
    }
  }

  fun onProfileSelected(profileItem: ProfileItem) {
    val subjectType = lastSubjectType(profileItem.id) ?: currentState().subjectType
    getSubjectsSource(profileItem.id, subjectType)
      .attach()
      .subscribeBy(
        onSuccess = { subjects ->
          updateState { state ->
            val lastSubjectId = lastSubjectId(profileItem.id, subjectType)
            val lastActionId = lastActionId(profileItem.id, subjectType, lastSubjectId)
            val subjectsList = subjects.asSingleSelectionList(subjectType, lastSubjectId)
            state.copy(
              profiles = state.profiles?.copy(selected = profileItem),
              subjectType = subjectType,
              subjects = subjectsList,
              actions = subjectsList?.selected?.actionsList(lastActionId)
            )
          }
        }
      )
      .disposeBySelf()
  }

  fun onSubjectTypeSelected(subjectType: SubjectType) {
    val (profile) = guardLet(currentState().profiles?.selected) { return }
    getSubjectsSource(profile.id, subjectType)
      .attach()
      .subscribeBy(
        onSuccess = { subjects ->
          updateState { state ->
            val lastSubjectId = lastSubjectId(profile.id, subjectType)
            val lastActionId = lastActionId(profile.id, subjectType, lastSubjectId)
            val subjectsList = subjects.asSingleSelectionList(subjectType, lastSubjectId)
            state.copy(
              subjectType = subjectType,
              subjects = subjectsList,
              actions = subjectsList?.selected?.actionsList(lastActionId)
            )
          }
        }
      )
      .disposeBySelf()
  }

  fun onSubjectSelected(subjectItem: SubjectItem) {
    updateState { state ->
      selections = state.updateSelections(subjectItem.id)

      val profileId = state.profiles?.selected?.id
      val lastActionId = lastActionId(profileId, state.subjectType, subjectItem.id)
      state.copy(
        subjects = state.subjects?.copy(selected = subjectItem),
        actions = subjectItem.actionsList(lastActionId)
      )
    }
  }

  fun onCaptionChange(caption: String) {
    updateState { it.copy(tagName = caption) }
  }

  fun onActionChange(actionId: ActionId) {
    updateState { state ->
      selections = state.updateSelections(actionId)
      state.copy(
        actions = state.actions?.copy(selected = actionId)
      )
    }
  }

  private suspend fun load(
    uuid: String,
    readOnly: Boolean,
    profileId: Long? = null,
    subjectType: SubjectType? = null,
    subjectId: Int? = null,
    actionId: ActionId? = null,
    name: String = "",
    id: Long? = null
  ) {
    val profiles = schedulers.io { runCatching { readAllProfilesUseCase().blockingFirst() }.getOrNull() }
    if (profiles == null) {
      Timber.w("Something wrong - no profiles found")
      sendEvent(EditNfcTagViewEvent.Close)
      return
    }
    val profileId = profileId ?: profiles.firstOrNull { it.active }?.id
    val subjectType = subjectType ?: SubjectType.CHANNEL

    val subjects = schedulers.io {
      profileId?.let { runCatching { getSubjectsSource(it, subjectType).blockingGet() }.getOrNull() } ?: emptyList()
    }
    val subjectsList = subjects.asSingleSelectionList(subjectType, subjectId)
    mode = id?.let { Mode.Edit(it) } ?: Mode.Insert(uuid, readOnly)

    updateState { state ->
      state.copy(
        tagName = name,
        tagUuid = uuid,
        profiles = profiles.asSingleSelectionList(profileId),
        subjects = subjectsList,
        subjectType = subjectType,
        actions = subjectsList?.selected?.actionsList(actionId),
        newTag = mode is Mode.Insert
      )
    }

    when (mode) {
      is Mode.Edit -> setScreenTitle(name)
      is Mode.Insert -> setScreenTitle(R.string.edit_nfc_new_tag_header)
      else -> {} // No title change needed
    }
  }

  private fun edit(state: EditNfcTagViewState, id: Long) {
    viewModelScope.launch {
      val tag = nfcTagRepository.findById(id) ?: return@launch
      nfcTagRepository.save(
        entity = tag.copy(
          name = state.tagName,
          profileId = state.profiles?.selected?.id,
          subjectType = state.subjectType,
          subjectId = state.subjects?.selected?.id,
          actionId = state.actions?.selected
        )
      )
      sendEvent(EditNfcTagViewEvent.Close)
    }
  }

  private fun insert(state: EditNfcTagViewState, uuid: String, readOnly: Boolean) {
    viewModelScope.launch {
      val id = nfcTagRepository.save(
        entity = NfcTagEntity(
          uuid = uuid,
          name = state.tagName,
          profileId = state.profiles?.selected?.id,
          subjectType = state.subjectType,
          subjectId = state.subjects?.selected?.id,
          actionId = state.actions?.selected,
          readOnly = readOnly
        )
      )
      nfcCallRepository.insert(id, NfcCallResult.TAG_ADDED)

      sendEvent(EditNfcTagViewEvent.Close)
    }
  }

  private fun getSubjectsSource(profileId: Long, subjectType: SubjectType): Single<List<SubjectItem>> =
    when (subjectType) {
      SubjectType.CHANNEL ->
        readAllChannelsWithChildrenUseCase(profileId)
          .firstOrError()
          .map { channels -> channelsSubjectItems(channels.filter { it.actions.isNotEmpty() }, getChannelValueStringUseCase) }
      SubjectType.GROUP ->
        channelGroupRepository.findProfileGroups(profileId)
          .map { groups -> groupsSubjectItems(groups.filter { it.function.actions.isNotEmpty() }) }
      SubjectType.SCENE ->
        sceneRepository.findProfileScenes(profileId)
          .map { scenes -> scenesSubjectItems(scenes) }
    }

  private fun EditNfcTagViewState.updateSelections(subjectId: Int): Set<Selection> =
    mutableSetOf<Selection>().apply {
      addAll(selections)
      val profileId = profiles?.selected?.id

      if (profileId != null) {
        addOrReplace(Selection(profileId, subjectType, subjectId, null))
      }
    }

  private fun EditNfcTagViewState.updateSelections(actionId: ActionId): Set<Selection> =
    mutableSetOf<Selection>().apply {
      addAll(selections)
      val profileId = profiles?.selected?.id
      val subjectId = subjects?.selected?.id

      if (profileId != null && subjectId != null) {
        addOrReplace(Selection(profileId, subjectType, subjectId, actionId))
      }
    }

  fun lastSubjectType(profileId: Long?): SubjectType? =
    selections.lastOrNull { it.profileId == profileId }?.subjectType

  fun lastSubjectId(profileId: Long?, subjectType: SubjectType): Int? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType }?.subjectId

  fun lastActionId(profileId: Long?, subjectType: SubjectType, subjectId: Int?): ActionId? =
    selections.lastOrNull { it.profileId == profileId && it.subjectType == subjectType && it.subjectId == subjectId }?.action
}

sealed interface EditNfcTagViewEvent : ViewEvent {
  data object Close : EditNfcTagViewEvent
}

private fun MutableSet<Selection>.addOrReplace(selection: Selection) {
  if (contains(selection)) {
    remove(selection)
  }
  add(selection)
}

data class Selection(
  val profileId: Long,
  val subjectType: SubjectType,
  val subjectId: Int,
  val action: ActionId?
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

sealed interface Mode {
  data class Edit(val id: Long) : Mode
  data class Insert(val uuid: String, val readOnly: Boolean) : Mode
  data object Unknown : Mode
}
