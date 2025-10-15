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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.supla.android.R
import org.supla.android.features.details.containerdetail.general.ContainerGeneralDetailFragment
import org.supla.android.features.details.electricitymeterdetail.general.ElectricityMeterGeneralFragment
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterHistoryFragment
import org.supla.android.features.details.electricitymeterdetail.settings.ElectricityMeterSettingsFragment
import org.supla.android.features.details.gpmdetail.history.GpmHistoryDetailFragment
import org.supla.android.features.details.humiditydetail.history.HumidityHistoryDetailFragment
import org.supla.android.features.details.impulsecounter.counterphoto.CounterPhotoFragment
import org.supla.android.features.details.impulsecounter.general.ImpulseCounterGeneralFragment
import org.supla.android.features.details.impulsecounter.history.ImpulseCounterHistoryDetailFragment
import org.supla.android.features.details.legacydetail.LegacyDetailFragment
import org.supla.android.features.details.switchdetail.general.SwitchGeneralFragment
import org.supla.android.features.details.switchdetail.timer.TimersDetailFragment
import org.supla.android.features.details.thermometerdetail.history.ThermometerHistoryDetailFragment
import org.supla.android.features.details.thermostatdetail.general.ThermostatGeneralFragment
import org.supla.android.features.details.thermostatdetail.heatpolhistory.HeatpolHistoryDetailFragment
import org.supla.android.features.details.thermostatdetail.history.ThermostatHistoryDetailFragment
import org.supla.android.features.details.thermostatdetail.schedule.ScheduleDetailFragment
import org.supla.android.features.details.thermostatdetail.slaves.ThermostatSlavesListFragment
import org.supla.android.features.details.thermostatdetail.timer.TimerDetailFragment
import org.supla.android.features.details.valveDetail.general.ValveGeneralDetailFragment
import org.supla.android.features.details.windowdetail.curtain.CurtainFragment
import org.supla.android.features.details.windowdetail.facadeblinds.FacadeBlindsFragment
import org.supla.android.features.details.windowdetail.garagedoor.GarageDoorFragment
import org.supla.android.features.details.windowdetail.projectorscreen.ProjectorScreenFragment
import org.supla.android.features.details.windowdetail.rollershutter.RollerShutterFragment
import org.supla.android.features.details.windowdetail.roofwindow.RoofWindowFragment
import org.supla.android.features.details.windowdetail.terraceawning.TerraceAwningFragment
import org.supla.android.features.details.windowdetail.verticalblinds.VerticalBlindsFragment
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

    DetailPage.THERMOSTAT -> ThermostatGeneralFragment().apply { arguments = ThermostatGeneralFragment.bundle(itemBundle) }
    DetailPage.THERMOSTAT_LIST -> ThermostatSlavesListFragment().apply { arguments = ThermostatSlavesListFragment.bundle(itemBundle) }
    DetailPage.SCHEDULE -> ScheduleDetailFragment().apply { arguments = ScheduleDetailFragment.bundle(itemBundle) }
    DetailPage.THERMOSTAT_TIMER -> TimerDetailFragment().apply { arguments = TimerDetailFragment.bundle(itemBundle) }
    DetailPage.THERMOSTAT_HISTORY -> ThermostatHistoryDetailFragment().apply {
      arguments = ThermostatHistoryDetailFragment.bundle(itemBundle.remoteId)
    }
    DetailPage.THERMOSTAT_HEATPOL_GENERAL -> LegacyDetailFragment().apply {
      arguments = LegacyDetailFragment.bundle(itemBundle.remoteId, LegacyDetailType.THERMOSTAT_HP, itemBundle.itemType)
    }
    DetailPage.THERMOSTAT_HEATPOL_HISTORY -> HeatpolHistoryDetailFragment().apply {
      arguments = HeatpolHistoryDetailFragment.bundle(itemBundle.remoteId)
    }

    DetailPage.THERMOMETER_HISTORY -> ThermometerHistoryDetailFragment().apply {
      arguments = ThermometerHistoryDetailFragment.bundle(itemBundle.remoteId)
    }

    DetailPage.HUMIDITY_HISTORY -> HumidityHistoryDetailFragment().apply {
      arguments = HumidityHistoryDetailFragment.bundle(itemBundle.remoteId)
    }

    DetailPage.GPM_HISTORY -> GpmHistoryDetailFragment().apply { arguments = GpmHistoryDetailFragment.bundle(itemBundle.remoteId) }

    DetailPage.ROLLER_SHUTTER -> RollerShutterFragment().apply { arguments = RollerShutterFragment.bundle(itemBundle) }
    DetailPage.ROOF_WINDOW -> RoofWindowFragment().apply { arguments = RoofWindowFragment.bundle(itemBundle) }
    DetailPage.FACADE_BLINDS -> FacadeBlindsFragment().apply { arguments = FacadeBlindsFragment.bundle(itemBundle) }
    DetailPage.TERRACE_AWNING -> TerraceAwningFragment().apply { arguments = TerraceAwningFragment.bundle(itemBundle) }
    DetailPage.PROJECTOR_SCREEN -> ProjectorScreenFragment().apply { arguments = ProjectorScreenFragment.bundle(itemBundle) }
    DetailPage.CURTAIN -> CurtainFragment().apply { arguments = CurtainFragment.bundle(itemBundle) }
    DetailPage.VERTICAL_BLIND -> VerticalBlindsFragment().apply { arguments = VerticalBlindsFragment.bundle(itemBundle) }
    DetailPage.GARAGE_DOOR_ROLLER -> GarageDoorFragment().apply { arguments = GarageDoorFragment.bundle(itemBundle) }

    DetailPage.EM_GENERAL -> ElectricityMeterGeneralFragment().apply { arguments = ElectricityMeterGeneralFragment.bundle(itemBundle) }
    DetailPage.EM_HISTORY -> ElectricityMeterHistoryFragment().apply {
      arguments = ElectricityMeterHistoryFragment.bundle(itemBundle.remoteId)
    }
    DetailPage.EM_SETTINGS -> ElectricityMeterSettingsFragment().apply { arguments = ElectricityMeterSettingsFragment.bundle(itemBundle) }

    DetailPage.CONTAINER_GENERAL -> ContainerGeneralDetailFragment().apply {
      arguments = ContainerGeneralDetailFragment.bundle(itemBundle)
    }

    DetailPage.IC_GENERAL -> ImpulseCounterGeneralFragment().apply { arguments = ImpulseCounterGeneralFragment.bundle(itemBundle) }
    DetailPage.IC_HISTORY -> ImpulseCounterHistoryDetailFragment().apply {
      arguments = ImpulseCounterHistoryDetailFragment.bundle(itemBundle.remoteId)
    }
    DetailPage.IC_OCR -> CounterPhotoFragment().apply { arguments = CounterPhotoFragment.bundle(itemBundle) }

    DetailPage.VALVE_GENERAL -> ValveGeneralDetailFragment().apply { arguments = ValveGeneralDetailFragment.bundle(itemBundle) }
  }
}

