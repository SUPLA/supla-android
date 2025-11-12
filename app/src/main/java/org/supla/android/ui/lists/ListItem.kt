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
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import java.util.Date

sealed interface ListItem {

  fun isDifferentFrom(another: ListItem): Boolean {
    if (this is SceneItem && another is SceneItem) {
      return sceneData.remoteId != another.sceneData.remoteId ||
        sceneData.sceneEntity.caption != another.sceneData.sceneEntity.caption
    }
    if (this is LocationItem && another is LocationItem) {
      return location != another.location
    }

    if (this is ChannelBasedItem && another is ChannelBasedItem) {
      return channelBase.remoteId != another.channelBase.remoteId ||
        channelBase.function != another.channelBase.function ||
        channelBase.status != another.channelBase.status ||
        channelBase.caption != another.channelBase.caption
    }

    return true
  }

  abstract class ChannelBasedItem(
    open val channelBase: ChannelDataBase
  ) : ListItem

  abstract class DefaultItem(
    val channel: ChannelDataEntity,
    val locationCaption: String,
    val online: ListOnlineState,
    val captionProvider: LocalizedString,
    val icon: ImageId,
    val value: String?,
    val issues: ListItemIssues
  ) : ChannelBasedItem(channel) {
    open fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.Default(
        onlineState = online,
        title = captionProvider,
        icon = icon,
        value = value,
        issues = issues,
        estimatedTimerEndDate = null,
        infoSupported = channel.showInfo
      )
    }
  }

  data class SceneItem(val sceneData: SceneDataEntity) : ListItem
  data class LocationItem(val location: LocationEntity) : ListItem

  class ChannelItem(
    override var channelBase: ChannelDataBase,
    val legacyBase: org.supla.android.db.ChannelBase
  ) : ChannelBasedItem(channelBase)

  class HvacThermostatItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: ListOnlineState,
    captionProvider: LocalizedString,
    icon: ImageId,
    value: String?,
    issues: ListItemIssues,
    private val estimatedTimerEndDate: Date?,
    private val subValue: String,
    @DrawableRes private val indicatorIcon: Int?
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, issues) {
    override fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.Thermostat(
        onlineState = online,
        title = captionProvider,
        icon = icon,
        issues = issues,
        estimatedTimerEndDate = estimatedTimerEndDate,
        value = value ?: NO_VALUE_TEXT,
        subValue = subValue,
        indicatorIcon = indicatorIcon,
        infoSupported = channel.showInfo
      )
    }
  }

  class HeatpolThermostatItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: ListOnlineState,
    captionProvider: LocalizedString,
    icon: ImageId,
    value: String?,
    issues: ListItemIssues,
    private val subValue: String,
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, issues) {
    override fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.Thermostat(
        onlineState = online,
        title = captionProvider,
        icon = icon,
        issues = issues,
        estimatedTimerEndDate = null,
        value = value ?: NO_VALUE_TEXT,
        subValue = subValue,
        indicatorIcon = null,
        infoSupported = channel.showInfo
      )
    }
  }

  class DoubleValueItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: ListOnlineState,
    captionProvider: LocalizedString,
    icon: ImageId,
    value: String?,
    issues: ListItemIssues,
    private val secondIcon: ImageId?,
    private val secondValue: String?
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, issues) {
    override fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.DoubleValue(
        onlineState = online,
        title = captionProvider,
        icon = icon,
        issues = issues,
        value = value ?: NO_VALUE_TEXT,
        infoSupported = channel.showInfo,
        secondIcon = secondIcon,
        secondValue = secondValue
      )
    }
  }

  class IconValueItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: ListOnlineState,
    captionProvider: LocalizedString,
    icon: ImageId,
    value: String? = null,
    issues: ListItemIssues
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, issues)

  class IconWithButtonsItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: ListOnlineState,
    captionProvider: LocalizedString,
    icon: ImageId,
    value: String?,
    private val estimatedTimerEndDate: Date?,
    issues: ListItemIssues
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, issues) {
    override fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.Default(
        onlineState = online,
        title = captionProvider,
        icon = icon,
        value = value,
        issues = issues,
        estimatedTimerEndDate = estimatedTimerEndDate,
        infoSupported = channel.showInfo
      )
    }
  }

  class IconWithRightButtonItem(
    channel: ChannelDataEntity,
    locationCaption: String,
    online: ListOnlineState,
    captionProvider: LocalizedString,
    icon: ImageId,
    value: String?,
    private val estimatedTimerEndDate: Date?,
    issues: ListItemIssues
  ) : DefaultItem(channel, locationCaption, online, captionProvider, icon, value, issues) {
    override fun toSlideableListItemData(): SlideableListItemData {
      return SlideableListItemData.Default(
        onlineState = online,
        title = captionProvider,
        icon = icon,
        value = value,
        issues = issues,
        estimatedTimerEndDate = estimatedTimerEndDate,
        infoSupported = channel.showInfo
      )
    }
  }
}
