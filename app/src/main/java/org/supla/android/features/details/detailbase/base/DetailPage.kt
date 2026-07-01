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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import org.supla.android.R

@Serializable
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

  // Recuperator
  RECUPERATOR_GENERAL(DetailBottomItem.GENERAL),
  RECUPERATOR_SCHEDULE(DetailBottomItem.SCHEDULE),

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
  IC_SETTINGS(DetailBottomItem.SETTINGS),

  // Valve
  VALVE_GENERAL(DetailBottomItem.GENERAL),

  // Gate
  GATE_GENERAL(DetailBottomItem.GENERAL),

  // RGB and Dimmer
  RGB(DetailBottomItem.RGB),
  DIMMER(DetailBottomItem.DIMMER),
  DIMMER_CCT(DetailBottomItem.DIMMER),
}

enum class DetailBottomItem(@param:DrawableRes val iconRes: Int, @param:StringRes val stringRes: Int) {
  GENERAL(R.drawable.ic_bottom_general, R.string.details_general),
  SCHEDULE(R.drawable.ic_bottom_schedule, R.string.details_schedule),
  TIMER(R.drawable.ic_bottom_timer, R.string.details_timer),
  HISTORY(R.drawable.ic_bottom_history, R.string.details_history),
  METRICS(R.drawable.ic_bottom_metrics, R.string.details_metrics),
  SETTINGS(R.drawable.ic_bottom_settings, R.string.details_settings),
  LIST(R.drawable.ic_bottom_list, R.string.details_list),
  OCR(R.drawable.ic_bottom_ocr, R.string.toolbar_ocr),
  RGB(R.drawable.ic_bottom_rgb, R.string.toolbar_rgb),
  DIMMER(R.drawable.ic_bottom_dimmer, R.string.toolbar_dimmer);

  @Composable
  fun Icon(selected: Boolean) {
    when (this) {
      RGB ->
        androidx.compose.material3.Icon(
          painter = painterResource(if (selected) R.drawable.ic_bottom_rgb_selected else R.drawable.ic_bottom_rgb),
          contentDescription = stringResource(stringRes),
          tint = Color.Unspecified
        )
      DIMMER ->
        androidx.compose.material3.Icon(
          painter = painterResource(if (selected) R.drawable.ic_bottom_dimmer_selected else R.drawable.ic_bottom_dimmer),
          contentDescription = stringResource(stringRes),
          tint = Color.Unspecified
        )
      else ->
        androidx.compose.material3.Icon(
          painter = painterResource(iconRes),
          contentDescription = stringResource(stringRes)
        )
    }
  }
}