enum class DetailPage(val item: DetailBottomItem) {
  // Switches
  SWITCH(DetailBottomItem.GENERAL),
  SWITCH_TIMER(DetailBottomItem.TIMER),

  // Thermostats
  THERMOSTAT(DetailBottomItem.GENERAL),
  THERMOSTAT_LIST(DetailBottomItem.LIST),
  SCHEDULE(DetailBottomItem.SCHEDULE),
  THERMOSTAT_HISTORY(DetailBottomItem.HISTORY),
  THERMOSTAT_TIMER(DetailBottomItem.TIMER),
  THERMOSTAT_HEATPOL_GENERAL(DetailBottomItem.GENERAL),
  THERMOSTAT_HEATPOL_HISTORY(DetailBottomItem.HISTORY),

  // Thermometers
  THERMOMETER_HISTORY(DetailBottomItem.HISTORY),

  // Humidity
  HUMIDITY_HISTORY(DetailBottomItem.HISTORY),

  // GPM
  GPM_HISTORY(DetailBottomItem.HISTORY),

  // Window details
  ROLLER_SHUTTER(DetailBottomItem.GENERAL),
  ROOF_WINDOW(DetailBottomItem.GENERAL),
  FACADE_BLINDS(DetailBottomItem.GENERAL),
  TERRACE_AWNING(DetailBottomItem.GENERAL),
  PROJECTOR_SCREEN(DetailBottomItem.GENERAL),
  CURTAIN(DetailBottomItem.GENERAL),
  VERTICAL_BLIND(DetailBottomItem.GENERAL),
  GARAGE_DOOR_ROLLER(DetailBottomItem.GENERAL),

  // EM
  EM_GENERAL(DetailBottomItem.GENERAL),
  EM_HISTORY(DetailBottomItem.HISTORY),
  EM_SETTINGS(DetailBottomItem.SETTINGS),

  // Container
  CONTAINER_GENERAL(DetailBottomItem.GENERAL),

  // IC
  IC_GENERAL(DetailBottomItem.GENERAL),
  IC_HISTORY(DetailBottomItem.HISTORY),
  IC_OCR(DetailBottomItem.OCR),

  // Valve
  VALVE_GENERAL(DetailBottomItem.GENERAL)
}

enum class DetailBottomItem(val menuId: Int, @DrawableRes val iconRes: Int, @StringRes val stringRes: Int) {
  GENERAL(R.id.bottom_item_general, R.drawable.ic_general, R.string.details_general),
  SCHEDULE(R.id.bottom_item_schedule, R.drawable.ic_schedule, R.string.details_schedule),
  TIMER(R.id.bottom_item_timer, R.drawable.ic_timer, R.string.details_timer),
  HISTORY(R.id.bottom_item_history, R.drawable.ic_history, R.string.details_history),
  METRICS(R.id.bottom_item_metrics, R.drawable.ic_metrics, R.string.details_metrics),
  SETTINGS(R.id.bottom_item_settings, R.drawable.ic_settings, R.string.settings),
  LIST(R.id.bottom_item_list, R.drawable.list, R.string.details_list),
  OCR(R.id.bottom_item_ocr, R.drawable.ic_ocr_photo, R.string.toolbar_ocr)
}
