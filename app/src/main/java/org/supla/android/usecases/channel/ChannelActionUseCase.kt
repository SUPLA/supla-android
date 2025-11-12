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
import io.reactivex.rxjava3.core.Maybe
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.lib.SuplaConst
import org.supla.core.shared.data.model.function.relay.SuplaRelayFlag
import org.supla.core.shared.data.model.valve.SuplaValveFlag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelActionUseCase @Inject constructor(
  private val channelRepository: RoomChannelRepository,
  suplaClientProvider: SuplaClientProvider
) : BaseActionUseCase<ChannelDataEntity>(suplaClientProvider) {

  operator fun invoke(channelId: Int, buttonType: ButtonType): Completable =
    getChannel(channelId).flatMapCompletable { performActionCompletable(it, buttonType, false) }

  override fun performAction(channelBase: ChannelDataEntity, buttonType: ButtonType, forGroup: Boolean) {
    if (isOnOff(channelBase.function.value) && buttonType == ButtonType.RIGHT && isOvercurrentRelayOff(channelBase)) {
      throw ActionException.ChannelExceedAmperage(channelBase.remoteId)
    }

    if (isValveChannel(channelBase.function.value)) {
      when (buttonType) {
        ButtonType.RIGHT -> {
          if (isManuallyClosed(channelBase)) {
            throw ActionException.ValveClosedManually(channelBase.remoteId)
          } else if (isFlooding(channelBase)) {
            throw ActionException.ValveFloodingAlarm(channelBase.remoteId)
          } else if (isValveMotorProblem(channelBase)) {
            throw ActionException.ValveMotorProblemOpening(channelBase.remoteId)
          }
        }
        ButtonType.LEFT -> {
          if (isValveMotorProblem(channelBase)) {
            throw ActionException.ValveMotorProblemClosing(channelBase.remoteId)
          }
        }
      }
    }

    super.performAction(channelBase, buttonType, forGroup)
  }

  private fun getChannel(channelId: Int): Maybe<ChannelDataEntity> =
    channelRepository.findChannelDataEntity(channelId).firstElement()

  private fun isOnOff(channelFunction: Int): Boolean =
    channelFunction == SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH ||
      channelFunction == SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH ||
      channelFunction == SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER

  private fun isValveChannel(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE ||
      function == SuplaConst.SUPLA_CHANNELFNC_VALVE_PERCENTAGE

  private fun isOvercurrentRelayOff(channelDataEntity: ChannelDataEntity) =
    channelDataEntity.channelValueEntity.asRelayValue().let {
      it.on.not() && it.flags.contains(SuplaRelayFlag.OVERCURRENT_RELAY_OFF)
    }

  private fun isManuallyClosed(channel: ChannelDataEntity): Boolean =
    channel.channelValueEntity.asValveValue().let {
      it.isClosed() && it.flags.contains(SuplaValveFlag.MANUALLY_CLOSED)
    }

  private fun isFlooding(channel: ChannelDataEntity): Boolean =
    channel.channelValueEntity.asValveValue().let {
      it.isClosed() && it.flags.contains(SuplaValveFlag.FLOODING)
    }

  private fun isValveMotorProblem(channel: ChannelDataEntity): Boolean =
    channel.channelValueEntity.asValveValue().flags.contains(SuplaValveFlag.MOTOR_PROBLEM)
}
