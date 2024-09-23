package org.supla.android.ui.views.list.components
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import org.supla.android.R
import org.supla.android.ui.lists.data.IssueIconType

@Composable
fun ListItemIssueIcon(issueIconType: IssueIconType, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Image(
    painter = painterResource(id = issueIconType.icon),
    contentDescription = null,
    modifier = modifier
      .size(dimensionResource(id = R.dimen.channel_warning_image_size))
      .clickable(interactionSource = remember { MutableInteractionSource() }, indication = ripple(), onClick = onClick)
  )
}
