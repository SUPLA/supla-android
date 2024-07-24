package org.supla.android.features.details.thermostatdetail.general.ui
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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.gray
import org.supla.android.features.details.thermostatdetail.general.data.ThermostatProgramInfo

@Composable
fun ProgramInfoRow(infos: List<ThermostatProgramInfo>) {
  Column(
    modifier = Modifier
      .height(80.dp)
      .padding(
        start = dimensionResource(id = R.dimen.distance_default),
        top = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default)
      ),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    infos.forEach {
      Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny))) {
        ProgramInfoLabel(stringResource(id = it.type.stringRes))
        if (it.icon != null && it.iconColor != null) {
          ProgramInfoIcon(it.icon, it.iconColor)
        }
        it.descriptionProvider?.let {
          ProgramInfoDescription(it(LocalContext.current))
        }
        it.time?.let {
          Text(
            text = it(LocalContext.current),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        if (it.manualActive) {
          ProgramInfoManual()
        }
      }
    }
  }
}

@Composable
private fun ProgramInfoLabel(label: String) =
  Text(
    modifier = Modifier.defaultMinSize(minWidth = 80.dp),
    text = label.uppercase(),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.gray
  )

@Composable
private fun ProgramInfoIcon(@DrawableRes icon: Int, @ColorRes color: Int) =
  Image(
    painter = painterResource(id = icon),
    contentDescription = null,
    colorFilter = ColorFilter.tint(color = colorResource(id = color)),
    modifier = Modifier.size(19.dp),
    contentScale = ContentScale.Fit
  )

@Composable
private fun ProgramInfoDescription(description: String) =
  Text(
    text = description,
    style = MaterialTheme.typography.bodyMedium,
    fontWeight = FontWeight.SemiBold,
    color = MaterialTheme.colorScheme.onBackground
  )

@Composable
private fun ProgramInfoManual() =
  Image(
    painter = painterResource(id = R.drawable.ic_manual),
    contentDescription = null,
    colorFilter = ColorFilter.tint(color = colorResource(id = R.color.primary)),
    modifier = Modifier.size(19.dp),
    contentScale = ContentScale.Fit
  )
