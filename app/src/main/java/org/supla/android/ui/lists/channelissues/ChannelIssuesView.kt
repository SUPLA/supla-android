package org.supla.android.ui.lists.channelissues
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import org.supla.android.R
import org.supla.android.core.shared.data.model.lists.resource
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.ui.views.Image
import org.supla.core.shared.data.model.lists.ChannelIssueItem

@Composable
fun ChannelIssuesView(issues: List<ChannelIssueItem>, modifier: Modifier = Modifier) {
  issues.forEach {
    it.messages.forEach { message ->
      Row(
        modifier = modifier
          .fillMaxWidth()
          .padding(horizontal = Distance.default),
        horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
      ) {
        Image(drawableId = it.icon.resource, modifier = Modifier.size(dimensionResource(R.dimen.icon_default_size)))
        Text(
          text = message.invoke(LocalContext.current),
          textAlign = TextAlign.Justify,
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }
}
