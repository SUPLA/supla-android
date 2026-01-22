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
  private val nfcScanner: NfcScanner,
  schedulers: SuplaSchedulers
) : BaseViewModel<AddNfcTagViewModelState, AddNfcTagViewEvent>(AddNfcTagViewModelState(), schedulers), AddNfcTagScope {

  private var currentJob: Job? = null

  override fun onTagNameChange(name: String) {
    updateState {
      it.copy(viewState = it.viewState.copy(tagName = name.take(100)))
    }
  }

  override fun onStepFinished(step: AddNfcStep) {
    when (step) {
      AddNfcStep.Preconditions -> {
        updateState {
          if (it.viewState.tagName.trim().isEmpty()) {
            it.copy(viewState = it.viewState.copy(error = true))
          } else {
            it.copy(viewState = it.viewState.copy(step = AddNfcStep.TagConfiguration, loading = true))
          }
        }
      }

      AddNfcStep.TagConfiguration -> {}
      is AddNfcStep.Summary -> sendEvent(AddNfcTagViewEvent.Close)
    }

    handleStep(currentState().viewState.step)
  }

  override fun onConfigureTagAction(tagId: Long) {
    sendEvent(AddNfcTagViewEvent.ConfigureTagAction(tagId))
  }

  fun handleIntent(intent: Intent) {
    nfcScanner.handleIntent(intent)
  }

  private fun handleStep(step: AddNfcStep) {
    when (step) {
      AddNfcStep.Preconditions -> {}
      AddNfcStep.TagConfiguration -> handleTagConfiguration()
      is AddNfcStep.Summary -> {}
    }
  }

  private fun handleTagConfiguration() {
    val job = viewModelScope.launch {
      sendEvent(AddNfcTagViewEvent.EnableNfc)

      val summary: AddNfcSummary = withContext(Dispatchers.IO) {
        when (val result = nfcScanner.scan()) {
          is NfcScanner.Result.Success -> {
            val name = currentState().viewState.tagName.trim()
            result.tag.prepareForSupla().toSummary(name)
          }

          NfcScanner.Result.Failure -> AddNfcSummary.Failure
          NfcScanner.Result.Timeout -> AddNfcSummary.Timeout
        }
      }

      updateState { it.copy(viewState = it.viewState.copy(step = AddNfcStep.Summary(summary))) }
    }
    job.invokeOnCompletion {
      sendEvent(AddNfcTagViewEvent.DisableNfc)
      currentJob = null
    }

    currentJob = job
  }

  suspend fun TagProcessingResult.toSummary(name: String): AddNfcSummary =
    when (this) {
      TagProcessingResult.Failure -> AddNfcSummary.Failure
      TagProcessingResult.NotUsable -> AddNfcSummary.NotUsable
      is TagProcessingResult.Success -> {
        val tags = nfcTagRepository.findAll()
        val existing = tags.find { it.uuid == uuid }

        if (existing != null) {
          AddNfcSummary.Duplicate(existing.id, existing.uuid, existing.name)
        } else {
          val id = nfcTagRepository.save(NfcTagEntity(uuid = uuid, name = name))
          AddNfcSummary.Success(id, uuid)
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
