package org.supla.android.features.devicecatalog
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.BodyMedium
import org.supla.android.ui.views.buttons.BlueTextButton

@Composable
fun DeviceCatalogErrorView(
  onTryAgainClick: () -> Unit = {},
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(color = MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(all = Distance.default),
      verticalArrangement = Arrangement.spacedBy(Distance.default),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_status_error),
        contentDescription = null
      )
      BodyMedium(stringResource(id = R.string.device_catalog_error))
      BlueTextButton(
        text = stringResource(id = R.string.status_try_again),
        onClick = onTryAgainClick
      )
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    DeviceCatalogErrorView()
  }
}
