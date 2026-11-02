package org.supla.android.features.details.impulsecounter.general
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
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.details.detailbase.impulsecounter.ImpulseCounterMetricsView
import org.supla.android.main.LocalNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.ViewModelHost
import org.supla.android.main.topbar.RegisterStatedTopBarIcon
import org.supla.android.main.topbar.TopBarIcon

@Composable
fun ImpulseCounterGeneralScreen(
  item: ItemBundle,
  viewModel: ImpulseCounterGeneralViewModel = hiltViewModel()
) {
  val navigator = LocalNavigator.current
  ViewModelHost(
    viewModel = viewModel,
    onCreate = { viewModel.onViewCreated(item.remoteId) },
    onStart = { viewModel.loadData(item.remoteId) },
  ) {
    RegisterStatedTopBarIcon(
      icon = TopBarIcon.OpenOcr,
      visible = it.hasPhoto,
      handler = { navigator?.navigateTo(MainRoute.CounterPhoto(item.remoteId)) }
    )

    ImpulseCounterMetricsView(it.viewState)
  }
}
