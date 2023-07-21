package org.supla.android.features.standarddetail
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
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.storage.RuntimeStateHolder
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentStandardDetailBinding
import org.supla.android.extensions.visibleIf
import org.supla.android.model.ItemType
import javax.inject.Inject

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"
private const val ARG_ITEM_TYPE = "ARG_ITEM_TYPE"
private const val ARG_FUNCTION = "ARG_FUNCTION"
private const val ARG_PAGES = "ARG_PAGES"

@AndroidEntryPoint
class StandardDetailFragment :
  BaseFragment<StandardDetailViewState, StandardDetailViewEvent>(R.layout.fragment_standard_detail) {

  @Inject
  lateinit var runtimeStateHolder: RuntimeStateHolder

  private val viewModel: StandardDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentStandardDetailBinding::bind)

  @Suppress("DEPRECATION") // Not deprecated method can be accessed from API 33
  private val itemType: ItemType by lazy { requireArguments().getSerializable(ARG_ITEM_TYPE) as ItemType }
  private val remoteId: Int by lazy { requireArguments().getInt(ARG_REMOTE_ID) }
  private val function: Int by lazy { requireArguments().getInt(ARG_FUNCTION) }

  @Suppress("UNCHECKED_CAST", "DEPRECATION") // Not deprecated method can be accessed from API 33
  private val pages by lazy { (requireArguments().getSerializable(ARG_PAGES) as Array<DetailPage>).asList() }

  override fun getViewModel(): BaseViewModel<StandardDetailViewState, StandardDetailViewEvent> = viewModel

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val openedPage = getOpenedPage()
    binding.detailBottomBar.inflateMenu(R.menu.detail_bottom)
    for (item in binding.detailBottomBar.menu.children) {
      item.isVisible = pages.map { it.menuId }.contains(item.itemId)
    }
    binding.detailBottomBar.selectedItemId = pages[openedPage].menuId
    binding.detailBottomBar.setOnItemSelectedListener(this::onBottomMenuItemSelected)
    binding.detailBottomBar.visibleIf(pages.count() > 1)
    binding.detailShadow.visibleIf(pages.count() > 1)

    binding.detailViewPager.adapter = StandardDetailPagerAdapter(pages, remoteId, itemType, this)
    binding.detailViewPager.setCurrentItem(openedPage, false)
    binding.detailViewPager.registerOnPageChangeCallback(pagerCallback)
    binding.detailViewPager.isUserInputEnabled = false

    viewModel.observeUpdates(remoteId, itemType, function)
    viewModel.loadData(remoteId, itemType, function)
  }

  override fun handleEvents(event: StandardDetailViewEvent) {
    when (event) {
      StandardDetailViewEvent.Close -> requireActivity().finish()
    }
  }

  override fun handleViewState(state: StandardDetailViewState) {
    state.channelBase?.let {
      setToolbarTitle(it.getNotEmptyCaption(context))
    }
  }

  private fun onBottomMenuItemSelected(menuItem: MenuItem): Boolean {
    binding.detailViewPager.currentItem = pages.map { it.menuId }.indexOf(menuItem.itemId)
    return true
  }

  private val pagerCallback = object : OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
      binding.detailBottomBar.selectedItemId = pages[position].menuId
      runtimeStateHolder.setDetailOpenedPage(remoteId, position)
    }
  }

  private fun getOpenedPage(): Int {
    val openedPage = runtimeStateHolder.getDetailOpenedPage(remoteId)
    return if (openedPage < 0 || openedPage >= pages.size) {
      0
    } else {
      openedPage
    }
  }

  companion object {
    fun bundle(remoteId: Int, itemType: ItemType, function: Int, pages: Array<DetailPage>) = bundleOf(
      ARG_REMOTE_ID to remoteId,
      ARG_ITEM_TYPE to itemType,
      ARG_FUNCTION to function,
      ARG_PAGES to pages
    )
  }
}
