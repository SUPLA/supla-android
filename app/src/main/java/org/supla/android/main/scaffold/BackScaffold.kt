package org.supla.android.main.scaffold
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.main.snackbar.LocalSnackbarController
import org.supla.android.main.view.StandardTopBar
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.views.EmptyListInfoView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackScaffold(
  content: @Composable () -> Unit
) {
  val snackbarController = LocalSnackbarController.current
  val useNavigationBarPadding = LocalConfiguration.current.isPhoneLandscape

  Scaffold(
    topBar = { StandardTopBar(useNavigationBarPadding) },
    snackbarHost = { SnackbarHost(snackbarController.state) }
  ) { paddings ->
    CompositionLocalProvider(LocalScaffoldPadding provides paddings) {
      content()
    }
  }
}

@Composable
@SuplaPreview
private fun Preview() {
  SuplaTheme {
    BackScaffold {
      Box(
        modifier = Modifier.fillMaxSize().screenPaddings()
      ) {
        EmptyListInfoView(modifier = Modifier.align(Alignment.TopCenter))
      }
    }
  }
}
