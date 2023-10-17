package org.supla.android.features.switchdetail
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
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentStandardDetailBinding
import org.supla.android.features.standarddetail.DetailPage
import org.supla.android.features.standarddetail.StandardDetailFragment

@AndroidEntryPoint
class SwitchDetailFragment :
  StandardDetailFragment<SwitchDetailViewState, SwitchDetailViewEvent>(R.layout.fragment_standard_detail) {

  override val viewModel: SwitchDetailViewModel by viewModels()

  private val binding by viewBinding(FragmentStandardDetailBinding::bind)

  override val detailBottomBar: BottomNavigationView
    get() = binding.detailBottomBar
  override val detailShadow: View
    get() = binding.detailShadow
  override val detailViewPager: ViewPager2
    get() = binding.detailViewPager

  override fun isCloseEvent(event: SwitchDetailViewEvent) = event == SwitchDetailViewEvent.Close

  override fun updateToolbarTitle(state: SwitchDetailViewState) {
    state.channelBase?.let {
      setToolbarTitle(it.getNotEmptyCaption(context))
    }
  }

  companion object {
    fun bundle(remoteId: Int, itemType: ItemType, function: Int, pages: Array<DetailPage>) =
      StandardDetailFragment.bundle(remoteId, itemType, function, pages)
  }
}
