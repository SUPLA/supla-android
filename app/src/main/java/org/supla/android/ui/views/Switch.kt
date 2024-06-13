package org.supla.android.ui.views
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

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme

@Composable
fun Switch(checked: Boolean, modifier: Modifier = Modifier, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) =
  androidx.compose.material3.Switch(
    checked = checked,
    onCheckedChange = onCheckedChange,
    colors = SwitchDefaults.colors(
      uncheckedThumbColor = MaterialTheme.colorScheme.surface,
      uncheckedTrackColor = colorResource(id = R.color.gray_light),
      uncheckedBorderColor = colorResource(id = R.color.gray_light),
      checkedThumbColor = MaterialTheme.colorScheme.surface
    ),
    modifier = modifier,
    enabled = enabled
  )

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
  SuplaTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(all = Distance.default)
    ) {
      Switch(checked = true) {}
      Switch(checked = false) {}
      Switch(checked = true, enabled = false) {}
      Switch(checked = false, enabled = false) {}
    }
  }
}
