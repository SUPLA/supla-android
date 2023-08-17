package org.supla.android.ui.lists.data
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

import androidx.annotation.DrawableRes
import org.supla.android.core.ui.BitmapProvider
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.db.Channel
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.toSlideableListItemData
import org.supla.android.ui.lists.ListItem

sealed class SlideableListItemData {
  abstract val online: Boolean
  abstract val titleProvider: StringProvider
  abstract val iconProvider: BitmapProvider?

  data class Thermostat(
    override val online: Boolean,
    override val titleProvider: StringProvider,
    override val iconProvider: BitmapProvider?,
    val value: String,
    val subValue: String,
    @DrawableRes val indicatorIcon: Int?
  ) : SlideableListItemData() {
    companion object
  }

  data class Default(
    override val online: Boolean,
    override val titleProvider: StringProvider,
    override val iconProvider: BitmapProvider?
  ) : SlideableListItemData()
}

fun SlideableListItemData.Thermostat.Companion.default(): SlideableListItemData.Thermostat =
  SlideableListItemData.Thermostat(
    online = false,
    titleProvider = { "" },
    iconProvider = null,
    value = "",
    subValue = "",
    indicatorIcon = null
  )

fun ListItem.ChannelItem.data(valuesFormatter: ValuesFormatter): SlideableListItemData.Thermostat {
  val (channel) = guardLet(channelBase as? Channel) {
    throw IllegalArgumentException("Expected Channel but got $channelBase")
  }
  val child = children?.firstOrNull { it.relationType == ChannelRelationType.MAIN_THERMOMETER }

  return channel.toSlideableListItemData(child?.channel, valuesFormatter)
}
