package org.supla.android.features.standarddetail.switchdetail
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
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.extensions.getTimerStateValue
import org.supla.android.lib.actions.ActionId
import org.supla.android.model.ItemType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SwitchDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<SwitchDetailViewState, SwitchDetailViewEvent>(SwitchDetailViewState(), schedulers) {

  fun loadData(remoteId: Int, itemType: ItemType) {
    getDataSource(remoteId, itemType)
      .attach()
      .subscribeBy(
        onSuccess = { channel ->
          updateState {
            it.copy(
              channelBase = channel,
              isOn = (channel as? Channel)?.value?.hiValue() ?: false,
              timerEndDate = getEstimatedCountDownEndTime(channel)
            )
          }
        }
      )
      .disposeBySelf()
  }

  private fun getDataSource(remoteId: Int, itemType: ItemType) = when (itemType) {
    ItemType.CHANNEL -> readChannelByRemoteIdUseCase(remoteId)
    ItemType.GROUP -> readChannelGroupByRemoteIdUseCase(remoteId)
  }

  fun turnOn(remoteId: Int, itemType: ItemType) {
    performAction(ActionId.TURN_ON, itemType, remoteId)
  }

  fun turnOff(remoteId: Int, itemType: ItemType) {
    performAction(ActionId.TURN_OFF, itemType, remoteId)
  }

  private fun performAction(actionId: ActionId, itemType: ItemType, remoteId: Int) {
    executeSimpleActionUseCase(actionId, itemType.subjectType, remoteId)
      .attach()
      .subscribeBy()
      .disposeBySelf()
  }

  private fun getEstimatedCountDownEndTime(channelBase: ChannelBase): Date? {
    return (channelBase as? Channel)?.let {
      val currentDate = dateProvider.currentDate()
      val estimatedEndDate = it.getTimerStateValue()?.countdownEndsAt

      if (estimatedEndDate?.after(currentDate) == true) {
        estimatedEndDate
      } else {
        null
      }
    }
  }
}

sealed class SwitchDetailViewEvent : ViewEvent

data class SwitchDetailViewState(
  val channelBase: ChannelBase? = null,
  val isOn: Boolean = false,
  val timerEndDate: Date? = null
) : ViewState()
