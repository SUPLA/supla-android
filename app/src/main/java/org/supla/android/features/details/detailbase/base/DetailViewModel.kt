package org.supla.android.features.details.detailbase.base
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
import kotlinx.coroutines.rx3.awaitFirst
import org.supla.android.core.ui.EventBasedViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.tools.SuplaSchedulers
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
  private val channelGroupRepository: ChannelGroupRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val channelRepository: ChannelRepository,
  private val schedulers: SuplaSchedulers
) : EventBasedViewModel<DetailViewEvent>(manageScreenTitle = true) {

  fun loadTitle(item: ItemBundle) {
    viewModelScope.launch {
      val data = schedulers.io {
        runCatching {
          when (item.itemType) {
            ItemType.CHANNEL -> channelRepository.findChannelDataEntity(item.remoteId).map { it.shareable }
            ItemType.GROUP -> channelGroupRepository.findGroupDataEntity(item.remoteId).map { it.shareable }
          }.awaitFirst()
        }.getOrNull()
      }

      data?.let {
        setScreenTitle(getCaptionUseCase(it))
      }
    }
  }
}

sealed interface DetailViewEvent : ViewEvent
