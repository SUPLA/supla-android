package org.supla.android.features.addwizard.view.components
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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance

@Composable
fun AddWizardActionButton(
  modifier: Modifier = Modifier,
  @StringRes textRes: Int = R.string.add_wizard_add_more,
  icon: ImageVector? = Icons.Default.Replay,
  onClick: () -> Unit
) =
  AddWizardButton(onClick = onClick, modifier = modifier) {
    icon?.let {
      androidx.compose.foundation.Image(
        imageVector = it,
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
      )
      Spacer(modifier = Modifier.width(Distance.tiny))
    }
    Text(
      text = stringResource(textRes),
      style = MaterialTheme.typography.labelLarge,
    )
  }
