package org.supla.android.features.thermostatdetail
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

import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentThermostatDetailBinding
import org.supla.android.db.Channel
import org.supla.android.features.standarddetail.DetailPage
import org.supla.android.features.standarddetail.StandardDetailFragment

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"
private const val ARG_ITEM_TYPE = "ARG_ITEM_TYPE"
private const val ARG_FUNCTION = "ARG_FUNCTION"
private const val ARG_PAGES = "ARG_PAGES"

@AndroidEntryPoint
class ThermostatDetailFragment :
  StandardDetailFragment<ThermostatDetailViewState, ThermostatDetailViewEvent>(R.layout.fragment_thermostat_detail) {

  override val viewModel: ThermostatDetailViewModel by viewModels()

  private val binding by viewBinding(FragmentThermostatDetailBinding::bind)

  override val detailBottomBar: BottomNavigationView
    get() = binding.detailBottomBar
  override val detailShadow: View
    get() = binding.detailShadow
  override val detailViewPager: ViewPager2
    get() = binding.detailViewPager

  override fun handleViewState(state: ThermostatDetailViewState) {
    super.handleViewState(state)

    (state.channelBase as? Channel)?.let { channel ->
      setToolbarTitle(channel.getNotEmptyCaption(context))
    }
  }

  override fun isCloseEvent(event: ThermostatDetailViewEvent) = event == ThermostatDetailViewEvent.Close

  companion object {
    fun bundle(remoteId: Int, itemType: ItemType, function: Int, pages: Array<DetailPage>) = bundleOf(
      ARG_REMOTE_ID to remoteId,
      ARG_ITEM_TYPE to itemType,
      ARG_FUNCTION to function,
      ARG_PAGES to pages
    )
  }
}
