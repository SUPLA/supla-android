package org.supla.android.ui.views.list.listitem
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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.isNotNull
import org.supla.android.extensions.preferences
import org.supla.android.features.details.actiontrigger.list.ActionTriggerIndicatorIcon
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.views.list.ListItemScaffold
import org.supla.android.ui.views.list.ListItemStatus
import org.supla.android.ui.views.list.StatusIndicator
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemMainRow
import org.supla.core.shared.data.model.function.actiontrigger.ActionTriggerValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString

@Composable
fun ActionTriggerListItemView(
  data: ListItem.ActionTriggerItem,
  showInfoIcon: Boolean = LocalContext.current.preferences.isShowChannelInfo,
  scale: Float = LocalContext.current.preferences.scale,
  onInfoClick: () -> Unit = { },
  onIssueClick: (ListItemIssues) -> Unit = { },
  onItemClick: () -> Unit = { },
  onTitleLongClick: () -> Unit = { }
) {
  ListItemScaffold(
    itemTitle = data.captionProvider(),
    itemEstimatedEndDate = data.estimatedTimerEndDate,
    onInfoClick = onInfoClick,
    onTitleLongClick = onTitleLongClick,
    showInfoIcon = showInfoIcon && data.infoSupported,
    issues = data.issues,
    onIssueClick = onIssueClick,
    onItemClick = onItemClick,
    scale = scale,
    statusIndicator = StatusIndicator(
      listItemStatus = data.status,
      hasLeftButton = data.leftButtonString.isNotNull,
      hasRightButton = data.rightButtonString.isNotNull
    )
  ) {
    ListItemMainRow(scale = scale) {
      val indicatoSize = dimensionResource(R.dimen.icon_default_size).times(scale)
      Box(modifier = Modifier.width(dimensionResource(id = R.dimen.channel_img_width).times(scale) + indicatoSize)) {
        ListItemIcon(imageId = data.icon, scale = scale, modifier = Modifier.align(Alignment.Center))
        ActionTriggerIndicatorIcon(
          type = data.indicatorType,
          modifier = Modifier.align(Alignment.BottomEnd),
          size = indicatoSize
        )
      }
    }
  }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(1.5f))
      ) {
        ActionTriggerListItemView(
          data = ListItem.ActionTriggerItem(
            remoteId = 1,
            profileId = 1L,
            function = SuplaFunction.ACTION_TRIGGER,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(ListOnlineState.ONLINE),
            captionProvider = LocalizedString.Constant("Button"),
            userCaption = "",
            icon = ImageId(R.drawable.fnc_action_trigger),
            issues = ListItemIssues(IssueIcon.Warning),
            estimatedTimerEndDate = null,
            infoSupported = true,
            indicatorType = ActionTriggerValue.IndicatorType.LOCKED
          ),
          showInfoIcon = true,
          scale = 1.5f
        )
      }
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height))
      ) {
        ActionTriggerListItemView(
          data = ListItem.ActionTriggerItem(
            remoteId = 1,
            profileId = 1L,
            function = SuplaFunction.ACTION_TRIGGER,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(ListOnlineState.PARTIALLY_ONLINE),
            captionProvider = LocalizedString.Constant("Button"),
            userCaption = "",
            icon = ImageId(R.drawable.fnc_action_trigger),
            issues = ListItemIssues(IssueIcon.Error),
            estimatedTimerEndDate = null,
            infoSupported = true,
            indicatorType = ActionTriggerValue.IndicatorType.TEMPORARY_UNLOCKED
          ),
          showInfoIcon = true,
          scale = 1.0f
        )
      }
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(0.6f))
      ) {
        ActionTriggerListItemView(
          data = ListItem.ActionTriggerItem(
            remoteId = 1,
            profileId = 1L,
            function = SuplaFunction.ACTION_TRIGGER,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(ListOnlineState.ONLINE),
            captionProvider = LocalizedString.Constant("Button"),
            userCaption = "",
            icon = ImageId(R.drawable.fnc_action_trigger),
            issues = ListItemIssues.empty,
            estimatedTimerEndDate = null,
            infoSupported = true,
            indicatorType = ActionTriggerValue.IndicatorType.LOCKED
          ),
          showInfoIcon = true,
          scale = 0.6f
        )
      }
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(0.6f))
      ) {
        ActionTriggerListItemView(
          data = ListItem.ActionTriggerItem(
            remoteId = 1,
            profileId = 1L,
            function = SuplaFunction.HVAC_THERMOSTAT,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(ListOnlineState.ONLINE),
            captionProvider = LocalizedString.Constant("Thermostat with very long name which goes out of the screen and must be cut"),
            userCaption = "",
            icon = ImageId(R.drawable.fnc_thermostat_cool),
            issues = ListItemIssues(IssueIcon.Warning),
            estimatedTimerEndDate = null,
            infoSupported = true,
            indicatorType = ActionTriggerValue.IndicatorType.LOCKED
          ),
          showInfoIcon = true,
          scale = 0.6f
        )
      }
    }
  }
}

@Preview(device = Devices.NEXUS_5)
@Preview(device = Devices.NEXUS_5, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_Narrow() {
  SuplaTheme {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .width(350.dp)
    ) {
      Column(
        modifier = Modifier
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(1.5f))
      ) {
        ActionTriggerListItemView(
          data = ListItem.ActionTriggerItem(
            remoteId = 1,
            profileId = 1L,
            function = SuplaFunction.ACTION_TRIGGER,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(ListOnlineState.ONLINE),
            captionProvider = LocalizedString.Constant("Button"),
            userCaption = "",
            icon = ImageId(R.drawable.fnc_action_trigger),
            issues = ListItemIssues(IssueIcon.Warning),
            estimatedTimerEndDate = null,
            infoSupported = true,
            indicatorType = ActionTriggerValue.IndicatorType.TEMPORARY_UNLOCKED
          ),
          showInfoIcon = true,
          scale = 1.5f
        )
      }

      Column(
        modifier = Modifier
          .height(dimensionResource(id = R.dimen.channel_layout_height))
      ) {
        ActionTriggerListItemView(
          data = ListItem.ActionTriggerItem(
            remoteId = 1,
            profileId = 1L,
            function = SuplaFunction.ACTION_TRIGGER,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(ListOnlineState.ONLINE),
            captionProvider = LocalizedString.Constant("Action trigger"),
            userCaption = "",
            icon = ImageId(R.drawable.fnc_action_trigger),
            issues = ListItemIssues(IssueIcon.Error),
            estimatedTimerEndDate = null,
            infoSupported = true,
            indicatorType = ActionTriggerValue.IndicatorType.TEMPORARY_UNLOCKED
          ),
          showInfoIcon = true,
          scale = 1.0f
        )
      }
    }
  }
}
