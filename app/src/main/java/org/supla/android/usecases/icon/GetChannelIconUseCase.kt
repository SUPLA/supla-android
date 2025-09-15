package org.supla.android.usecases.icon
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

import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.model.general.IconType
import org.supla.android.db.ChannelBase
import org.supla.android.images.ImageCacheProxy
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.general.suplaFunction
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.extensions.ifLet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelIconUseCase @Inject constructor(
  private val getChannelStateUseCase: GetChannelStateUseCase,
  private val getDefaultIconResourceUseCase: GetDefaultIconResourceUseCase,
  private val imageCacheProxy: ImageCacheProxy
) {

  operator fun invoke(
    channelDataBase: ChannelDataBase,
    type: IconType = IconType.SINGLE,
    channelStateValue: ChannelState.Value? = null
  ): ImageId {
    if (type != IconType.SINGLE && channelDataBase.function != SuplaFunction.HUMIDITY_AND_TEMPERATURE) {
      throw IllegalArgumentException("Wrong icon type (iconType: '$type', function: '${channelDataBase.function}')!")
    }

    val state = channelStateValue?.let { ChannelState(it) } ?: getChannelStateUseCase(channelDataBase)

    ifLet(findUserIcon(channelDataBase, type, state)) { (id) ->
      return id
    }

    val iconData = IconData(
      function = channelDataBase.function,
      altIcon = channelDataBase.altIcon,
      state = state,
      type = type
    )
    return ImageId(getDefaultIconResourceUseCase(iconData))
  }

  operator fun invoke(
    channelBase: ChannelBase,
    type: IconType = IconType.SINGLE,
    channelState: ChannelState? = null
  ): ImageId? {
    if (type != IconType.SINGLE && channelBase.func != SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
      // TODO: Should be restored when ChannelLayout removed
      // throw IllegalArgumentException("Wrong icon type (iconType: '$type', function: '${channelBase.func}')!")
      return null
    }

    val state = channelState ?: getChannelStateUseCase(channelBase)

    ifLet(findUserIcon(channelBase, type, state)) { (id) ->
      return id
    }

    val iconData = IconData(
      function = channelBase.func.suplaFunction(),
      altIcon = channelBase.altIcon,
      state = state,
      type = type
    )

    return ImageId(getDefaultIconResourceUseCase(iconData))
  }

  fun forState(
    channelBase: org.supla.android.data.model.general.ChannelBase,
    channelState: ChannelState,
    type: IconType = IconType.SINGLE
  ): ImageId {
    if (type != IconType.SINGLE && channelBase.function != SuplaFunction.HUMIDITY_AND_TEMPERATURE) {
      throw IllegalArgumentException("Wrong icon type (iconType: '$type', function: '${channelBase.function}')!")
    }

    ifLet(findUserIcon(channelBase, type, channelState)) { (id) ->
      return id
    }

    val iconData = IconData(
      function = channelBase.function,
      altIcon = channelBase.altIcon,
      state = channelState,
      type = type
    )
    return ImageId(getDefaultIconResourceUseCase(iconData))
  }

  private fun findUserIcon(channelBase: ChannelBase, iconType: IconType, state: ChannelState): ImageId? {
    val (userIconId) = guardLet(channelBase.userIconId) { return null }
    if (userIconId == 0) {
      return null
    }

    return userImageId(channelBase.func, userIconId, iconType, state, channelBase.profileId).let {
      if (imageCacheProxy.bitmapExists(it)) {
        it
      } else {
        null
      }
    }
  }

  private fun findUserIcon(
    channelEntity: ChannelDataBase,
    iconType: IconType,
    state: ChannelState
  ): ImageId? {
    val (userIconId) = guardLet(channelEntity.userIcon) { return null }
    if (userIconId == 0) {
      return null
    }

    return userImageId(channelEntity.function.value, userIconId, iconType, state, channelEntity.profileId).let {
      if (imageCacheProxy.bitmapExists(it)) {
        it
      } else {
        null
      }
    }
  }

  private fun findUserIcon(
    channelBase: org.supla.android.data.model.general.ChannelBase,
    iconType: IconType,
    state: ChannelState
  ): ImageId? {
    val (userIconId) = guardLet(channelBase.userIcon) { return null }
    if (userIconId == 0) {
      return null
    }

    return userImageId(channelBase.function.value, userIconId, iconType, state, channelBase.profileId).let {
      if (imageCacheProxy.bitmapExists(it)) {
        it
      } else {
        null
      }
    }
  }

  private fun userImageId(
    function: Int,
    userIconId: Int,
    iconType: IconType,
    state: ChannelState,
    profileId: Long
  ): ImageId =
    when (function) {
      SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
        ImageId(userIconId, if (iconType == IconType.SECOND) 1 else 2, profileId)

      SUPLA_CHANNELFNC_THERMOMETER ->
        ImageId(userIconId, 1, profileId = profileId)

      SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
      SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR ->
        when (state.value) {
          ChannelState.Value.PARTIALLY_OPENED -> ImageId(userIconId, 3, profileId = profileId)
          ChannelState.Value.OPEN -> ImageId(userIconId, 1, profileId = profileId)
          else -> ImageId(userIconId, 2, profileId = profileId)
        }

      SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING ->
        when (state.complex) {
          listOf(ChannelState.Value.OFF, ChannelState.Value.OFF) -> ImageId(userIconId, 1, profileId)
          listOf(ChannelState.Value.ON, ChannelState.Value.OFF) -> ImageId(userIconId, 2, profileId)
          listOf(ChannelState.Value.OFF, ChannelState.Value.ON) -> ImageId(userIconId, 3, profileId)
          listOf(ChannelState.Value.ON, ChannelState.Value.ON) -> ImageId(userIconId, 4, profileId)
          else -> ImageId(userIconId, if (state.isActive()) 4 else 1, profileId)
        }

      else -> ImageId(userIconId, if (state.isActive()) 2 else 1, profileId)
    }

  // for java
  fun invoke(channelBase: ChannelBase): ImageId? {
    return invoke(channelBase, IconType.SINGLE, null)
  }

  fun invoke(channelBase: ChannelBase, type: IconType): ImageId? {
    return invoke(channelBase, type, null)
  }
}
