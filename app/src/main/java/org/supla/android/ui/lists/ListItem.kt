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
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.ui.lists.data.SlideableListItemData
import java.util.Date

sealed interface ListItem {

  abstract class ChannelBasedItem(
    open val channelBase: ChannelDataBase
  ) : ListItem

  abstract class DefaultItem(
    val channel: ChannelDataEntity,
    val locationCaption: String,
    val online: Boolean,
    val captionProvider: StringProvider,
    val icon: ImageId,
    val value: String?,
    val issueIconType: IssueIconType?,
    val issueMessage: Int?
  ) : ChannelBasedItem(channel) {
    open fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.Default(
        online = online,
        titleProvider = captionProvider,
        icon = icon,
        value = value,
        issueIconType = issueIconType,
        estimatedTimerEndDate = null,
        infoSupported = SuplaChannelFlag.CHANNEL_STATE.inside(channel.flags)
      )
    }
  }

  data class SceneItem(val sceneData: SceneDataEntity) : ListItem
  data class ChannelItem(
    override var channelBase: ChannelDataBase,
    val children: List<ChannelChildEntity>? = null,
    val legacyBase: org.supla.android.db.ChannelBase
  ) : ChannelBasedItem(channelBase)

  data class LocationItem(val location: LocationEntity) : ListItem

  class HvacThermostatItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    value: String?,
    issueIconType: IssueIconType?,
    issueMessage: Int?,
    private val estimatedTimerEndDate: Date?,
    private val subValue: String,
    @DrawableRes private val indicatorIcon: Int?
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, issueIconType, issueMessage) {
    override fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.Thermostat(
        online = online,
        titleProvider = captionProvider,
        icon = icon,
        issueIconType = issueIconType,
        estimatedTimerEndDate = estimatedTimerEndDate,
        value = value ?: ValuesFormatter.NO_VALUE_TEXT,
        subValue = subValue,
        indicatorIcon = indicatorIcon,
        infoSupported = SuplaChannelFlag.CHANNEL_STATE.inside(channel.flags)
      )
    }
  }

  class MeasurementItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    value: String? = null
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, null, null)

  class ShadingSystemItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    issueIconType: IssueIconType?,
    issueMessage: Int?
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, null, issueIconType, issueMessage)

  class SwitchItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    value: String?
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, null, null)

  class GeneralPurposeMeterItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    value: String
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, null, null)

  class GeneralPurposeMeasurementItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: Boolean,
    captionProvider: StringProvider,
    icon: ImageId,
    value: String
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, null, null)
}
