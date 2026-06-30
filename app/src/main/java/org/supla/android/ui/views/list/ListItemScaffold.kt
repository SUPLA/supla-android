package org.supla.android.ui.views.list

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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.gray
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.differenceInSeconds
import org.supla.android.extensions.preferences
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.extensions.ifFalse
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.list.components.ListItemInfoIcon
import org.supla.android.ui.views.list.components.ListItemIssueIcon
import org.supla.android.ui.views.list.components.ListItemMainRow
import org.supla.android.ui.views.list.components.ListItemTitle
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.data.model.lists.ListItemIssues
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@Composable
fun ListItemScaffold(
  itemTitle: String,
  itemEstimatedEndDate: Date?,
  onInfoClick: () -> Unit,
  onIssueClick: (ListItemIssues) -> Unit,
  onTitleLongClick: () -> Unit,
  onItemClick: () -> Unit,
  issues: ListItemIssues,
  statusIndicator: StatusIndicator,
  scale: Float = LocalContext.current.preferences.scale,
  showInfoIcon: Boolean = false,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .clickable(onClick = onItemClick)
  ) {
    itemEstimatedEndDate?.let {
      ListItemTimerText(date = it, scale = scale)
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
    ) {
      statusIndicator.View(StatusSide.START)

      statusIndicator.isGroup.ifFalse {
        if (showInfoIcon) {
          ListItemInfoIcon(onInfoClick, modifier = Modifier.padding(start = dimensionResource(id = R.dimen.list_horizontal_spacing)))
        } else {
          ListItemIssueIconSpacing()
        }
      }

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .weight(1f),
        content = content
      )

      statusIndicator.isGroup.ifFalse {
        if (issues.isEmpty()) {
          ListItemIssueIconSpacing()
        } else {
          ListItemIssuesView(
            issues = issues,
            onClick = onIssueClick
          )
        }
      }

      statusIndicator.View(StatusSide.END)
    }

    ListItemTitle(
      text = itemTitle,
      onLongClick = onTitleLongClick,
      onItemClick = onItemClick,
      scale = scale,
      modifier = Modifier
        .padding(horizontal = Distance.default, vertical = Distance.small.times(scale).div(LocalDensity.current.fontScale))
        .align(Alignment.BottomCenter)
    )

    Separator(modifier = Modifier.align(Alignment.BottomCenter))
  }
}

@Composable
private fun ListItemIssueIconSpacing() {
  Box(
    modifier = Modifier
      .padding(end = dimensionResource(id = R.dimen.list_horizontal_spacing))
      .size(dimensionResource(id = R.dimen.channel_warning_image_size))
  )
}

@Composable
private fun BoxScope.ListItemTimerText(date: Date, scale: Float) {
  var text by remember { mutableStateOf<String?>(null) }
  val context = LocalContext.current

  LaunchedEffect(date) {
    var currentDate = Date()
    if (date.after(currentDate)) {
      do {
        val leftTimeSeconds = date.differenceInSeconds(currentDate)
        text = ValuesFormatter.getTimerRestTime(leftTimeSeconds)(context)
        delay(1.seconds)
        currentDate = Date()
      } while (currentDate.before(date))
    }
    text = null
  }

  text?.let {
    val fontSize = MaterialTheme.typography.bodyMedium.fontSize
    Text(
      text = it,
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (scale < 1) fontSize.times(0.8f) else fontSize),
      color = MaterialTheme.colorScheme.gray,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = Distance.small.times(scale), end = Distance.small)
    )
  }
}

@Composable
private fun ListItemIssuesView(
  issues: ListItemIssues,
  onClick: (ListItemIssues) -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .padding(end = dimensionResource(id = R.dimen.list_horizontal_spacing))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(),
        onClick = { onClick(issues) },
        enabled = issues.hasMessage()
      )
  ) {
    issues.icons.forEach {
      ListItemIssueIcon(it)
    }
  }
}

@Composable
@SuplaPreview
private fun Preview() {
  SuplaTheme {
    Column(
      modifier = Modifier.background(MaterialTheme.colorScheme.surface),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .width(500.dp)
          .height(100.dp)
      ) {
        ListItemScaffold(
          itemTitle = "Power Switch",
          null,
          { },
          { },
          { },
          { },
          ListItemIssues(IssueIcon.Warning),
          scale = 1f,
          showInfoIcon = true,
          statusIndicator = StatusIndicator(
            listItemStatus = ListItemStatus.Channel(ListOnlineState.ONLINE),
            hasLeftButton = false,
            hasRightButton = false
          )
        ) {
          ListItemMainRow(scale = 0.75f) {
            Image(
              painter = painterResource(id = R.drawable.fnc_curtain_close),
              contentDescription = null,
              alignment = Alignment.Center,
              modifier = Modifier
                .width(dimensionResource(id = R.dimen.channel_img_width))
                .height(dimensionResource(id = R.dimen.channel_img_height)),
              contentScale = ContentScale.Fit
            )
          }
        }
      }
      Box(
        modifier = Modifier
          .width(500.dp)
          .height(100.dp)
      ) {
        ListItemScaffold(
          itemTitle = "Power Switch",
          Date(),
          {},
          { },
          { },
          { },
          ListItemIssues(),
          scale = 1f,
          showInfoIcon = false,
          statusIndicator = StatusIndicator(
            listItemStatus = ListItemStatus.Channel(ListOnlineState.OFFLINE),
            hasLeftButton = false,
            hasRightButton = false
          )
        ) {
        }
      }
      Box(
        modifier = Modifier
          .width(500.dp)
          .height(100.dp)
      ) {
        ListItemScaffold(
          itemTitle = "Power Switch",
          Date(),
          {},
          { },
          { },
          { },
          ListItemIssues(),
          scale = 1f,
          showInfoIcon = false,
          statusIndicator = StatusIndicator(
            listItemStatus = ListItemStatus.Channel(ListOnlineState.PARTIALLY_ONLINE),
            hasLeftButton = true,
            hasRightButton = true
          )
        ) {
        }
      }
      Box(
        modifier = Modifier
          .width(500.dp)
          .height(100.dp)
      ) {
        ListItemScaffold(
          itemTitle = "Power Switch",
          Date(),
          {},
          { },
          { },
          { },
          ListItemIssues(),
          scale = 1f,
          showInfoIcon = false,
          statusIndicator = StatusIndicator(
            listItemStatus = ListItemStatus.Group(
              onlinePercentage = 0f,
              activePercentage = 0f
            ),
            hasLeftButton = true,
            hasRightButton = true
          )
        ) {
        }
      }
      Box(
        modifier = Modifier
          .width(500.dp)
          .height(100.dp)
      ) {
        ListItemScaffold(
          itemTitle = "Power Switch",
          Date(),
          {},
          { },
          { },
          { },
          ListItemIssues(),
          scale = 1f,
          showInfoIcon = false,
          statusIndicator = StatusIndicator(
            listItemStatus = ListItemStatus.Group(
              onlinePercentage = 0.75f,
              activePercentage = 0.25f
            ),
            hasLeftButton = true,
            hasRightButton = true
          )
        ) {
        }
      }
    }
  }
}
