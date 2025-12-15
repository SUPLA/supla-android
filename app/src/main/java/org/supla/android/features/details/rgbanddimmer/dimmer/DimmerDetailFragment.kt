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

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.detailbase.base.ItemBundle

private const val ARG_ITEM_BUNDLE = "ARG_ITEM_BUNDLE"

@AndroidEntryPoint
class DimmerDetailFragment : BaseComposeFragment<DimmerDetailModelState, DimmerDetailViewEvent>() {
  override val viewModel: DimmerDetailViewModel by viewModels()

  private val item: ItemBundle by lazy { requireSerializable(ARG_ITEM_BUNDLE, ItemBundle::class.java) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.observeData(item.remoteId, item.itemType)
  }

  @Composable
  override fun ComposableContent(modelState: DimmerDetailModelState) {
    SuplaTheme {
      viewModel.View(modelState.viewState)
    }
  }

  override fun handleEvents(event: DimmerDetailViewEvent) {
    when (event) {
      DimmerDetailViewEvent.ShowLimitReached ->
        Toast.makeText(requireContext(), getText(R.string.rgb_detail_colors_limit), Toast.LENGTH_SHORT).show()
    }
  }

  companion object {
    fun bundle(itemBundle: ItemBundle) = bundleOf(
      ARG_ITEM_BUNDLE to itemBundle
    )
  }
}
