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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.listItemCaption
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.ui.views.Separator
import java.lang.Float.max

@Composable
fun ListItemScaffold(
  itemTitle: String,
  online: Boolean,
  onInfoClick: () -> Unit,
  onIssueClick: () -> Unit,
  onTitleLongClick: () -> Unit,
  onItemClick: () -> Unit,
  showInfoIcon: Boolean,
  issueIconType: IssueIconType?,
  hasLeftButton: Boolean = false,
  hasRightButton: Boolean = false,
  scale: Float = 1f,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
  ) {
    if (online && showInfoIcon) {
      ListItemInfoIcon(onInfoClick)
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ListItemDotLeading(online, hasLeftButton)
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        content = content
      )
      ListItemDotTrading(online, hasRightButton)
    }
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      ListItemTitle(text = itemTitle, onLongClick = onTitleLongClick, onItemClick = onItemClick, scale = scale)
      Separator()
    }

    issueIconType?.let { ListItemIssueIcon(it, onIssueClick) }
  }
}

@Composable
private fun ListItemDotLeading(online: Boolean, withButton: Boolean) =
  ListItemDot(
    online = online,
    withButton = withButton,
    paddingValues = PaddingValues(start = dimensionResource(id = R.dimen.distance_default))
  )

@Composable
private fun ListItemDotTrading(online: Boolean, withButton: Boolean) =
  ListItemDot(
    online = online,
    withButton = withButton,
    paddingValues = PaddingValues(end = dimensionResource(id = R.dimen.distance_default))
  )

@Composable
private fun ListItemDot(online: Boolean, withButton: Boolean, paddingValues: PaddingValues) {
  val color = if (online) colorResource(id = R.color.primary) else colorResource(id = R.color.red)
  val background = if (withButton) color else Color.Transparent
  Box(
    modifier = Modifier
      .padding(paddingValues = paddingValues)
      .width(dimensionResource(id = R.dimen.channel_dot_size))
      .height(dimensionResource(id = R.dimen.channel_dot_size))
      .border(width = 1.dp, color = color, shape = CircleShape)
      .background(color = background, shape = CircleShape)
  )
}

context (BoxScope)
@Composable
private fun ListItemInfoIcon(onClick: () -> Unit) {
  val startPadding = dimensionResource(id = R.dimen.channel_dot_size)
    .plus(dimensionResource(id = R.dimen.distance_default).times(2))

  Image(
    painter = painterResource(id = R.drawable.channelstateinfo),
    contentDescription = null,
    modifier = Modifier
      .align(Alignment.CenterStart)
      .padding(start = startPadding)
      .size(dimensionResource(id = R.dimen.channel_state_image_size))
      .clickable(interactionSource = MutableInteractionSource(), indication = null, onClick = onClick)
  )
}

context (BoxScope)
@Composable
private fun ListItemIssueIcon(issueIconType: IssueIconType, onClick: () -> Unit) {
  val endPadding = dimensionResource(id = R.dimen.channel_dot_size)
    .plus(dimensionResource(id = R.dimen.distance_default).times(2))

  Image(
    painter = painterResource(id = issueIconType.icon),
    contentDescription = null,
    modifier = Modifier
      .align(Alignment.CenterEnd)
      .padding(end = endPadding)
      .size(dimensionResource(id = R.dimen.channel_warning_image_size))
      .clickable(interactionSource = MutableInteractionSource(), indication = null, onClick = onClick)
  )
}

@Composable
private fun ListItemTitle(text: String, onLongClick: () -> Unit, onItemClick: () -> Unit, scale: Float = 1f) {
  val textSize = MaterialTheme.typography.listItemCaption().fontSize.times(max(scale, 1f))
  Text(
    text = text,
    style = MaterialTheme.typography.listItemCaption(),
    modifier = Modifier
      .padding(
        horizontal = dimensionResource(id = R.dimen.distance_default),
        vertical = dimensionResource(id = R.dimen.distance_small).times(scale)
      )
      .pointerInput(Unit) {
        detectTapGestures(onLongPress = { onLongClick() }, onTap = { onItemClick() })
      },
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    fontSize = textSize
  )
}

@Composable
@Preview
private fun Preview() {
  SuplaTheme {
    Column(
      modifier = Modifier.background(MaterialTheme.colors.surface),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .width(500.dp)
          .height(100.dp)
      ) {
        ListItemScaffold(itemTitle = "Power Switch", online = true, { }, { }, { }, { }, true, IssueIconType.WARNING) {
        }
      }
      Box(
        modifier = Modifier
          .width(500.dp)
          .height(100.dp)
      ) {
        ListItemScaffold(itemTitle = "Power Switch", online = false, { }, { }, { }, { }, false, null) {
        }
      }
    }
  }
}
