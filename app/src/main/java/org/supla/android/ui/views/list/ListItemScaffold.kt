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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.grey
import org.supla.android.core.ui.theme.listItemCaption
import org.supla.android.extensions.differenceInSeconds
import org.supla.android.extensions.preferences
import org.supla.android.extensions.valuesFormatter
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.ui.views.Separator
import java.lang.Float.max
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@Composable
fun ListItemScaffold(
  itemTitle: String,
  itemOnline: Boolean,
  itemEstimatedEndDate: Date?,
  onInfoClick: () -> Unit,
  onIssueClick: () -> Unit,
  onTitleLongClick: () -> Unit,
  onItemClick: () -> Unit,
  itemIssueIconType: IssueIconType?,
  hasLeftButton: Boolean = false,
  hasRightButton: Boolean = false,
  scale: Float = LocalContext.current.preferences.scale,
  showInfoIcon: Boolean = LocalContext.current.preferences.isShowChannelInfo,
  content: @Composable BoxScope.() -> Unit
) {
  var title by remember { mutableStateOf(itemTitle) }
  var online by remember { mutableStateOf(itemOnline) }
  var estimatedEndDate by remember { mutableStateOf(itemEstimatedEndDate) }
  var issueIconType by remember { mutableStateOf(itemIssueIconType) }

  if (title != itemTitle) {
    title = itemTitle
  }
  if (online != itemOnline) {
    online = itemOnline
  }
  if (estimatedEndDate != itemEstimatedEndDate) {
    estimatedEndDate = itemEstimatedEndDate
  }
  if (issueIconType != itemIssueIconType) {
    issueIconType = itemIssueIconType
  }
  var itemSize by remember { mutableStateOf(IntSize.Zero) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .onSizeChanged { itemSize = it }
  ) {
    estimatedEndDate?.let {
      ListItemTimerText(date = it, scale = scale)
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
    ) {
      ListItemDotLeading(online, hasLeftButton)

      if (online && showInfoIcon) {
        ListItemInfoIcon(onInfoClick)
      }

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .weight(1f),
        content = content
      )

      issueIconType?.let { ListItemIssueIcon(it, onIssueClick) } ?: ListItemIssueIconSpacing()

      ListItemDotTrading(online, hasRightButton)
    }

    ListItemTitle(
      text = title,
      onLongClick = onTitleLongClick,
      onItemClick = onItemClick,
      scale = scale,
      modifier = Modifier.align(Alignment.BottomCenter)
    )

    Separator(modifier = Modifier.align(Alignment.BottomCenter))
  }
}

@Composable
private fun ListItemDotLeading(online: Boolean, withButton: Boolean, modifier: Modifier = Modifier) =
  ListItemDot(
    online = online,
    withButton = withButton,
    paddingValues = PaddingValues(start = Distance.default),
    modifier = modifier
  )

@Composable
private fun ListItemDotTrading(online: Boolean, withButton: Boolean, modifier: Modifier = Modifier) =
  ListItemDot(
    online = online,
    withButton = withButton,
    paddingValues = PaddingValues(end = Distance.default),
    modifier = modifier
  )

@Composable
private fun ListItemDot(online: Boolean, withButton: Boolean, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
  val color = if (online) colorResource(id = R.color.primary) else colorResource(id = R.color.red)
  val background = if (withButton) color else Color.Transparent
  Box(
    modifier = modifier
      .padding(paddingValues = paddingValues)
      .width(dimensionResource(id = R.dimen.channel_dot_size))
      .height(dimensionResource(id = R.dimen.channel_dot_size))
      .border(width = 1.dp, color = color, shape = CircleShape)
      .background(color = background, shape = CircleShape)
  )
}

@Composable
private fun ListItemInfoIcon(onClick: () -> Unit) {
  Image(
    painter = painterResource(id = R.drawable.channelstateinfo),
    contentDescription = null,
    modifier = Modifier
      .padding(start = Distance.default)
      .size(dimensionResource(id = R.dimen.channel_state_image_size))
      .clickable(interactionSource = MutableInteractionSource(), indication = null, onClick = onClick)
  )
}

@Composable
private fun ListItemIssueIcon(issueIconType: IssueIconType, onClick: () -> Unit) {
  Image(
    painter = painterResource(id = issueIconType.icon),
    contentDescription = null,
    modifier = Modifier
      .padding(end = Distance.default)
      .size(dimensionResource(id = R.dimen.channel_warning_image_size))
      .clickable(interactionSource = MutableInteractionSource(), indication = null, onClick = onClick)
  )
}

@Composable
private fun ListItemIssueIconSpacing() {
  Box(
    modifier = Modifier
      .padding(end = Distance.default)
      .size(dimensionResource(id = R.dimen.channel_warning_image_size))
  )
}

@Composable
private fun ListItemTitle(
  text: String,
  onLongClick: () -> Unit,
  onItemClick: () -> Unit,
  modifier: Modifier = Modifier,
  scale: Float = 1f
) {
  val textSize = MaterialTheme.typography.listItemCaption().fontSize.times(max(scale, 1f))
  Text(
    text = text,
    style = MaterialTheme.typography.listItemCaption(),
    modifier = modifier
      .padding(
        horizontal = Distance.default,
        vertical = Distance.small.times(scale)
      )
      .pointerInput(Unit) {
        detectTapGestures(onLongPress = { onLongClick() }, onTap = { onItemClick() })
      },
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    fontSize = textSize
  )
}

context (BoxScope)
@Composable
private fun ListItemTimerText(date: Date, scale: Float) {
  var text by remember { mutableStateOf<String?>(null) }
  val context = LocalContext.current

  LaunchedEffect(date) {
    var currentDate = Date()
    if (date.after(currentDate)) {
      do {
        val leftTimeSeconds = date.differenceInSeconds(currentDate)
        text = context.valuesFormatter.getTimerRestTime(leftTimeSeconds)(context)
        delay(1.seconds)
        currentDate = Date()
      } while (currentDate.before(date))
    }
    text = null
  }

  text?.let {
    val fontSize = MaterialTheme.typography.body2.fontSize
    Text(
      text = it,
      style = MaterialTheme.typography.body2.copy(fontSize = if (scale < 1) fontSize.times(0.8f) else fontSize),
      color = MaterialTheme.colors.grey,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = Distance.small.times(scale), end = Distance.small)
    )
  }
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
        ListItemScaffold(
          itemTitle = "Power Switch",
          itemOnline = true,
          null,
          { },
          { },
          { },
          { },
          IssueIconType.WARNING,
          scale = 1f,
          showInfoIcon = true
        ) {
        }
      }
      Box(
        modifier = Modifier
          .width(500.dp)
          .height(100.dp)
      ) {
        ListItemScaffold(itemTitle = "Power Switch", itemOnline = false, null, { }, { }, { }, { }, null, scale = 1f, showInfoIcon = false) {
        }
      }
    }
  }
}
