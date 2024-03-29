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

import org.supla.android.core.ui.BitmapProvider
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.db.ChannelBase
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifLet
import org.supla.android.images.ImageCacheProxy
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.stateWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelIconUseCase @Inject constructor(
  private val getChannelStateUseCase: GetChannelStateUseCase,
  private val getDefaultIconResourceUseCase: GetDefaultIconResourceUseCase,
  private val imageCacheProxy: ImageCacheProxy
) {

  fun getIconProvider(channelData: ChannelDataEntity, iconType: IconType = IconType.SINGLE): BitmapProvider =
    { imageCacheProxy.getBitmap(it, invoke(channelData, iconType)) }

  // We intentionally specify icons with the _nighthtmode
  // suffix for night mode instead of using the default icons
  // from the drawable-night directory because not every
  // part of the application is night mode enabled yet.
  operator fun invoke(
    channelData: ChannelDataEntity,
    type: IconType = IconType.SINGLE,
    nightMode: Boolean = false,
    channelStateValue: ChannelState.Value? = null
  ): ImageId {
    if (type != IconType.SINGLE && channelData.function != SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
      throw IllegalArgumentException("Wrong icon type (iconType: '$type', function: '${channelData.function}')!")
    }

    val stateWrapper = channelData.channelValueEntity.toStateWrapper()
    val state = channelStateValue?.let { ChannelState(it) } ?: getChannelStateUseCase(channelData.function, stateWrapper)

    ifLet(findUserIcon(channelData.channelEntity, type, state)) { (id) ->
      return id
    }

    val iconData = IconData(
      function = channelData.function,
      altIcon = channelData.channelEntity.altIcon,
      state = state,
      type = type,
      nightMode = nightMode
    )
    return ImageId(getDefaultIconResourceUseCase(iconData))
  }

  operator fun invoke(
    channelBase: ChannelBase,
    type: IconType = IconType.SINGLE,
    nightMode: Boolean = false,
    channelStateValue: ChannelState.Value? = null
  ): ImageId? {
    if (type != IconType.SINGLE && channelBase.func != SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
      // TODO: Should be restored when ChannelLayout removed
      // throw IllegalArgumentException("Wrong icon type (iconType: '$type', function: '${channelBase.func}')!")
      return null
    }
    val (stateWrapper) = guardLet(channelBase.stateWrapper) {
      throw IllegalArgumentException("Could not get state wrapper from the given channel base!")
    }

    val state = channelStateValue?.let { ChannelState(it) } ?: getChannelStateUseCase(channelBase.func, stateWrapper)

    ifLet(findUserIcon(channelBase, type, state)) { (id) ->
      return id
    }

    val iconData = IconData(
      function = channelBase.func,
      altIcon = channelBase.altIcon,
      state = state,
      type = type,
      nightMode = nightMode
    )

    return ImageId(getDefaultIconResourceUseCase(iconData))
  }

  private fun findUserIcon(channelBase: ChannelBase, iconType: IconType, state: ChannelState): ImageId? {
    val (userIconId) = guardLet(channelBase.userIconId) { return null }
    if (userIconId == 0) {
      return null
    }

    val id = when (channelBase.func) {
      SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
        ImageId(userIconId, if (iconType == IconType.SECOND) 1 else 2, channelBase.profileId)

      SUPLA_CHANNELFNC_THERMOMETER ->
        ImageId(userIconId, 1, profileId = channelBase.profileId)

      SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
      SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR ->
        when (state.value) {
          ChannelState.Value.PARTIALLY_OPENED -> ImageId(userIconId, 3, profileId = channelBase.profileId)
          ChannelState.Value.OPEN -> ImageId(userIconId, 1, profileId = channelBase.profileId)
          else -> ImageId(userIconId, 2, profileId = channelBase.profileId)
        }

      else -> ImageId(userIconId, if (state.isActive()) 2 else 1, channelBase.profileId)
    }

    return if (imageCacheProxy.bitmapExists(id)) {
      id
    } else {
      null
    }
  }

  private fun findUserIcon(channelEntity: ChannelEntity, iconType: IconType, state: ChannelState): ImageId? {
    val (userIconId) = guardLet(channelEntity.userIcon) { return null }
    if (userIconId == 0) {
      return null
    }

    val id = when (channelEntity.function) {
      SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
        ImageId(userIconId, if (iconType == IconType.SECOND) 1 else 2, channelEntity.profileId)

      SUPLA_CHANNELFNC_THERMOMETER ->
        ImageId(userIconId, 1, profileId = channelEntity.profileId)

      SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
      SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR ->
        when (state.value) {
          ChannelState.Value.PARTIALLY_OPENED -> ImageId(userIconId, 3, profileId = channelEntity.profileId)
          ChannelState.Value.OPEN -> ImageId(userIconId, 1, profileId = channelEntity.profileId)
          else -> ImageId(userIconId, 2, profileId = channelEntity.profileId)
        }

      else -> ImageId(userIconId, if (state.isActive()) 2 else 1, channelEntity.profileId)
    }

    return if (imageCacheProxy.bitmapExists(id)) {
      id
    } else {
      null
    }
  }

  // for java
  fun invoke(channelBase: ChannelBase): ImageId? {
    return invoke(channelBase, IconType.SINGLE, false, null)
  }

  fun invoke(channelBase: ChannelBase, type: IconType): ImageId? {
    return invoke(channelBase, type, false, null)
  }
}
