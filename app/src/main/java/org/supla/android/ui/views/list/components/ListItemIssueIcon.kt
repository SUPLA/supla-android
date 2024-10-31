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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import org.supla.android.core.shared.data.model.lists.height
import org.supla.android.core.shared.data.model.lists.resource
import org.supla.android.core.shared.data.model.lists.rotation
import org.supla.android.core.shared.data.model.lists.width
import org.supla.core.shared.data.model.lists.IssueIcon

@Composable
fun ListItemIssueIcon(icon: IssueIcon, modifier: Modifier = Modifier) {
  Image(
    painter = painterResource(id = icon.resource),
    contentDescription = null,
    modifier = modifier
      .size(width = dimensionResource(icon.width), height = dimensionResource(icon.height))
      .rotate(icon.rotation)
  )
}
