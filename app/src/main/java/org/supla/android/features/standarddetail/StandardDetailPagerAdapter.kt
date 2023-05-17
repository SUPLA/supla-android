package org.supla.android.features.standarddetail

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.supla.android.R
import org.supla.android.features.standarddetail.scheduledetail.ScheduleDetailFragment
import org.supla.android.features.standarddetail.switchdetail.SwitchDetailFragment
import org.supla.android.model.ItemType


class StandardDetailPagerAdapter(
  private val pages: List<DetailPage>,
  private val remoteId: Int,
  private val itemType: ItemType,
  fragment: Fragment
) : FragmentStateAdapter(fragment) {


  override fun getItemCount(): Int = pages.size

  override fun createFragment(position: Int): Fragment = when (pages[position]) {
    DetailPage.GENERAL -> SwitchDetailFragment().apply { arguments = SwitchDetailFragment.bundle(remoteId, itemType) }
    else -> ScheduleDetailFragment()
  }
}

enum class DetailPage(val menuId: Int) {
  GENERAL(R.id.detail_general), SCHEDULE(R.id.detail_schedule), HISTORY(R.id.detail_history), SETTINGS(R.id.detail_settings)
}