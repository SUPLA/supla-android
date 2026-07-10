package org.supla.android.features.details.detailbase.base
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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.features.details.containerdetail.general.ContainerGeneralScreen
import org.supla.android.features.details.electricitymeterdetail.general.ElectricityMeterGeneralScreen
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterHistoryScreen
import org.supla.android.features.details.electricitymeterdetail.settings.ElectricityMeterSettingsScreen
import org.supla.android.features.details.gatedetail.general.GateGeneralScreen
import org.supla.android.features.details.gpmdetail.history.GpmHistoryScreen
import org.supla.android.features.details.humiditydetail.history.HumidityHistoryScreen
import org.supla.android.features.details.impulsecounter.counterphoto.CounterPhotoScreen
import org.supla.android.features.details.impulsecounter.general.ImpulseCounterGeneralScreen
import org.supla.android.features.details.impulsecounter.history.ImpulseCounterHistoryScreen
import org.supla.android.features.details.impulsecounter.settings.ImpulseCounterSettingsScreen
import org.supla.android.features.details.legacydetail.ThermostatHeatpolGeneralScreen
import org.supla.android.features.details.recuperator.general.RecuperatorGeneralScreen
import org.supla.android.features.details.recuperator.schedule.RecuperatorScheduleScreen
import org.supla.android.features.details.rgbanddimmer.dimmer.DimmerDetailScreen
import org.supla.android.features.details.rgbanddimmer.dimmercct.DimmerCctDetailScreen
import org.supla.android.features.details.rgbanddimmer.rgb.RgbDetailScreen
import org.supla.android.features.details.switchdetail.general.SwitchGeneralScreen
import org.supla.android.features.details.switchdetail.timer.SwitchTimerScreen
import org.supla.android.features.details.thermometerdetail.history.ThermometerHistoryScreen
import org.supla.android.features.details.thermostatdetail.general.ThermostatGeneralScreen
import org.supla.android.features.details.thermostatdetail.heatpolhistory.HeatpolHistoryScreen
import org.supla.android.features.details.thermostatdetail.history.ThermostatHistoryScreen
import org.supla.android.features.details.thermostatdetail.schedule.ThermostatScheduleScreen
import org.supla.android.features.details.thermostatdetail.slaves.ThermostatSlavesListScreen
import org.supla.android.features.details.thermostatdetail.timer.ThermostatTimerScreen
import org.supla.android.features.details.valveDetail.general.ValveGeneralScreen
import org.supla.android.features.details.windowdetail.curtain.CurtainScreen
import org.supla.android.features.details.windowdetail.facadeblinds.FacadeBlindsScreen
import org.supla.android.features.details.windowdetail.garagedoor.GarageDoorScreen
import org.supla.android.features.details.windowdetail.projectorscreen.ProjectorScreenScreen
import org.supla.android.features.details.windowdetail.rollershutter.RollerShutterScreen
import org.supla.android.features.details.windowdetail.roofwindow.RoofWindowScreen
import org.supla.android.features.details.windowdetail.terraceawning.TerraceAwningScreen
import org.supla.android.features.details.windowdetail.verticalblinds.VerticalBlindsScreen
import org.supla.android.main.EventHandler
import org.supla.android.main.LifeCycleObserver
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.scaffold.LocalScaffoldPadding
import org.supla.android.main.topbar.ManageTopBar
import org.supla.android.main.view.NavigationBarLabel
import org.supla.android.main.view.StandardTopBar
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.navigation.SuplaNavigationBarItem
import org.supla.android.ui.navigation.SuplaRailItem

@Composable
fun DetailScreen(
  item: ItemBundle,
  pages: List<DetailPage>,
  navigator: MainComposeNavigator,
  viewModel: DetailViewModel = hiltViewModel()
) {
  viewModel.LifeCycleObserver(
    onCreate = { viewModel.setup(item) }
  )

  EventHandler(viewModel) { handleEvent(it, navigator) }
  ManageTopBar(viewModel)

  if (LocalConfiguration.current.isPhoneLandscape) {
    LandscapeScreen(item, pages, navigator)
  } else {
    PortraitScreen(item, pages, navigator)
  }
}

@Composable
private fun PortraitScreen(
  item: ItemBundle,
  pages: List<DetailPage>,
  navigator: MainComposeNavigator,
) {
  var page by remember(pages) { mutableStateOf(pages.first()) }

  Scaffold(
    topBar = { StandardTopBar() },
    bottomBar = {
      if (pages.size > 1) {
        NavigationBar(
          modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          pages.forEach {
            SuplaNavigationBarItem(
              selected = page == it,
              onClick = { page = it },
              icon = { it.item.Icon(page == it) },
              label = { NavigationBarLabel(it.item.stringRes) }
            )
          }
        }
      }
    }
  ) { paddings ->
    CompositionLocalProvider(LocalScaffoldPadding provides paddings) {
      Content(
        item = item,
        page = page,
        navigator = navigator
      )
    }
  }
}

