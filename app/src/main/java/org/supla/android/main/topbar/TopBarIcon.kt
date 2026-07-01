package org.supla.android.main.topbar
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

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.ui.views.texts.BodyMedium

@Composable
fun TopBarIcon.Icon(
  topBarController: TopBarController,
  modifier: Modifier = Modifier
) {
  when (this) {
    TopBarIcon.ReloadHistory -> DeleteChartHistory(topBarController, modifier)
    TopBarIcon.OpenOcr -> OpenOcr(topBarController, modifier)
    TopBarIcon.OpenSettings -> OpenSettings(topBarController, modifier)
  }
}

@Composable
private fun DeleteChartHistory(
  topBarController: TopBarController,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  Box(modifier = modifier) {
    var menuExpanded by remember { mutableStateOf(false) }

    IconButton(onClick = { menuExpanded = true }) {
      Icon(Icons.Default.MoreVert, contentDescription = null)
    }

    DropdownMenu(
      expanded = menuExpanded,
      onDismissRequest = { menuExpanded = false }
    ) {
      DropdownMenuItem(
        text = { BodyMedium(R.string.toolbar_delete_chart_history) },
        onClick = {
          menuExpanded = false
          scope.launch {
            topBarController.emit(TopBarEvent.ReloadChartHistory)
          }
        }
      )
    }
  }
}

@Composable
private fun OpenOcr(
  topBarController: TopBarController,
  modifier: Modifier = Modifier
) = SingleIcon(TopBarIcon.OpenOcr, topBarController, modifier)

@Composable
private fun OpenSettings(
  topBarController: TopBarController,
  modifier: Modifier = Modifier
) = SingleIcon(TopBarIcon.OpenSettings, topBarController, modifier)

@Composable
private fun SingleIcon(
  icon: TopBarIcon,
  topBarController: TopBarController,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()

  IconButton(
    onClick = { scope.launch { topBarController.emit(icon.event) } },
    modifier = modifier
  ) {
    Icon(
      painter = painterResource(icon.iconRes!!),
      contentDescription = icon.descriptionRes?.let { stringResource(it) }
    )
  }
}
