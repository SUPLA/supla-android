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

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.view.StandardTopBar

@Composable
fun BackScaffold(
  navigator: MainComposeNavigator,
  content: @Composable () -> Unit
) {
  Scaffold(
    topBar = {
      StandardTopBar { navigator.back() }
    },
  ) { paddings ->
    CompositionLocalProvider(LocalScaffoldPadding provides paddings) {
      content()
    }
  }
}
