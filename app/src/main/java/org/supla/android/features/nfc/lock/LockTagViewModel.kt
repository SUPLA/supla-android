package org.supla.android.features.nfc.lock
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
import org.supla.android.usecases.nfc.LockTagUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LockTagViewModel @Inject constructor(
  private val nfcTagRepository: NfcTagRepository,
  private val lockTagUseCase: LockTagUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<LockTagViewState, LockTagViewEvent>(LockTagViewState(), schedulers), LockTagViewScope {

  private var currentJob: Job? = null
  private var tagId: Long = 0

  fun loadData(tagId: Long) {
    this.tagId = tagId
    viewModelScope.launch {
      nfcTagRepository.findById(tagId)?.let { tag ->
        updateState { it.copy(tagName = tag.name) }
      }
    }
  }

  fun handleTag(tag: Tag) {
    val state = currentState()
    if (state.state != LockTagViewState.State.AWAITING_TAG || state.error != null) {
      Timber.i("Got intent but is not in awaiting state")
      return
    }

    if (currentJob != null) {
      Timber.i("Got intent but already processing")
      return
    }

    currentJob = viewModelScope.launch {
      when (val result = lockTagUseCase(tag, tagId)) {
        LockTagUseCase.Success -> updateState { it.copy(state = LockTagViewState.State.SUCCESS) }
        is LockTagUseCase.Failure -> updateState { it.copy(error = result.error) }
      }
    }
    currentJob?.invokeOnCompletion { currentJob = null }
  }

  override fun onCloseClick() {
    sendEvent(LockTagViewEvent.Close)
  }

  override fun onRetryClick() {
    updateState { it.copy(error = null) }
  }
}

sealed interface LockTagViewEvent : ViewEvent {
  data object Close : LockTagViewEvent
}
