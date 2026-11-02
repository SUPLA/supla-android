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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.storage.LocalApplicationPreferences
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.views.list.ListItemScaffold
import org.supla.android.ui.views.list.ListItemStatus
import org.supla.android.ui.views.list.StatusIndicator
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemMainRow
import org.supla.core.shared.data.model.lists.ListItemIssues

@Composable
fun SceneListItemView(
  data: ListItem.SceneItem,
  scale: Float = LocalApplicationPreferences.current.scale,
  onInfoClick: () -> Unit = { },
  onIssueClick: (ListItemIssues) -> Unit = { },
  onItemClick: () -> Unit = { },
  onTitleLongClick: () -> Unit = { }
) {
  ListItemScaffold(
    itemTitle = data.userCaption,
    itemEstimatedEndDate = data.estimatedTimerEndDate,
    onInfoClick = onInfoClick,
    onIssueClick = onIssueClick,
    onTitleLongClick = onTitleLongClick,
    onItemClick = onItemClick,
    issues = ListItemIssues.empty,
    scale = scale,
    showInfoIcon = false,
    statusIndicator = StatusIndicator(
      listItemStatus = data.status,
      hasLeftButton = false,
      hasRightButton = false
    )
  ) {
    ListItemMainRow(scale = scale) {
      ListItemIcon(imageId = data.icon, scale = scale)
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(0.75f))
      ) {
        SceneListItemView(
          data = ListItem.SceneItem(
            remoteId = 1,
            profileId = 1L,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(onlineState = ListOnlineState.ONLINE),
            userCaption = "Scene",
            icon = ImageId(R.drawable.fnc_gpm_5),
            estimatedTimerEndDate = null
          ),
          scale = 0.75f,
        )
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = R.dimen.channel_layout_height))
      ) {
        SceneListItemView(
          data = ListItem.SceneItem(
            remoteId = 1,
            profileId = 1L,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(onlineState = ListOnlineState.ONLINE),
            userCaption = "Scene",
            icon = ImageId(R.drawable.fnc_gpm_5),
            estimatedTimerEndDate = null
          ),
          scale = 1f,
        )
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = R.dimen.channel_layout_height))
      ) {
        SceneListItemView(
          data = ListItem.SceneItem(
            remoteId = 1,
            profileId = 1L,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(onlineState = ListOnlineState.ONLINE),
            userCaption = "Scene",
            icon = ImageId(R.drawable.fnc_gpm_5),
            estimatedTimerEndDate = null
          ),
          scale = 1f,
        )
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(1.5f))
      ) {
        SceneListItemView(
          data = ListItem.SceneItem(
            remoteId = 1,
            profileId = 1L,
            locationCaption = "",
            locationId = 1,
            status = ListItemStatus.Channel(onlineState = ListOnlineState.ONLINE),
            userCaption = "Scene",
            icon = ImageId(R.drawable.fnc_gpm_5),
            estimatedTimerEndDate = null
          ),
          scale = 1.5f,
        )
      }
    }
  }
}
