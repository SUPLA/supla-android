package org.supla.android.features.standarddetail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.ChannelStatePopup
import org.supla.android.R
import org.supla.android.core.storage.InternalPreferences
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentStandardDetailBinding
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.model.ItemType
import org.supla.android.ui.ToolbarItemsClickHandler
import javax.inject.Inject

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"
private const val ARG_ITEM_TYPE = "ARG_ITEM_TYPE"
private const val ARG_PAGES = "ARG_PAGES"

@AndroidEntryPoint
class StandardDetailFragment :
  BaseFragment<StandardDetailViewState, StandardDetailViewEvent>(R.layout.fragment_standard_detail),
  ToolbarItemsClickHandler {

  @Inject
  lateinit var internalPreferences: InternalPreferences

  private val viewModel: StandardDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentStandardDetailBinding::bind)
  private lateinit var statePopup: ChannelStatePopup

  private val itemType: ItemType by lazy { arguments!!.getSerializable(ARG_ITEM_TYPE) as ItemType }
  private val remoteId: Int by lazy { arguments!!.getInt(ARG_REMOTE_ID) }
  @Suppress("UNCHECKED_CAST")
  private val pages by lazy { (arguments!!.getSerializable(ARG_PAGES) as Array<DetailPage>).asList() }

  override fun getViewModel(): BaseViewModel<StandardDetailViewState, StandardDetailViewEvent> = viewModel

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    statePopup = ChannelStatePopup(requireActivity())
    val openedPage = getOpenedPage()
    binding.detailBottomBar.inflateMenu(R.menu.detail_bottom)
    for (item in binding.detailBottomBar.menu.children) {
      item.isVisible = pages.map { it.menuId }.contains(item.itemId)
    }
    binding.detailBottomBar.selectedItemId = pages[openedPage].menuId
    binding.detailBottomBar.setOnItemSelectedListener(this::onBottomMenuItemSelected)

    binding.detailViewPager.adapter = StandardDetailPagerAdapter(pages, remoteId, itemType, this)
    binding.detailViewPager.setCurrentItem(openedPage, false)
    binding.detailViewPager.registerOnPageChangeCallback(pagerCallback)

    viewModel.loadData(remoteId, itemType)
  }

  override fun onStart() {
    super.onStart()
    setToolbarItemVisible(R.id.toolbar_info, true)
  }

  override fun onStop() {
    setToolbarItemVisible(R.id.toolbar_info, false)
    super.onStop()
  }

  override fun handleEvents(event: StandardDetailViewEvent) {
  }

  override fun handleViewState(state: StandardDetailViewState) {
    if (state.channelBase != null) {
      setToolbarTitle(state.channelBase.getNotEmptyCaption(context))
    }
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onChannelState -> {
        if (message.channelState.channelID == remoteId && statePopup.isVisible) {
          statePopup.update(message.channelState)
        }
      }
      SuplaClientMsg.onDataChanged -> {
        if (message.channelId == remoteId) {
          viewModel.loadData(remoteId, itemType)
          if (statePopup.isVisible) {
            statePopup.update(remoteId)
          }
        }
      }
    }
  }

  private fun onBottomMenuItemSelected(menuItem: MenuItem): Boolean {
    binding.detailViewPager.currentItem = pages.map { it.menuId }.indexOf(menuItem.itemId)
    return true
  }

  private val pagerCallback = object : OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
      binding.detailBottomBar.selectedItemId = pages[position].menuId
      internalPreferences.setDetailOpenedPage(remoteId, position)
    }
  }

  override fun onMenuItemClick(menuItem: MenuItem): Boolean {
    if (menuItem.itemId == R.id.toolbar_info) {
      statePopup.show(remoteId)
      return true
    }

    return false
  }

  private fun getOpenedPage(): Int {
    val openedPage = internalPreferences.getDetailOpenedPage(remoteId)
    return if (openedPage < 0 || openedPage >= pages.size) {
      0
    } else {
      openedPage
    }
  }

  companion object {
    fun bundle(remoteId: Int, itemType: ItemType, pages: Array<DetailPage>) = bundleOf(
      ARG_REMOTE_ID to remoteId,
      ARG_ITEM_TYPE to itemType,
      ARG_PAGES to pages
    )
  }
}
