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
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.local.entity.isGarageDoorRoller
import org.supla.android.data.source.local.entity.isProjectorScreen
import org.supla.android.data.source.local.entity.isShadingSystem
import org.supla.android.data.source.local.entity.isThermostat
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.IGNORE_CCT
import org.supla.android.lib.actions.IGNORE_COLOR
import org.supla.android.lib.actions.RgbwActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.core.shared.data.model.general.SuplaFunction

open class BaseActionUseCase<T : ChannelDataBase>(
  private val suplaClientProvider: SuplaClientProvider
) {

  protected fun performActionCompletable(channelBase: T, buttonType: ButtonType, forGroup: Boolean): Completable =
    Completable.fromRunnable { performAction(channelBase, buttonType, forGroup) }

  protected open fun performAction(channelBase: T, buttonType: ButtonType, forGroup: Boolean) {
    val client = suplaClientProvider.provide() ?: return
    if (isRGBW(channelBase.function)) {
      client.executeAction(getRgbwParameters(buttonType, forGroup, channelBase.remoteId))
    } else if (channelBase.isShadingSystem() || channelBase.isProjectorScreen() || channelBase.isGarageDoorRoller()) {
      if (SuplaChannelFlag.RS_SBS_AND_STOP_ACTIONS inside channelBase.flags) {
        client.executeAction(ActionParameters(getRevealShutStopActionId(buttonType), getSubjectType(forGroup), channelBase.remoteId))
      } else {
        client.executeAction(ActionParameters(getRevealShutActionId(buttonType), getSubjectType(forGroup), channelBase.remoteId))
      }
    } else if (channelBase.isThermostat()) {
      if (buttonType == ButtonType.RIGHT) {
        client.executeAction(ActionParameters(ActionId.TURN_ON, getSubjectType(forGroup), channelBase.remoteId))
      } else {
        client.executeAction(ActionParameters(ActionId.TURN_OFF, getSubjectType(forGroup), channelBase.remoteId))
      }
    } else {
      client.open(channelBase.remoteId, forGroup, getOnOffValue(buttonType))
    }
  }

  private fun isRGBW(function: SuplaFunction): Boolean =
    when (function) {
      SuplaFunction.DIMMER,
      SuplaFunction.DIMMER_CCT,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.DIMMER_CCT_AND_RGB,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING -> true

      else -> false
    }

  private fun getOnOffValue(buttonType: ButtonType): Int = when (buttonType) {
    ButtonType.LEFT -> 0
    ButtonType.RIGHT -> 1
  }

  private fun getRgbwParameters(buttonType: ButtonType, forGroup: Boolean, remoteId: Int): RgbwActionParameters {
    val brightness: Short = when (buttonType) {
      ButtonType.LEFT -> 0
      ButtonType.RIGHT -> 100
    }

    return RgbwActionParameters(
      action = ActionId.SET_RGBW_PARAMETERS,
      subjectType = getSubjectType(forGroup),
      subjectId = remoteId,
      brightness = brightness,
      color = IGNORE_COLOR,
      colorBrightness = brightness,
      colorRandom = false,
      whiteTemperature = IGNORE_CCT,
      onOff = true
    )
  }

  private fun getRevealShutActionId(buttonType: ButtonType): ActionId = when (buttonType) {
    ButtonType.LEFT -> ActionId.SHUT
    ButtonType.RIGHT -> ActionId.REVEAL
  }

  private fun getRevealShutStopActionId(buttonType: ButtonType): ActionId = when (buttonType) {
    ButtonType.LEFT -> ActionId.DOWN_OR_STOP
    ButtonType.RIGHT -> ActionId.UP_OR_STOP
  }

  private fun getSubjectType(group: Boolean) = if (group) {
    SubjectType.GROUP
  } else {
    SubjectType.CHANNEL
  }
}

enum class ButtonType { LEFT, RIGHT }

sealed class ActionException : Throwable() {
  data class ValveClosedManually(val remoteId: Int) : ActionException()
  data class ValveFloodingAlarm(val remoteId: Int) : ActionException()
  data class ValveMotorProblemOpening(val remoteId: Int) : ActionException()
  data class ValveMotorProblemClosing(val remoteId: Int) : ActionException()
  data class ChannelExceedAmperage(val remoteId: Int) : ActionException()
}
