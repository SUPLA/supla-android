package org.supla.android.usecases.channel
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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.db.ChannelBase
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.SubjectType

open class BaseActionUseCase<T : ChannelBase>(
  private val suplaClientProvider: SuplaClientProvider
) {

  protected fun performActionCompletable(channelBase: T, buttonType: ButtonType, forGroup: Boolean): Completable =
    Completable.fromRunnable { performAction(channelBase, buttonType, forGroup) }

  protected open fun performAction(channelBase: T, buttonType: ButtonType, forGroup: Boolean) {
    if (buttonType == ButtonType.LEFT && isValveChannel(channelBase.func)) {
      throw ActionException.ChannelClosedManually(channelBase.remoteId)
    }

    val client = suplaClientProvider.provide() ?: return
    if (isRGBW(channelBase.func)) {
      client.executeAction(ActionParameters(getTurnOnOffActionId(buttonType), getSubjectType(forGroup), channelBase.remoteId))
    } else if (isRollerShutter(channelBase.func)) {
      client.executeAction(ActionParameters(getRevealShutActionId(buttonType), getSubjectType(forGroup), channelBase.remoteId))
    } else {
      client.open(channelBase.remoteId, forGroup, getOnOffValue(buttonType))
    }
  }

  private fun isValveChannel(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE ||
      function == SuplaConst.SUPLA_CHANNELFNC_VALVE_PERCENTAGE

  private fun isRollerShutter(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER ||
      function == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW

  private fun isRGBW(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING ||
      function == SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING ||
      function == SuplaConst.SUPLA_CHANNELFNC_DIMMER

  private fun getOnOffValue(buttonType: ButtonType): Int = when (buttonType) {
    ButtonType.LEFT -> 0
    ButtonType.RIGHT -> 1
  }

  private fun getTurnOnOffActionId(buttonType: ButtonType): ActionId = when (buttonType) {
    ButtonType.LEFT -> ActionId.TURN_OFF
    ButtonType.RIGHT -> ActionId.TURN_ON
  }

  private fun getRevealShutActionId(buttonType: ButtonType): ActionId = when (buttonType) {
    ButtonType.LEFT -> ActionId.SHUT
    ButtonType.RIGHT -> ActionId.REVEAL
  }

  private fun getSubjectType(group: Boolean) = if (group) {
    SubjectType.GROUP
  } else {
    SubjectType.CHANNEL
  }
}

enum class ButtonType { LEFT, RIGHT }

sealed class ActionException : Throwable() {
  data class ChannelClosedManually(val remoteId: Int) : ActionException()
  data class ChannelExceedAmperage(val remoteId: Int) : ActionException()
}