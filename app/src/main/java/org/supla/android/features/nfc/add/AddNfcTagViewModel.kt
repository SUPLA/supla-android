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

import android.nfc.Tag
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.nfc.PrepareNfcTagUseCase
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class AddNfcTagViewModel @Inject constructor(
  private val prepareNfcTagUseCase: PrepareNfcTagUseCase,
  private val nfcTagRepository: NfcTagRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<AddNfcTagViewState, AddNfcTagViewEvent>(AddNfcTagViewState(), schedulers), AddNfcTagScope {

  private var currentJob: Job? = null
  private lateinit var fragment: WeakReference<AddNfcTagFragment>

  fun attachFragment(fragment: AddNfcTagFragment) {
    this.fragment = WeakReference(fragment)
  }

  override fun onViewCreated() {
  }

  override fun onTryAgain() {
    updateState { it.copy(dialog = null) }
  }

  override fun onOpenTag(tagId: Long) {
    sendEvent(AddNfcTagViewEvent.OpenExisting(tagId))
  }

  override fun onClose() {
    sendEvent(AddNfcTagViewEvent.Close)
  }

  fun handleTag(tag: Tag) {
    val state = currentState()
    if (state.dialog != null) {
      Timber.i("Got intent but dialog is presented")
      return
    }

    if (currentJob != null) {
      Timber.i("Got intent but already processing")
      return
    }

    currentJob = viewModelScope.launch {
      when (val result = prepareNfcTagUseCase(tag)) {
        is PrepareNfcTagUseCase.Failure -> updateState { it.copy(dialog = AddNfcTagDialog.Failure(result.error)) }
        is PrepareNfcTagUseCase.Success -> {
          val tag = nfcTagRepository.findByUuid(result.uuid)
          if (tag == null) {
            sendEvent(AddNfcTagViewEvent.ConfigureNewTag(result.uuid, result.readOnly))
          } else {
            updateState { it.copy(dialog = AddNfcTagDialog.Duplicate(tag.id, tag.name)) }
            nfcTagRepository.save(tag.copy(readOnly = result.readOnly))
          }
        }
      }
    }
    currentJob?.invokeOnCompletion {
      currentJob = null
    }
  }

  fun handleBack() {
    currentJob?.cancel()
    sendEvent(AddNfcTagViewEvent.Close)
  }
}

sealed interface AddNfcTagViewEvent : ViewEvent {
  data object Close : AddNfcTagViewEvent
  data class OpenExisting(val tagId: Long) : AddNfcTagViewEvent
  data class ConfigureNewTag(val uuid: String, val readOnly: Boolean) : AddNfcTagViewEvent
}
