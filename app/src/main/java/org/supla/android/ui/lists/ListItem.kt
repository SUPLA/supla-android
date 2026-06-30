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
import org.supla.android.R
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.images.ImageId
import org.supla.android.ui.views.list.ListItemStatus
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import java.util.Date

sealed interface ListItem {
  val remoteId: Int
  val profileId: Long
  val userCaption: String
  val key: String
  val draggable: Boolean

  open class DefaultItem(
    override val remoteId: Int,
    override val profileId: Long,
    override val userCaption: String,
    val function: SuplaFunction,
    val locationCaption: String,
    val locationId: Int,
    val status: ListItemStatus,
    val captionProvider: LocalizedString,
    val icon: ImageId,
    val value: String?,
    val issues: ListItemIssues,
    val processing: Boolean = false,
    val estimatedTimerEndDate: Date? = null,
    val infoSupported: Boolean = false,
    val leftButtonString: LocalizedString? = null,
    val rightButtonString: LocalizedString? = null
  ) : ListItem {
    override val key: String = "C$remoteId"
    override val draggable: Boolean = true
  }

  open class GroupItem(
    remoteId: Int,
    profileId: Long,
    function: SuplaFunction,
    locationCaption: String,
    locationId: Int,
    status: ListItemStatus,
    captionProvider: LocalizedString,
    userCaption: String,
    icon: ImageId,
    value: String? = null,
    issues: ListItemIssues = ListItemIssues.empty,
    processing: Boolean = false,
    estimatedTimerEndDate: Date? = null,
    leftButtonString: LocalizedString? = null,
    rightButtonString: LocalizedString? = null
  ) : DefaultItem(
    remoteId = remoteId,
    profileId = profileId,
    userCaption = userCaption,
    function = function,
    locationCaption = locationCaption,
    locationId = locationId,
    status = status,
    captionProvider = captionProvider,
    icon = icon,
    value = value,
    issues = issues,
    processing = processing,
    estimatedTimerEndDate = estimatedTimerEndDate,
    infoSupported = false,
    leftButtonString = leftButtonString,
    rightButtonString = rightButtonString

  ) {
    override val key: String = "G$remoteId"
    override val draggable: Boolean = true
  }

  data class SceneItem(
    override val remoteId: Int,
    override val profileId: Long,
    override val userCaption: String,
    val locationCaption: String,
    val locationId: Int,
    val status: ListItemStatus,
    val icon: ImageId,
    val estimatedTimerEndDate: Date?
  ) : ListItem {
    override val key: String = "S$remoteId"
    override val draggable: Boolean = true
  }

  data class LocationItem(
    override val remoteId: Int,
    override val profileId: Long,
    override val userCaption: String,
    val collapsed: Boolean
  ) : ListItem {
    override val key: String = "L$remoteId"
    override val draggable: Boolean = false
  }

  class HvacThermostatItem(
    remoteId: Int,
    profileId: Long,
    function: SuplaFunction,
    locationCaption: String,
    locationId: Int,
    status: ListItemStatus,
    captionProvider: LocalizedString,
    userCaption: String,
    icon: ImageId,
    value: String?,
    issues: ListItemIssues,
    processing: Boolean = false,
    estimatedTimerEndDate: Date? = null,
    infoSupported: Boolean = false,
    val subValue: String,
    @param:DrawableRes val indicatorIcon: Int?
  ) : DefaultItem(
    remoteId = remoteId,
    profileId = profileId,
    function = function,
    locationCaption = locationCaption,
    locationId = locationId,
    status = status,
    captionProvider = captionProvider,
    userCaption = userCaption,
    icon = icon,
    value = value,
    issues = issues,
    processing = processing,
    estimatedTimerEndDate = estimatedTimerEndDate,
    infoSupported = infoSupported,
    leftButtonString = localizedString(R.string.channel_btn_off),
    rightButtonString = localizedString(R.string.channel_btn_on)
  )

  class HeatpolThermostatItem(
    remoteId: Int,
    profileId: Long,
    locationCaption: String,
    locationId: Int,
    status: ListItemStatus,
    captionProvider: LocalizedString,
    userCaption: String,
    icon: ImageId,
    value: String?,
    issues: ListItemIssues,
    processing: Boolean = false,
    estimatedTimerEndDate: Date? = null,
    infoSupported: Boolean = false,
    val subValue: String,
  ) : DefaultItem(
    remoteId = remoteId,
    profileId = profileId,
    function = SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
    locationCaption = locationCaption,
    locationId = locationId,
    status = status,
    captionProvider = captionProvider,
    userCaption = userCaption,
    icon = icon,
    value = value,
    issues = issues,
    processing = processing,
    estimatedTimerEndDate = estimatedTimerEndDate,
    infoSupported = infoSupported,
    leftButtonString = localizedString(R.string.channel_btn_off),
    rightButtonString = localizedString(R.string.channel_btn_on)
  )

  class DoubleValueItem(
    remoteId: Int,
    profileId: Long,
    function: SuplaFunction,
    locationCaption: String,
    locationId: Int,
    status: ListItemStatus,
    captionProvider: LocalizedString,
    userCaption: String,
    icon: ImageId,
    value: String?,
    issues: ListItemIssues,
    processing: Boolean = false,
    estimatedTimerEndDate: Date? = null,
    infoSupported: Boolean = false,
    leftButtonString: LocalizedString? = null,
    rightButtonString: LocalizedString? = null,
    val secondIcon: ImageId?,
    val secondValue: String?
  ) : DefaultItem(
    remoteId = remoteId,
    profileId = profileId,
    function = function,
    locationCaption = locationCaption,
    locationId = locationId,
    status = status,
    captionProvider = captionProvider,
    userCaption = userCaption,
    icon = icon,
    value = value,
    issues = issues,
    processing = processing,
    estimatedTimerEndDate = estimatedTimerEndDate,
    infoSupported = infoSupported,
    leftButtonString = leftButtonString,
    rightButtonString = rightButtonString
  )
}

fun LocationEntity.locationItem(collapsedFlag: CollapsedFlag): ListItem.LocationItem =
  ListItem.LocationItem(
    remoteId = remoteId,
    profileId = profileId,
    userCaption = caption,
    collapsed = isCollapsed(collapsedFlag)
  )

fun SceneDataEntity.sceneItem(getSceneIconUseCase: GetSceneIconUseCase): ListItem.SceneItem =
  ListItem.SceneItem(
    remoteId = remoteId,
    profileId = sceneEntity.profileId?.toLongOrNull() ?: 0,
    locationCaption = locationEntity.caption,
    locationId = locationEntity.remoteId,
    userCaption = sceneEntity.caption,
    status = ListItemStatus.Scene,
    icon = getSceneIconUseCase(sceneEntity),
    estimatedTimerEndDate = sceneEntity.estimatedEndDate
  )
