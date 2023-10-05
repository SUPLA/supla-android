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

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.supla.android.R
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.features.legacydetail.LegacyDetailFragment
import org.supla.android.features.switchdetail.switchgeneral.SwitchGeneralFragment
import org.supla.android.features.switchdetail.timersdetail.TimersDetailFragment
import org.supla.android.features.thermostatdetail.history.HistoryDetailFragment
import org.supla.android.features.thermostatdetail.scheduledetail.ScheduleDetailFragment
import org.supla.android.features.thermostatdetail.thermostatgeneral.ThermostatGeneralFragment
import org.supla.android.usecases.details.LegacyDetailType

class StandardDetailPagerAdapter(
  private val pages: List<DetailPage>,
  private val remoteId: Int,
  private val itemType: ItemType,
  fragment: Fragment
) : FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int = pages.size

  override fun createFragment(position: Int): Fragment = when (pages[position]) {
    DetailPage.SWITCH -> SwitchGeneralFragment().apply { arguments = SwitchGeneralFragment.bundle(remoteId, itemType) }
    DetailPage.TIMER -> TimersDetailFragment().apply { arguments = TimersDetailFragment.bundle(remoteId) }
    DetailPage.HISTORY_IC -> LegacyDetailFragment().apply {
      arguments = LegacyDetailFragment.bundle(remoteId, LegacyDetailType.IC, itemType)
    }
    DetailPage.HISTORY_EM -> LegacyDetailFragment().apply {
      arguments = LegacyDetailFragment.bundle(remoteId, LegacyDetailType.EM, itemType)
    }
    DetailPage.THERMOSTAT -> ThermostatGeneralFragment().apply { arguments = ThermostatGeneralFragment.bundle(remoteId) }
    DetailPage.SCHEDULE -> ScheduleDetailFragment().apply { arguments = ScheduleDetailFragment.bundle(remoteId) }
    DetailPage.THERMOSTAT_HISTORY -> HistoryDetailFragment().apply { arguments = HistoryDetailFragment.bundle(remoteId) }
    else -> Fragment() // Just an empty fragment
  }
}

enum class DetailPage(val menuId: Int) {
  // Switches
  SWITCH(R.id.detail_general),
  TIMER(R.id.detail_timer),
  HISTORY_IC(R.id.detail_metrics),
  HISTORY_EM(R.id.detail_metrics),

  // Thermostats
  THERMOSTAT(R.id.detail_general),
  SCHEDULE(R.id.detail_schedule),
  THERMOSTAT_HISTORY(R.id.detail_history),
}
