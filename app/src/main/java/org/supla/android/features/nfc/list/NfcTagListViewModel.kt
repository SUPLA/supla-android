package org.supla.android.features.nfc.list
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

import android.nfc.NfcAdapter
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.complex.NfcTagDataEntity
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.extensions.invoke
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.core.shared.extensions.ifFalse
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class NfcTagListViewModel @Inject constructor(
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getSceneIconUseCase: GetSceneIconUseCase,
  private val profileRepository: RoomProfileRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val nfcTagRepository: NfcTagRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<NfcTagListViewModelState, NfcTagListViewEvent>(NfcTagListViewModelState(), schedulers), NfcTagListScope {

  fun onStart(nfcAdapter: NfcAdapter?) {
    val nfcState = when {
      nfcAdapter == null -> NfcTagListViewState.NfcState.NOT_SUPPORTED
      nfcAdapter.isEnabled -> NfcTagListViewState.NfcState.ENABLED
      else -> NfcTagListViewState.NfcState.DISABLED
    }

    viewModelScope.launch {
      val profilesCount = profileRepository.findAllProfilesKtx().count()
      val tags = nfcTagRepository.findAllWithDependencies()
      updateState { state ->
        state.copy(
          viewState = state.viewState.copy(
            items = tags.map { it.toItem(profilesCount) },
            nfcState = nfcState
          )
        )
      }
    }
  }

  override fun onAddClick() {
    if (currentState().viewState.nfcState == NfcTagListViewState.NfcState.ENABLED) {
      sendEvent(NfcTagListViewEvent.NavigateToAdd)
    } else {
      updateState { it.copy(viewState = it.viewState.copy(showNfcDialog = true)) }
    }
  }

  override fun onItemClick(item: NfcTagItem) {
    sendEvent(NfcTagListViewEvent.NavigateToItemDetail(item.id))
  }

  override fun onNfcSettingsClick() {
    sendEvent(NfcTagListViewEvent.NavigateToNfcSettings)
  }

  override fun onNfcDialogDismiss() {
    updateState { it.copy(viewState = it.viewState.copy(showNfcDialog = false)) }
  }

  private fun NfcTagDataEntity.toItem(profilesCount: Int = 0) =
    NfcTagItem(
      id = tagEntity.id,
      name = tagEntity.name,
      icon = icon(getChannelIconUseCase, getSceneIconUseCase),
      profileName = (profilesCount == 1).ifFalse(profileEntity?.name),
      channelName = channelEntity?.let { getCaptionUseCase(it) },
      action = tagEntity.actionId,
      readOnly = tagEntity.readOnly,
      channelNotExists =
      when (tagEntity.subjectType) {
        SubjectType.CHANNEL -> tagEntity.subjectId != null && channelEntity == null
        SubjectType.GROUP -> tagEntity.subjectId != null && groupEntity == null
        SubjectType.SCENE -> tagEntity.subjectId != null && sceneEntity == null
        null -> false
      }
    )
}

sealed interface NfcTagListViewEvent : ViewEvent {
  data object NavigateToAdd : NfcTagListViewEvent
  data class NavigateToItemDetail(val id: Long) : NfcTagListViewEvent
  data object NavigateToNfcSettings : NfcTagListViewEvent
}

data class NfcTagListViewModelState(
  val viewState: NfcTagListViewState = NfcTagListViewState()
) : ViewState()
