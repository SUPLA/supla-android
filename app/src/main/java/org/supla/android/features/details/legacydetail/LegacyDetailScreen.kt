package org.supla.android.features.details.legacydetail
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

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.features.details.detailbase.base.DetailViewEvent
import org.supla.android.features.details.detailbase.base.DetailViewModel
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.main.EventHandler
import org.supla.android.main.LifeCycleObserver
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.topbar.ManageTopBar
import org.supla.android.ui.views.LegacyFragmentScreen
import org.supla.core.shared.data.model.general.SuplaFunction

@Composable
fun LegacyDetailScreen(
  route: MainRoute.LegacyDetail,
  navigator: MainComposeNavigator,
  viewModel: DetailViewModel = hiltViewModel()
) {
  viewModel.LifeCycleObserver(
    onCreate = { viewModel.setup(route.itemBundle) }
  )

  EventHandler(viewModel) { handleEvent(it, navigator) }
  ManageTopBar(viewModel)

  LegacyFragmentScreen(
    fragmentClass = LegacyDetailFragment::class.java,
    arguments = LegacyDetailFragment.bundle(route.remoteId, route.legacyDetailType, route.itemType)
  )
}

private fun handleEvent(event: DetailViewEvent, navigator: MainComposeNavigator) =
  when (event) {
    DetailViewEvent.Close -> navigator.back()
  }

private val MainRoute.LegacyDetail.itemBundle: ItemBundle
  get() = ItemBundle(remoteId, 0, 0L, itemType, SuplaFunction.NONE)
