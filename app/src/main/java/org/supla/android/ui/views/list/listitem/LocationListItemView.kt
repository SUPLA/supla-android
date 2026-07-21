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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.main.topbar.LocalTopBarController
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.texts.HighlightedTextBySearch

@Composable
fun LocationListItemView(
  caption: String,
  collapsed: Boolean,
  inSearch: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
  onLongClick: () -> Unit = {}
) {
  Column {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .heightIn(min = dimensionResource(R.dimen.channel_section_height))
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .combinedClickable(
          enabled = !inSearch,
          onClick = onClick,
          onLongClick = onLongClick
        )
        .padding(horizontal = Distance.default),
      verticalAlignment = Alignment.CenterVertically
    ) {
      HighlightedTextBySearch(
        text = caption,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineSmall,
        maxLines = 1,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Start
      )

      if (!inSearch) {
        Icon(
          painter = painterResource(id = R.drawable.ic_arrow_right),
          contentDescription = null,
          modifier = Modifier.rotate(if (collapsed) 90f else 270f),
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

@Composable
@SuplaPreview
private fun Preview() {
  SuplaTheme {
    Column {
      LocationListItemView(caption = "Leaving Room", collapsed = true, inSearch = false)
      LocationListItemView(caption = "Sleeping Room", collapsed = false, inSearch = false)
      LocationListItemView(caption = "Sleeping Room", collapsed = false, inSearch = true)
    }
  }
}
