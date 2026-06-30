package org.supla.android.features.notificationslog
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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.features.nfc.call.screens.ViewModelHost
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.scaffold.LocalScaffoldPadding
import org.supla.android.main.view.SearchTopBar
import org.supla.android.ui.views.texts.BodyMedium

@Composable
fun NotificationsLogScreen(
  navigator: MainComposeNavigator,
  viewModel: NotificationsLogViewModel = hiltViewModel()
) {
  var searchText by remember { mutableStateOf("") }

  BackHandler {
    if (searchText.isEmpty()) {
      navigator.back()
    } else {
      searchText = ""
      viewModel.loadAll()
    }
  }

  Scaffold(
    topBar = {
      SearchTopBar(
        searchText = searchText,
        onBackClick = {
          if (searchText.isEmpty()) {
            navigator.back()
          } else {
            searchText = ""
            viewModel.loadAll()
          }
        },
        onTextChange = {
          searchText = it
          viewModel.search(it)
        },
        rightIcon = {
          RightMenu(
            onDeleteAll = { viewModel.askDeleteAll() },
            onDeleteOlderThanMonth = { viewModel.askDeleteOlderThanMonth() }
          )
        }
      )
    }
  ) { paddings ->
    CompositionLocalProvider(LocalScaffoldPadding provides paddings) {
      ViewModelHost(
        viewModel = viewModel
      ) { state ->
        viewModel.View(state)
      }
    }
  }
}

@Composable
fun RightMenu(
  onDeleteAll: () -> Unit,
  onDeleteOlderThanMonth: () -> Unit
) {
  Box {
    var menuExpanded by remember { mutableStateOf(false) }

    IconButton(onClick = { menuExpanded = true }) {
      Icon(Icons.Default.MoreVert, contentDescription = "Więcej")
    }

    DropdownMenu(
      expanded = menuExpanded,
      onDismissRequest = { menuExpanded = false }
    ) {
      DropdownMenuItem(
        text = { BodyMedium(R.string.toolbar_delete_all) },
        onClick = {
          menuExpanded = false
          onDeleteAll()
        }
      )

      DropdownMenuItem(
        text = { BodyMedium(R.string.toolbar_delete_older_than_month) },
        onClick = {
          menuExpanded = false
          onDeleteOlderThanMonth()
        }
      )
    }
  }
}
