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

import android.content.Context
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.model.general.ChannelBase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelCaptionUseCase @Inject constructor(
  private val getChannelDefaultCaptionUseCase: GetChannelDefaultCaptionUseCase
) {

  operator fun invoke(channelEntity: ChannelBase): StringProvider {
    if (channelEntity.caption.trim().isEmpty()) {
      return getChannelDefaultCaptionUseCase(channelEntity.function)
    } else {
      return { channelEntity.caption }
    }
  }

  operator fun invoke(channelEntity: ChannelBase, context: Context): String {
    return if (channelEntity.caption.trim().isEmpty()) {
      getChannelDefaultCaptionUseCase(channelEntity.function)(context)
    } else {
      channelEntity.caption
    }
  }
}
