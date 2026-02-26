package org.supla.android.features.nfc.detail
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
import kotlinx.coroutines.launch
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.NfcCallRepository
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.local.entity.NfcCallEntity
import org.supla.android.data.source.local.entity.NfcCallResult
import org.supla.android.extensions.toLocalDateTime
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.extensions.invoke
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class NfcTagDetailViewModel @Inject constructor(
  private val getCaptionUseCase: GetCaptionUseCase,
  private val nfcCallRepository: NfcCallRepository,
  private val nfcTagRepository: NfcTagRepository,
  private val schedulers: SuplaSchedulers
) : BaseViewModel<NfcTagDetailViewState, NfcTagDetailViewEvent>(NfcTagDetailViewState(), schedulers), NfcTagDetailViewScope {

  private var itemId: Long = 0

  fun setItemId(itemId: Long) {
    this.itemId = itemId
  }

  fun delete() {
    updateState {
      if (it.tagLocked) {
        it.copy(dialogToShow = NfcTagDetailViewState.DialogType.DELETE_LOCKED_TAG)
      } else {
        it.copy(dialogToShow = NfcTagDetailViewState.DialogType.DELETE_TAG)
      }
    }
  }

  override fun onStart() {
    viewModelScope.launch {
      val tagData = schedulers.io { nfcTagRepository.findByIdWithDependencies(itemId) } ?: return@launch
      val readingItems = schedulers.io { nfcCallRepository.findLastForId(tagData.tagEntity.id).map { it.toReadingItem } }

      updateState { state ->
        state.copy(
          tagName = tagData.tagEntity.name,
          tagUuid = tagData.tagEntity.uuid,
          tagLocked = tagData.tagEntity.readOnly,
          actionId = tagData.tagEntity.actionId,
          subjectName = tagData.channelEntity?.let { getCaptionUseCase(it) },
          lastReadingItems = readingItems
        )
      }
      sendEvent(NfcTagDetailViewEvent.SetToolbarTitle(tagData.tagEntity.name))
    }
  }

  override fun onInfoClick() {
    updateState { it.copy(dialogToShow = NfcTagDetailViewState.DialogType.INFO) }
  }

  override fun onLockClick() {
    sendEvent(NfcTagDetailViewEvent.LockTag)
  }

  override fun onEditClick() {
    sendEvent(NfcTagDetailViewEvent.EditTag)
  }

  override fun onDeleteClick() {
    viewModelScope.launch {
      nfcTagRepository.delete(itemId)
      sendEvent(NfcTagDetailViewEvent.Close)
    }
  }

  override fun onDismissDialogs() {
    updateState { it.copy(dialogToShow = null) }
  }
}

sealed interface NfcTagDetailViewEvent : ViewEvent {
  data object EditTag : NfcTagDetailViewEvent
  data object Close : NfcTagDetailViewEvent
  data object LockTag : NfcTagDetailViewEvent
  data class SetToolbarTitle(val tagName: String) : NfcTagDetailViewEvent
}

private val NfcCallEntity.toReadingItem: NfcTagDetailViewState.ReadingItem
  get() = NfcTagDetailViewState.ReadingItem(date.toLocalDateTime(), result)
