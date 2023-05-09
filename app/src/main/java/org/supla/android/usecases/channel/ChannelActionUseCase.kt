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
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Channel
import org.supla.android.lib.SuplaConst
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelActionUseCase @Inject constructor(
  private val channelRepository: ChannelRepository,
  suplaClientProvider: SuplaClientProvider
) : BaseActionUseCase<Channel>(suplaClientProvider) {

  operator fun invoke(channelId: Int, buttonType: ButtonType): Completable =
    getChannel(channelId).flatMapCompletable { performActionCompletable(it, buttonType, false) }

  override fun performAction(channel: Channel, buttonType: ButtonType, forGroup: Boolean) {
    if (isOnOff(channel.func) && buttonType == ButtonType.RIGHT && channel.value.hiValue().not() && channel.value.overcurrentRelayOff()) {
      throw ActionException.ChannelExceedAmperage(channel.channelId)
    }

    super.performAction(channel, buttonType, forGroup)
  }

  private fun getChannel(channelId: Int): Maybe<Channel> = Maybe.fromCallable { channelRepository.getChannel(channelId) }

  private fun isOnOff(channelFunction: Int): Boolean =
    channelFunction == SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH ||
      channelFunction == SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH ||
      channelFunction == SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
}