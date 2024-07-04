package org.supla.android.features.main
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
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.ui.BackHandler
import org.supla.android.core.ui.BaseFragment
import org.supla.android.databinding.FragmentMainBinding
import org.supla.android.features.notificationinfo.NotificationInfoDialog
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment :
  BaseFragment<MainViewState, MainViewEvent>(R.layout.fragment_main), BackHandler {

  override val viewModel: MainViewModel by viewModels()
  private val binding by viewBinding(FragmentMainBinding::bind)
  private val pages = ListPage.values()

  @Inject
  lateinit var notificationsHelper: NotificationsHelper

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.mainBottomBar.inflateMenu(R.menu.bottom_dashboard)
    binding.mainBottomBar.setOnItemSelectedListener(this::onBottomMenuItemSelected)

    binding.mainViewPager.adapter = StandardDetailPagerAdapter(pages, this)
    binding.mainViewPager.registerOnPageChangeCallback(pagerCallback)
    binding.mainViewPager.isUserInputEnabled = false

    notificationsHelper.setup(requireActivity()) {
      NotificationInfoDialog.create().show(requireActivity().supportFragmentManager, null)
    }
  }

  override fun onResume() {
    super.onResume()
    binding.mainBottomBar.labelVisibilityMode = viewModel.getLabelVisibility()
  }

  override fun handleEvents(event: MainViewEvent) {
  }

  override fun handleViewState(state: MainViewState) {
  }

  private fun onBottomMenuItemSelected(menuItem: MenuItem): Boolean {
    binding.mainViewPager.setCurrentItem(pages.map { it.menuId }.indexOf(menuItem.itemId), false)
    return true
  }

  private val pagerCallback = object : ViewPager2.OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
      binding.mainBottomBar.selectedItemId = pages[position].menuId
    }
  }

  override fun onBackPressed(): Boolean {
    if (binding.mainViewPager.currentItem != 0) {
      binding.mainViewPager.setCurrentItem(0, false)
      return true
    }

    return false
  }
}
