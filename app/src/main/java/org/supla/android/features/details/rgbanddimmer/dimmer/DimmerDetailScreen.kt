package org.supla.android.features.details.rgbanddimmer.dimmer
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

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerDetailViewEvent
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.ViewModelHost
import org.supla.android.main.topbar.RegisterStatedTopBarIcon
import org.supla.android.main.topbar.TopBarIcon

@Composable
fun DimmerDetailScreen(
  item: ItemBundle,
  navigator: MainComposeNavigator,
  viewModel: DimmerDetailViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  ViewModelHost(
    viewModel = viewModel,
    onCreate = { viewModel.observeData(item.remoteId, item.itemType) },
    eventHandler = { handleEvent(it, context) }
  ) { state ->
    RegisterStatedTopBarIcon(
      icon = TopBarIcon.OpenSettings,
      visible = state.hasSettings,
      handler = { navigator.navigateTo(MainRoute.LegacyDimmerSettings(item)) }
    )

    viewModel.View(state.viewState)
  }
}

private fun handleEvent(event: DimmerDetailViewEvent, context: Context) {
  when (event) {
    DimmerDetailViewEvent.ShowLimitReached ->
      Toast.makeText(context, R.string.rgb_detail_colors_limit, Toast.LENGTH_SHORT).show()
  }
}
