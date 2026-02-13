package org.supla.android.features.nfc.add
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

import android.content.Intent
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.nfc.TagProcessingResult
import org.supla.android.core.infrastructure.nfc.nfcTag
import org.supla.android.core.infrastructure.nfc.prepareForSupla
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.local.entity.NfcTagEntity
import org.supla.android.tools.SuplaSchedulers
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class AddNfcTagViewModel @Inject constructor(
  private val nfcTagRepository: NfcTagRepository,
  private val preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseViewModel<AddNfcTagViewModelState, AddNfcTagViewEvent>(AddNfcTagViewModelState(), schedulers), AddNfcTagScope {

  private var currentJob: Job? = null
  private lateinit var fragment: WeakReference<AddNfcTagFragment>

  fun attachFragment(fragment: AddNfcTagFragment) {
    this.fragment = WeakReference(fragment)
  }

  override fun onViewCreated() {
    updateState { it.copy(viewState = it.viewState.copy(lockTag = preferences.lockNfcTag)) }
  }

  override fun onTagNameChange(name: String) {
    updateState {
      it.copy(viewState = it.viewState.copy(tagName = name.take(100)))
    }
  }

  override fun onStepFinished(step: AddNfcStep) {
    when (step) {
      AddNfcStep.TagReading -> {} // Nothing to do
      is AddNfcStep.TagSummary ->
        when (step.result) {
          is AddNfcSummary.Success,
          is AddNfcSummary.Duplicate -> Unit // Nothing to do
          AddNfcSummary.Failure,
          AddNfcSummary.NotEnoughSpace,
          AddNfcSummary.NotUsable -> sendEvent(AddNfcTagViewEvent.Close)
        }
    }
  }

  override fun onConfigureTagAction(tagId: Long) {
    sendEvent(AddNfcTagViewEvent.ConfigureTagAction(tagId))
  }

  override fun onWriteLockChanged(active: Boolean) {
    preferences.lockNfcTag = active
    updateState { it.copy(viewState = it.viewState.copy(lockTag = active)) }
  }

  override fun onPrepareAnother() {
    val step = currentState().viewState.step

    if (step is AddNfcStep.TagSummary) {
      when (step.result) {
        is AddNfcSummary.Duplicate,
        AddNfcSummary.Failure,
        AddNfcSummary.NotEnoughSpace,
        AddNfcSummary.NotUsable -> {
          updateState { it.copy(viewState = it.viewState.copy(tagName = "", error = false)) }
          changeStep(AddNfcStep.TagReading)
        }

        is AddNfcSummary.Success ->
          viewModelScope.launch {
            getTagName()?.let {
              saveWithDuplicateCheck(step.result.tagUuid, it, step.result.readOnly)
              changeStep(AddNfcStep.TagReading)
            }
          }
      }
    }
  }

  override fun onStart() {
    if (currentState().viewState.step == AddNfcStep.TagReading) {
      fragment.get()?.enableNfcDispatch()
    }
  }

  override fun onStop() {
    if (currentState().viewState.step == AddNfcStep.TagReading) {
      fragment.get()?.disableNfcDispatch()
    }
  }

  fun handleIntent(intent: Intent) {
    val state = currentState()
    if (state.viewState.step != AddNfcStep.TagReading) {
      Timber.i("Got intent but is not in reading state")
      return
    }

    val tag = intent.nfcTag
    if (tag == null) {
      Timber.i("Got intent but without a tag")
      return
    }

    if (currentJob != null) {
      Timber.i("Got intent but already processing")
      return
    }

    currentJob = viewModelScope.launch {
      when (val result = tag.prepareForSupla(state.viewState.lockTag).toSummary()) {
        is AddNfcSummary.Success -> sendEvent(AddNfcTagViewEvent.ConfigureNewTag(result.tagUuid, result.readOnly))
        is AddNfcSummary.Duplicate -> sendEvent(AddNfcTagViewEvent.ConfigureTagAction(result.tagId))
        else -> changeStep(AddNfcStep.TagSummary(result))
      }
    }
    currentJob?.invokeOnCompletion {
      if (it != null) {
        // If job failed we need to disable NFC manually.
        fragment.get()?.disableNfcDispatch()
      }
      currentJob = null
    }
  }

  fun handleBack() {
    currentJob?.cancel()
    sendEvent(AddNfcTagViewEvent.Close)
  }

  private fun getTagName(): String? {
    val tagName = currentState().viewState.tagName
    if (tagName.trim().isEmpty()) {
      updateState { it.copy(viewState = it.viewState.copy(error = true)) }
      return null
    } else {
      updateState { it.copy(viewState = it.viewState.copy(error = false)) }
      return tagName
    }
  }

  private suspend fun saveWithDuplicateCheck(uuid: String, tagName: String, readOnly: Boolean): Long {
    val existing = nfcTagRepository.findByUuid(uuid)
    if (existing != null) {
      val updated = existing.copy(name = tagName, readOnly = readOnly)
      nfcTagRepository.save(updated)
      return updated.id
    } else {
      val new = NfcTagEntity(uuid = uuid, name = tagName, readOnly = readOnly)
      return nfcTagRepository.save(new)
    }
  }

  suspend fun TagProcessingResult.toSummary(): AddNfcSummary =
    when (this) {
      TagProcessingResult.Failure -> AddNfcSummary.Failure
      TagProcessingResult.NotUsable -> AddNfcSummary.NotUsable
      TagProcessingResult.NotEnoughSpace -> AddNfcSummary.NotEnoughSpace
      is TagProcessingResult.Success -> {
        val tags = nfcTagRepository.findAll()
        val existing = tags.find { it.uuid == uuid }

        if (existing != null) {
          val updated = existing.copy(readOnly = readOnly)
          // tag is already saved so update it in case the writable flag changed
          nfcTagRepository.save(updated)
          AddNfcSummary.Duplicate(existing.id, existing.uuid, existing.name)
        } else {
          AddNfcSummary.Success(uuid, readOnly)
        }
      }
    }

  private fun changeStep(newStep: AddNfcStep) {
    val state = currentState()
    val oldStep = state.viewState.step

    if (oldStep == newStep) {
      Timber.w("Trying to change step to the same one: $newStep")
      return
    }

    if (newStep == AddNfcStep.TagReading) {
      fragment.get()?.enableNfcDispatch()
    } else if (oldStep == AddNfcStep.TagReading) {
      fragment.get()?.disableNfcDispatch()
    }

    updateState { it.copy(viewState = state.viewState.copy(step = newStep)) }
  }
}

sealed interface AddNfcTagViewEvent : ViewEvent {
  data object Close : AddNfcTagViewEvent
  data class ConfigureTagAction(val tagId: Long) : AddNfcTagViewEvent
  data class ConfigureNewTag(val uuid: String, val readOnly: Boolean) : AddNfcTagViewEvent
}

data class AddNfcTagViewModelState(
  val viewState: AddNfcTagViewState = AddNfcTagViewState()
) : ViewState()
