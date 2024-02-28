package org.supla.android.features.details.detailbase.standarddetail
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
import org.supla.android.features.details.blindsdetail.general.BlindsGeneralFragment
import org.supla.android.features.details.gpmdetail.history.GpmHistoryDetailFragment
import org.supla.android.features.details.legacydetail.LegacyDetailFragment
import org.supla.android.features.details.switchdetail.general.SwitchGeneralFragment
import org.supla.android.features.details.switchdetail.timer.TimersDetailFragment
import org.supla.android.features.details.thermometerdetail.history.ThermometerHistoryDetailFragment
import org.supla.android.features.details.thermostatdetail.general.ThermostatGeneralFragment
import org.supla.android.features.details.thermostatdetail.history.ThermostatHistoryDetailFragment
import org.supla.android.features.details.thermostatdetail.schedule.ScheduleDetailFragment
import org.supla.android.features.details.thermostatdetail.timer.TimerDetailFragment
import org.supla.android.usecases.details.LegacyDetailType

class StandardDetailPagerAdapter(
  private val pages: List<DetailPage>,
  private val itemBundle: ItemBundle,
  fragment: Fragment
) : FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int = pages.size

  override fun createFragment(position: Int): Fragment = when (pages[position]) {
    DetailPage.SWITCH -> SwitchGeneralFragment().apply { arguments = SwitchGeneralFragment.bundle(itemBundle) }
    DetailPage.SWITCH_TIMER -> TimersDetailFragment().apply { arguments = TimersDetailFragment.bundle(itemBundle.remoteId) }
    DetailPage.HISTORY_IC -> LegacyDetailFragment().apply {
      arguments = LegacyDetailFragment.bundle(itemBundle.remoteId, LegacyDetailType.IC, itemBundle.itemType)
    }

    DetailPage.HISTORY_EM -> LegacyDetailFragment().apply {
      arguments = LegacyDetailFragment.bundle(itemBundle.remoteId, LegacyDetailType.EM, itemBundle.itemType)
    }

    DetailPage.THERMOSTAT -> ThermostatGeneralFragment().apply { arguments = ThermostatGeneralFragment.bundle(itemBundle) }
    DetailPage.SCHEDULE -> ScheduleDetailFragment().apply { arguments = ScheduleDetailFragment.bundle(itemBundle) }
    DetailPage.THERMOSTAT_TIMER -> TimerDetailFragment().apply { arguments = TimerDetailFragment.bundle(itemBundle) }
    DetailPage.THERMOSTAT_HISTORY -> ThermostatHistoryDetailFragment().apply {
      arguments = ThermostatHistoryDetailFragment.bundle(itemBundle.remoteId)
    }

    DetailPage.THERMOMETER_HISTORY -> ThermometerHistoryDetailFragment().apply {
      arguments = ThermometerHistoryDetailFragment.bundle(itemBundle.remoteId)
    }

    DetailPage.GPM_HISTORY -> GpmHistoryDetailFragment().apply { arguments = GpmHistoryDetailFragment.bundle(itemBundle.remoteId) }

    DetailPage.BLINDS -> BlindsGeneralFragment().apply { arguments = BlindsGeneralFragment.bundle(itemBundle) }
  }
}

enum class DetailPage(val menuId: Int) {
  // Switches
  SWITCH(R.id.detail_general),
  SWITCH_TIMER(R.id.detail_timer),
  HISTORY_IC(R.id.detail_metrics),
  HISTORY_EM(R.id.detail_metrics),

  // Thermostats
  THERMOSTAT(R.id.detail_general),
  SCHEDULE(R.id.detail_schedule),
  THERMOSTAT_HISTORY(R.id.detail_history),
  THERMOSTAT_TIMER(R.id.detail_timer),

  // Thermometers
  THERMOMETER_HISTORY(R.id.detail_history),

  // GPM
  GPM_HISTORY(R.id.detail_history),

  // Blinds
  BLINDS(R.id.detail_general)
}
