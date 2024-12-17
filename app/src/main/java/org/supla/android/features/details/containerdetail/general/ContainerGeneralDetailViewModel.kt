package org.supla.android.features.details.containerdetail.general
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
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.extensions.toHex
import javax.inject.Inject

@HiltViewModel
class ContainerGeneralDetailViewModel @Inject constructor(
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<ContainerGeneralDetailViewModeState, ContainerGeneralDetailViewEvent>(
  ContainerGeneralDetailViewModeState(),
  schedulers
) {

  fun loadData(remoteId: Int) {
    readChannelWithChildrenUseCase(remoteId)
      .attach()
      .subscribeBy(
        onSuccess = this::handle,
        onError = defaultErrorHandler("loadData()")
      )
      .disposeBySelf()
  }

  private fun handle(channelWithChildren: ChannelWithChildren) {
    updateState {
      it.copy(
        viewState = it.viewState.copy(
          icon = getChannelIconUseCase(channelWithChildren.channel),
          level = getChannelValueStringUseCase(channelWithChildren.channel),
          value = channelWithChildren.channel.channelValueEntity.getValueAsByteArray().toHex("-")
        )
      )
    }
  }
}

sealed class ContainerGeneralDetailViewEvent : ViewEvent

data class ContainerGeneralDetailViewModeState(
  val viewState: ContainerGeneralDetailViewState = ContainerGeneralDetailViewState()
) : ViewState()
