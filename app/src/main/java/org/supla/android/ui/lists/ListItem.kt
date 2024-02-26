package org.supla.android.ui.lists
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
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.Scene
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.Location
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.ui.lists.data.SlideableListItemData
import java.util.Date

sealed interface ListItem {

  abstract class ChannelBasedItem(
    open val channelBase: ChannelBase
  ) : ListItem

  abstract class DefaultItem(
    val channel: Channel,
    val locationCaption: String,
    val online: Boolean,
    val captionProvider: StringProvider,
    val icon: ImageId,
    val value: String?
  ) : ChannelBasedItem(channel) {
    open fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.Default(
        online = online,
        titleProvider = captionProvider,
        icon = icon,
        value = value,
        issueIconType = null,
        estimatedTimerEndDate = null
      )
    }
  }

  data class SceneItem(val scene: Scene, val location: Location) : ListItem
  data class ChannelItem(override var channelBase: ChannelBase, val location: Location, val children: List<ChannelChildEntity>? = null) :
    ChannelBasedItem(channelBase)

  data class LocationItem(val location: Location) : ListItem

  class HvacThermostatItem(
    channel: Channel,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    value: String?,
    private val issueIconType: IssueIconType?,
    private val estimatedTimerEndDate: Date?,
    private val subValue: String,
    @DrawableRes private val indicatorIcon: Int?,
    val issueMessage: Int?
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value) {
    override fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.Thermostat(
        online = online,
        titleProvider = captionProvider,
        icon = icon,
        issueIconType = issueIconType,
        estimatedTimerEndDate = estimatedTimerEndDate,
        value = value ?: ValuesFormatter.NO_VALUE_TEXT,
        subValue = subValue,
        indicatorIcon = indicatorIcon
      )
    }
  }

  class MeasurementItem(
    channel: Channel,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    value: String?
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value)

  class GeneralPurposeMeterItem(
    channel: Channel,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    value: String
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value)

  class GeneralPurposeMeasurementItem(
    channel: Channel,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    value: String
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value)
}
