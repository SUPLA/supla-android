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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.ui.views.buttons.OutlinedButton

@Composable
fun AddWizardRepeatButton(modifier: Modifier = Modifier, @StringRes textRes: Int = R.string.add_wizard_add_more, onClick: () -> Unit) =
  OutlinedButton(
    colors = ButtonDefaults.addWizardButtonColors(),
    contentPadding = PaddingValues(start = Distance.small, top = Distance.small, end = Distance.small, bottom = Distance.small),
    onClick = onClick,
    modifier = modifier.defaultMinSize(minWidth = 130.dp, minHeight = 56.dp),
  ) {
    androidx.compose.foundation.Image(
      imageVector = Icons.Default.Replay,
      contentDescription = null,
      colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
    )
    Spacer(modifier = Modifier.width(Distance.tiny))
    Text(
      text = stringResource(textRes),
      style = MaterialTheme.typography.labelLarge,
    )
  }
