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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.NfcScanner
import org.supla.android.core.infrastructure.nfc.TagProcessingResult
import org.supla.android.core.infrastructure.nfc.prepareForSupla
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.local.entity.NfcTagEntity
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class AddNfcTagViewModel @Inject constructor(
  private val nfcTagRepository: NfcTagRepository,
  private val preferences: Preferences,
  private val nfcScanner: NfcScanner,
  schedulers: SuplaSchedulers
) : BaseViewModel<AddNfcTagViewModelState, AddNfcTagViewEvent>(AddNfcTagViewModelState(), schedulers), AddNfcTagScope {

  private var currentJob: Job? = null

  override fun onViewCreated() {
    startTagScanJob()
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
      is AddNfcStep.TagConfiguration ->
        when (step.result) {
          AddNfcSummary.Failure,
          AddNfcSummary.NotEnoughSpace,
          AddNfcSummary.NotUsable,
          AddNfcSummary.Timeout,
          is AddNfcSummary.Duplicate -> sendEvent(AddNfcTagViewEvent.Close)

          is AddNfcSummary.Success -> saveAndClose(step.result.tagUuid)
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
    currentJob?.cancel()
    updateState {
      it.copy(
        viewState = it.viewState.copy(
          tagName = "",
          error = false,
          step = AddNfcStep.TagReading
        )
      )
    }
    startTagScanJob()
  }

  override fun onSaveAndPrepareAnother(uuid: String) {
    viewModelScope.launch {
      getTagName()?.let {
        saveWithDuplicateCheck(uuid, it)
        onPrepareAnother()
      }
    }
  }

  override fun onSaveAndConfigure(uuid: String) {
    viewModelScope.launch {
      getTagName()?.let {
        val id = saveWithDuplicateCheck(uuid, it)
        sendEvent(AddNfcTagViewEvent.ConfigureTagAction(id))
      }
    }
  }

  override fun onConfigure(id: Long) {
    sendEvent(AddNfcTagViewEvent.ConfigureTagAction(id))
  }

  fun handleIntent(intent: Intent) {
    nfcScanner.handleIntent(intent)
  }

  fun handleBack() {
    currentJob?.cancel()
    sendEvent(AddNfcTagViewEvent.Close)
  }

  private fun saveAndClose(uuid: String) {
    viewModelScope.launch {
      getTagName()?.let {
        saveWithDuplicateCheck(uuid, it)
        sendEvent(AddNfcTagViewEvent.Close)
      }
    }
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

  private suspend fun saveWithDuplicateCheck(uuid: String, tagName: String): Long {
    val existing = nfcTagRepository.findByUuid(uuid)
    if (existing != null) {
      val updated = existing.copy(name = tagName)
      nfcTagRepository.save(updated)
      return updated.id
    } else {
      val new = NfcTagEntity(uuid = uuid, name = tagName)
      return nfcTagRepository.save(new)
    }
  }

  private fun startTagScanJob() {
    val job = viewModelScope.launch {
      sendEvent(AddNfcTagViewEvent.EnableNfc)

      val summary: AddNfcSummary = withContext(Dispatchers.IO) {
        when (val result = nfcScanner.scan()) {
          is NfcScanner.Result.Success -> result.tag.prepareForSupla(currentState().viewState.lockTag).toSummary()
          NfcScanner.Result.Failure -> AddNfcSummary.Failure
          NfcScanner.Result.Timeout -> AddNfcSummary.Timeout
        }
      }

      updateState { it.copy(viewState = it.viewState.copy(step = AddNfcStep.TagConfiguration(summary))) }
    }
    job.invokeOnCompletion {
      sendEvent(AddNfcTagViewEvent.DisableNfc)
      currentJob = null
    }

    currentJob = job
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
          AddNfcSummary.Duplicate(existing.id, existing.uuid, existing.name)
        } else {
          AddNfcSummary.Success(uuid)
        }
      }
    }
}

sealed interface AddNfcTagViewEvent : ViewEvent {
  data object Close : AddNfcTagViewEvent
  data class ConfigureTagAction(val tagId: Long) : AddNfcTagViewEvent
  data object EnableNfc : AddNfcTagViewEvent
  data object DisableNfc : AddNfcTagViewEvent
}

data class AddNfcTagViewModelState(
  val viewState: AddNfcTagViewState = AddNfcTagViewState()
) : ViewState()
