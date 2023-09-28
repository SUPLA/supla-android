package org.supla.android.features.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BackHandler
import org.supla.android.core.ui.BaseFragment
import org.supla.android.databinding.FragmentMainBinding

@AndroidEntryPoint
class MainFragment :
  BaseFragment<MainViewState, MainViewEvent>(R.layout.fragment_main), BackHandler {

  override val viewModel: MainViewModel by viewModels()
  private val binding by viewBinding(FragmentMainBinding::bind)
  private val pages = ListPage.values()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.mainBottomBar.inflateMenu(R.menu.bottom_dashboard)
    binding.mainBottomBar.setOnItemSelectedListener(this::onBottomMenuItemSelected)

    binding.mainViewPager.adapter = StandardDetailPagerAdapter(pages, this)
    binding.mainViewPager.registerOnPageChangeCallback(pagerCallback)
    binding.mainViewPager.isUserInputEnabled = false
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
