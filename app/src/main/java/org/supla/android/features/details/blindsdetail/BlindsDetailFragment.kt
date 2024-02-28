package org.supla.android.features.details.blindsdetail

import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.databinding.FragmentStandardDetailBinding
import org.supla.android.features.details.detailbase.standarddetail.DetailPage
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.features.details.detailbase.standarddetail.StandardDetailFragment

@AndroidEntryPoint
class BlindsDetailFragment : StandardDetailFragment<BlindsDetailViewState, BlindsDetailViewEvent>(R.layout.fragment_standard_detail) {
  override val viewModel: BlindDetailViewModel by viewModels()

  private val binding by viewBinding(FragmentStandardDetailBinding::bind)

  override val detailBottomBar: BottomNavigationView
    get() = binding.detailBottomBar
  override val detailShadow: View
    get() = binding.detailShadow
  override val detailViewPager: ViewPager2
    get() = binding.detailViewPager

  override fun isCloseEvent(event: BlindsDetailViewEvent) = event == BlindsDetailViewEvent.Close

  override fun updateToolbarTitle(state: BlindsDetailViewState) {
    state.caption?.let { setToolbarTitle(it(requireContext())) }
  }

  companion object {
    fun bundle(itemBundle: ItemBundle, pages: Array<DetailPage>) =
      StandardDetailFragment.bundle(itemBundle, pages)
  }
}