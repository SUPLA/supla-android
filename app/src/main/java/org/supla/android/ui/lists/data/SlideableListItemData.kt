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
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListOnlineState
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import java.util.Date

sealed class SlideableListItemData {
  abstract val onlineState: ListOnlineState
  abstract val title: LocalizedString
  abstract val icon: ImageId?
  abstract val issues: ListItemIssues
  abstract val estimatedTimerEndDate: Date?
  abstract val infoSupported: Boolean

  data class Thermostat(
    override val onlineState: ListOnlineState,
    override val title: LocalizedString,
    override val icon: ImageId?,
    override val issues: ListItemIssues,
    override val estimatedTimerEndDate: Date?,
    override val infoSupported: Boolean,
    val value: String,
    val subValue: String,
    @DrawableRes val indicatorIcon: Int?
  ) : SlideableListItemData() {
    companion object
  }

  data class Default(
    override val onlineState: ListOnlineState,
    override val title: LocalizedString,
    override val icon: ImageId?,
    override val issues: ListItemIssues,
    override val estimatedTimerEndDate: Date? = null,
    override val infoSupported: Boolean,
    val value: String?
  ) : SlideableListItemData() {
    companion object
  }
}

fun SlideableListItemData.Thermostat.Companion.default(): SlideableListItemData.Thermostat =
  SlideableListItemData.Thermostat(
    onlineState = ListOnlineState.OFFLINE,
    title = LocalizedString.Empty,
    icon = null,
    value = "",
    subValue = "",
    indicatorIcon = null,
    issues = ListItemIssues(),
    estimatedTimerEndDate = null,
    infoSupported = false
  )

fun SlideableListItemData.Default.Companion.default(): SlideableListItemData.Default =
  SlideableListItemData.Default(
    onlineState = ListOnlineState.OFFLINE,
    title = LocalizedString.Empty,
    icon = null,
    value = "",
    issues = ListItemIssues(),
    estimatedTimerEndDate = null,
    infoSupported = false
  )
