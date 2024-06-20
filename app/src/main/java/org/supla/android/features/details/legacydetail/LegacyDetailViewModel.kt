package org.supla.android.features.details.legacydetail
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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.db.ChannelBase
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class LegacyDetailViewModel @Inject constructor(
  private val channelRepository: ChannelRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<LegacyDetailViewState, LegacyDetailViewEvent>(LegacyDetailViewState(), schedulers) {

  fun loadData(remoteId: Int, itemType: ItemType) {
    getDataSource(remoteId, itemType)
      .attach()
      .subscribeBy(
        onSuccess = { sendEvent(LegacyDetailViewEvent.LoadDetailView(it)) },
        onError = defaultErrorHandler("loadData($remoteId, $itemType)")
      )
      .disposeBySelf()
  }

  private fun getDataSource(remoteId: Int, itemType: ItemType) = when (itemType) {
    ItemType.CHANNEL -> Maybe.fromCallable { channelRepository.getChannel(remoteId) }
    ItemType.GROUP -> Maybe.fromCallable { channelRepository.getChannelGroup(remoteId) }
  }
}

sealed class LegacyDetailViewEvent : ViewEvent {
  data class LoadDetailView(val channelBase: ChannelBase) : LegacyDetailViewEvent()
}

class LegacyDetailViewState : ViewState()
