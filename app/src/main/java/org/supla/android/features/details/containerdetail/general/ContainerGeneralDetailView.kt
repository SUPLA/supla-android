package org.supla.android.features.details.containerdetail.general
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.images.ImageId
import org.supla.android.ui.views.Image

data class ContainerGeneralDetailViewState(
  val icon: ImageId? = null,
  val level: String = "",
  val value: String = ""
)

@Composable
fun ContainerGeneralDetailView(state: ContainerGeneralDetailViewState) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Distance.default),
    modifier = Modifier.fillMaxWidth().padding(Distance.default)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
      state.icon?.let {
        Image(it)
      }
      Text(state.level, style = MaterialTheme.typography.titleLarge)
    }
    Text(state.value, style = MaterialTheme.typography.bodyMedium)
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Preview() {
  SuplaTheme {
    ContainerGeneralDetailView(
      ContainerGeneralDetailViewState(
        icon = ImageId(R.drawable.fnc_container_half),
        level = "43%",
        value = "0 0 0 12 33 0 87"
      )
    )
  }
}
