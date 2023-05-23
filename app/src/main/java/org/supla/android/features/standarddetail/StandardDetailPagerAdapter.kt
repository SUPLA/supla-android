package org.supla.android.features.standarddetail

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.supla.android.R
import org.supla.android.features.legacydetail.LegacyDetailFragment
import org.supla.android.features.standarddetail.scheduledetail.ScheduleDetailFragment
import org.supla.android.features.standarddetail.switchdetail.SwitchDetailFragment
import org.supla.android.features.standarddetail.timersdetail.TimersDetailFragment
import org.supla.android.model.ItemType
import org.supla.android.usecases.details.LegacyDetailType

class StandardDetailPagerAdapter(
  private val pages: List<DetailPage>,
  private val remoteId: Int,
  private val itemType: ItemType,
  fragment: Fragment
) : FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int = pages.size

  override fun createFragment(position: Int): Fragment = when (pages[position]) {
    DetailPage.GENERAL -> SwitchDetailFragment().apply { arguments = SwitchDetailFragment.bundle(remoteId, itemType) }
    DetailPage.TIMER -> TimersDetailFragment().apply { arguments = TimersDetailFragment.bundle(remoteId) }
    DetailPage.HISTORY_IC -> LegacyDetailFragment().apply {
      arguments = LegacyDetailFragment.bundle(remoteId, LegacyDetailType.IC, itemType)
    }
    DetailPage.HISTORY_EM -> LegacyDetailFragment().apply {
      arguments = LegacyDetailFragment.bundle(remoteId, LegacyDetailType.EM, itemType)
    }
    else -> Fragment() // Just an empty fragment
  }
}

enum class DetailPage(val menuId: Int) {
  GENERAL(R.id.detail_general),
  TIMER(R.id.detail_timer),
  SCHEDULE(R.id.detail_schedule),
  HISTORY_IC(R.id.detail_history),
  HISTORY_EM(R.id.detail_history),
  SETTINGS(R.id.detail_settings)
}
