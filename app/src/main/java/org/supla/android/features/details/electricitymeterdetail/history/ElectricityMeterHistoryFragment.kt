package org.supla.android.features.details.electricitymeterdetail.history
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

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle

private const val ARG_ITEM_BUNDLE = "ARG_ITEM_BUNDLE"

@AndroidEntryPoint
class ElectricityMeterHistoryFragment :
  BaseComposeFragment<ElectricityMeterHistoryViewModelState, ElectricityMeterHistoryViewEvent>() {

  override val viewModel: ElectricityMeterHistoryViewModel by viewModels()

  @Composable
  override fun ComposableContent() {
    SuplaTheme {
      Text(text = "Electricity meter history fragment")
    }
  }

  override fun handleEvents(event: ElectricityMeterHistoryViewEvent) {
  }

  companion object {
    fun bundle(itemBundle: ItemBundle) = bundleOf(
      ARG_ITEM_BUNDLE to itemBundle
    )
  }
}