@Composable
private fun LandscapeScreen(
  item: ItemBundle,
  pages: List<DetailPage>,
  navigator: MainComposeNavigator
) {
  var page by remember(pages) { mutableStateOf(pages.first()) }

  Row {
    Scaffold(
      topBar = { StandardTopBar() },
      modifier = Modifier.weight(1f)
    ) { paddings ->
      CompositionLocalProvider(LocalScaffoldPadding provides paddings) {
        Content(
          item = item,
          page = page,
          navigator = navigator
        )
      }
    }

    if (pages.size > 1) {
      NavigationRail(
        modifier = Modifier
          .fillMaxHeight()
          .border(1.dp, MaterialTheme.colorScheme.outline)
      ) {
        Column(
          modifier = Modifier.fillMaxHeight(),
          verticalArrangement = Arrangement.SpaceEvenly
        ) {
          pages.forEach {
            SuplaRailItem(
              selected = page == it,
              onClick = { page = it },
              icon = { it.item.Icon(page == it) },
              label = { NavigationBarLabel(it.item.stringRes) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun Content(
  item: ItemBundle,
  page: DetailPage,
  navigator: MainComposeNavigator
) =
  when (page) {
    DetailPage.SWITCH -> SwitchGeneralScreen(item)
    DetailPage.SWITCH_TIMER -> SwitchTimerScreen(item)
    DetailPage.THERMOSTAT -> ThermostatGeneralScreen(item)
    DetailPage.THERMOSTAT_LIST -> ThermostatSlavesListScreen(item, navigator)
    DetailPage.SCHEDULE -> ThermostatScheduleScreen(item)
    DetailPage.THERMOSTAT_HISTORY -> ThermostatHistoryScreen(item)
    DetailPage.THERMOSTAT_TIMER -> ThermostatTimerScreen(item)
    DetailPage.THERMOSTAT_HEATPOL_GENERAL -> ThermostatHeatpolGeneralScreen(item)
    DetailPage.THERMOSTAT_HEATPOL_HISTORY -> HeatpolHistoryScreen(item)
    DetailPage.RECUPERATOR_GENERAL -> RecuperatorGeneralScreen(item)
    DetailPage.RECUPERATOR_SCHEDULE -> RecuperatorScheduleScreen(item)
    DetailPage.THERMOMETER_HISTORY -> ThermometerHistoryScreen(item)
    DetailPage.HUMIDITY_HISTORY -> HumidityHistoryScreen(item)
    DetailPage.GPM_HISTORY -> GpmHistoryScreen(item)
    DetailPage.ROLLER_SHUTTER -> RollerShutterScreen(item, navigator)
    DetailPage.ROOF_WINDOW -> RoofWindowScreen(item, navigator)
    DetailPage.FACADE_BLINDS -> FacadeBlindsScreen(item, navigator)
    DetailPage.TERRACE_AWNING -> TerraceAwningScreen(item, navigator)
    DetailPage.PROJECTOR_SCREEN -> ProjectorScreenScreen(item, navigator)
    DetailPage.CURTAIN -> CurtainScreen(item, navigator)
    DetailPage.VERTICAL_BLIND -> VerticalBlindsScreen(item, navigator)
    DetailPage.GARAGE_DOOR_ROLLER -> GarageDoorScreen(item, navigator)
    DetailPage.EM_GENERAL -> ElectricityMeterGeneralScreen(item)
    DetailPage.EM_HISTORY -> ElectricityMeterHistoryScreen(item)
    DetailPage.EM_SETTINGS -> ElectricityMeterSettingsScreen(item)
    DetailPage.CONTAINER_GENERAL -> ContainerGeneralScreen(item)
    DetailPage.IC_GENERAL -> ImpulseCounterGeneralScreen(item, navigator)
    DetailPage.IC_HISTORY -> ImpulseCounterHistoryScreen(item)
    DetailPage.IC_OCR -> CounterPhotoScreen(item.remoteId, navigator)
    DetailPage.IC_SETTINGS -> ImpulseCounterSettingsScreen(item)
    DetailPage.VALVE_GENERAL -> ValveGeneralScreen(item)
    DetailPage.GATE_GENERAL -> GateGeneralScreen(item)
    DetailPage.RGB -> RgbDetailScreen(item)
    DetailPage.DIMMER -> DimmerDetailScreen(item, navigator)
    DetailPage.DIMMER_CCT -> DimmerCctDetailScreen(item)
  }

private fun handleEvent(event: DetailViewEvent, navigator: MainComposeNavigator) =
  when (event) {
    DetailViewEvent.Close -> navigator.back()
  }
