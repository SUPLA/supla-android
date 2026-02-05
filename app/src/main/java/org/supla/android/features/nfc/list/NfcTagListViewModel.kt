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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.local.entity.complex.NfcTagDataEntity
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import javax.inject.Inject

@HiltViewModel
class NfcTagListViewModel @Inject constructor(
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getSceneIconUseCase: GetSceneIconUseCase,
  private val nfcTagRepository: NfcTagRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<NfcTagListViewModelState, NfcTagListViewEvent>(NfcTagListViewModelState(), schedulers), NfcTagListScope {

  override fun onStart() {
    viewModelScope.launch {
      val tags = nfcTagRepository.findAllWithDependencies()
      updateState { state ->
        state.copy(viewState = state.viewState.copy(items = tags.map { it.toItem }))
      }
    }
  }

  override fun onAddClick() {
    sendEvent(NfcTagListViewEvent.NavigateToAdd)
  }

  override fun onItemClick(item: NfcTagItem) {
    sendEvent(NfcTagListViewEvent.NavigateToItemEdit(item.id))
  }

  private val NfcTagDataEntity.toItem
    get() = NfcTagItem(
      id = tagEntity.id,
      uuid = tagEntity.uuid,
      name = tagEntity.name,
      icon = icon(getChannelIconUseCase, getSceneIconUseCase),
      profileName = profileEntity?.name
    )
}

sealed interface NfcTagListViewEvent : ViewEvent {
  data object NavigateToAdd : NfcTagListViewEvent
  data class NavigateToItemEdit(val id: Long) : NfcTagListViewEvent
}

data class NfcTagListViewModelState(
  val viewState: NfcTagListViewState = NfcTagListViewState()
) : ViewState()
