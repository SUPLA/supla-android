package org.supla.android.features.details.actiontrigger.list
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import org.supla.android.R
import org.supla.android.core.ui.ViewState
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.sensordata.RelatedChannelData
import org.supla.android.ui.lists.sensordata.RelatedChannelsView
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.icons.LockIcon
import org.supla.android.ui.views.texts.BodyMedium
import org.supla.core.shared.data.model.function.actiontrigger.ActionTriggerValue
import org.supla.core.shared.infrastructure.LocalizedString

data class ActionTriggerListViewState(
  val relatedChannels: List<RelatedChannelData> = emptyList(),
  val scale: Float = 1f
) : ViewState()

interface ActionTriggerListScope {
  fun onItemClick(data: RelatedChannelData)
  fun onItemCaptionLongPress(data: RelatedChannelData)
  fun onItemInfoClick(data: RelatedChannelData)
}

private val previewScope = object : ActionTriggerListScope {
  override fun onItemClick(data: RelatedChannelData) {}
  override fun onItemCaptionLongPress(data: RelatedChannelData) {}
  override fun onItemInfoClick(data: RelatedChannelData) {}
}

@Composable
fun ActionTriggerListScope.View(
  state: ActionTriggerListViewState,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    BodyMedium(
      stringRes = R.string.action_trigger_list_related_title,
      modifier = Modifier.padding(start = Distance.default, top = Distance.default, end = Distance.default),
      textAlign = TextAlign.Start
    )

    RelatedChannelsView(
      channels = state.relatedChannels,
      scale = state.scale,
      onClick = ::onItemClick,
      onInfoClick = ::onItemInfoClick,
      onCaptionLongPress = ::onItemCaptionLongPress
    )
  }
}

@Composable
fun ActionTriggerIcons(
  weeklyActive: Boolean,
  indicatorType: ActionTriggerValue.IndicatorType,
  stateIconVisible: Boolean
) {
  Row {
    ActionTriggerIndicatorIcon(
      type = indicatorType,
      size = dimensionResource(R.dimen.icon_default_size),
      modifier = Modifier.padding(end = Distance.tiny)
    )
    weeklyActive.ifTrue {
      Image(
        drawableId = R.drawable.ic_schedule,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(end = Distance.tiny)
      )
    }

    if (!stateIconVisible && (indicatorType != ActionTriggerValue.IndicatorType.NONE || weeklyActive)) {
      Spacer(modifier = Modifier.width(Distance.tiny))
    }
  }
}

@Composable
fun ActionTriggerIndicatorIcon(
  type: ActionTriggerValue.IndicatorType,
  modifier: Modifier = Modifier,
  size: Dp = dimensionResource(R.dimen.icon_small_size)
) {
  when (type) {
    ActionTriggerValue.IndicatorType.NONE -> Unit
    ActionTriggerValue.IndicatorType.LOCKED ->
      LockIcon(
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
        size = size
      )
    ActionTriggerValue.IndicatorType.TEMPORARY_UNLOCKED ->
      Image(
        drawableId = R.drawable.ic_lock_temporary_open,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.size(size)
      )
  }
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      state = ActionTriggerListViewState(
        relatedChannels = listOf(
          RelatedChannelData.Visible(
            channelId = 1,
            profileId = 1L,
            onlineState = ListOnlineState.ONLINE,
            icon = ImageId(R.drawable.fnc_action_trigger),
            caption = LocalizedString.Constant("Action trigger 1"),
            userCaption = "",
            iconsBlock = { ActionTriggerIcons(true, ActionTriggerValue.IndicatorType.LOCKED, true) },
            showChannelStateIcon = true
          ),
          RelatedChannelData.Visible(
            channelId = 2,
            profileId = 1L,
            onlineState = ListOnlineState.OFFLINE,
            icon = ImageId(R.drawable.fnc_action_trigger),
            caption = LocalizedString.Constant("Action trigger 2"),
            userCaption = "",
            iconsBlock = { ActionTriggerIcons(false, ActionTriggerValue.IndicatorType.TEMPORARY_UNLOCKED, false) },
            showChannelStateIcon = false
          )
        )
      )
    )
  }
}
