package org.supla.android.features.details.impulsecounter
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

import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.databinding.FragmentStandardDetailBinding
import org.supla.android.features.details.detailbase.base.BaseDetailFragment
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.details.impulsecounter.counterphoto.CounterPhotoFragment
import org.supla.android.ui.ToolbarItemsClickHandler

@AndroidEntryPoint
class ImpulseCounterDetailFragment :
  BaseDetailFragment<ImpulseCounterDetailViewState, ImpulseCounterDetailViewEvent>(R.layout.fragment_standard_detail),
  ToolbarItemsClickHandler {

  override val viewModel: ImpulseCounterDetailViewModel by viewModels()

  private val binding by viewBinding(FragmentStandardDetailBinding::bind)

  override val detailBottomBar: BottomNavigationView
    get() = binding.detailBottomBar
  override val detailShadow: View
    get() = binding.detailShadow
  override val detailViewPager: ViewPager2
    get() = binding.detailViewPager

  override fun isCloseEvent(event: ImpulseCounterDetailViewEvent) = event == ImpulseCounterDetailViewEvent.Close

  override fun updateToolbarTitle(state: ImpulseCounterDetailViewState) {
    state.caption?.let { setToolbarTitle(it(requireContext())) }
  }

  override fun onStart() {
    super.onStart()
    setToolbarItemVisible(R.id.toolbar_ocr, viewState.hasPhoto)
  }

  override fun onStop() {
    super.onStop()
    setToolbarItemVisible(R.id.toolbar_ocr, false)
  }

  override fun handleViewState(state: ImpulseCounterDetailViewState) {
    super.handleViewState(state)
    setToolbarItemVisible(R.id.toolbar_ocr, state.hasPhoto)
  }

  override fun onMenuItemClick(menuItem: MenuItem): Boolean {
    if (menuItem.itemId == R.id.toolbar_ocr) {
      navigator.navigateTo(R.id.counter_photo_fragment, CounterPhotoFragment.bundle(item))
      return true
    }

    return false
  }

  companion object {
    fun bundle(itemBundle: ItemBundle, pages: Array<DetailPage>) =
      BaseDetailFragment.bundle(itemBundle, pages)
  }
}
